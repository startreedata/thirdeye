/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.cube.data.cube.Cube;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.rootcause.MaxScoreSet;
import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineContext;
import ai.startree.thirdeye.rootcause.PipelineInitContext;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.rootcause.entity.DimensionEntity;
import ai.startree.thirdeye.rootcause.entity.MetricEntity;
import ai.startree.thirdeye.rootcause.entity.TimeRangeEntity;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.util.DataFrameUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MetricComponentAnalysisPipeline performs iterative factor analysis on a metric's dimensions.
 * Returns the k biggest outliers (contributors to relative change, similar to principal
 * components).
 *
 * Iteration executes by choosing the the dimension value (slice) with the biggest change in
 * contribution and then re-running the analysis while excluding this (and all previously chosen)
 * slices from the dataset. The result is an ordered list of the top k slices with the biggest
 * relative change.
 *
 * @see Cube
 */
public class MetricComponentAnalysisPipeline extends Pipeline {

  private static final Logger LOG = LoggerFactory.getLogger(MetricComponentAnalysisPipeline.class);

  private static final String COL_RAW = "raw";
  private static final String COL_CONTRIB = "contribution";
  private static final String COL_DELTA = "delta";
  private static final String COL_DIM_NAME = "dimension";
  private static final String COL_DIM_VALUE = "value";
  private static final String COL_SCORE = "score";

  private static final String PROP_PARALLELISM = "parallelism";
  private static final int PROP_PARALLELISM_DEFAULT = 1;

  private static final String PROP_K = "k";
  private static final int PROP_K_DEFAULT = 3;

  private static final String PROP_EXCLUDE_DIMENSIONS = "excludeDimensions";
  private static final Set<String> PROP_EXCLUDE_DIMENSIONS_DEFAULT = Collections.emptySet();

  public static final long TIMEOUT = 120000;

  private DataSourceCache cache;
  private MetricConfigManager metricDAO;
  private DatasetConfigManager datasetDAO;
  private ExecutorService executor;
  private Set<String> excludeDimensions;
  private int k;
  private ThirdEyeCacheRegistry thirdEyeCacheRegistry;

  @Override
  public void init(final PipelineInitContext context) {
    super.init(context);
    Map<String, Object> properties = context.getProperties();
    this.metricDAO = context.getMetricConfigManager();
    this.datasetDAO = context.getDatasetConfigManager();
    this.cache = context.getDataSourceCache();
    this.thirdEyeCacheRegistry = context.getThirdEyeCacheRegistry();

    this.executor = Executors.newFixedThreadPool(
        MapUtils.getInteger(properties, PROP_PARALLELISM, PROP_PARALLELISM_DEFAULT));
    this.k = MapUtils.getInteger(properties, PROP_K, PROP_K_DEFAULT);

    if (properties.containsKey(PROP_EXCLUDE_DIMENSIONS)) {
      this.excludeDimensions = new HashSet<>((Collection<String>) properties.get(
          PROP_EXCLUDE_DIMENSIONS));
    } else {
      this.excludeDimensions = PROP_EXCLUDE_DIMENSIONS_DEFAULT;
    }
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    Set<MetricEntity> metricsEntities = context.filter(MetricEntity.class);

    final TimeRangeEntity anomaly = TimeRangeEntity.getTimeRangeAnomaly(context);
    final TimeRangeEntity baseline = TimeRangeEntity.getTimeRangeBaseline(context);

    final Multimap<String, String> rawDimensions = HashMultimap.create();
    final Set<DimensionEntity> dimensions = new MaxScoreSet<>();

    if (metricsEntities.isEmpty()) {
      return new PipelineResult(context, new MaxScoreSet<>());
    }

    if (metricsEntities.size() > 1) {
      // NOTE: emergency brake, expensive computation and queries
      throw new IllegalArgumentException("Cannot process more than one metric at a time");
    }

    final MetricEntity metric = metricsEntities.iterator().next();

    // collects filters over multiple iterations
    final Multimap<String, String> filters = HashMultimap.create(metric.getFilters());

    // metric total for score calculation
    final MetricSlice sliceTotal = MetricSlice
        .from(metric.getId(), anomaly.getStart(), anomaly.getEnd(), filters);
    final double total;
    try {
      total = getTotal(sliceTotal);
    } catch (Exception e) {
      LOG.warn("Could not retrieve total for '{}'", metric.getUrn());
      return new PipelineResult(context, dimensions);
    }

    for (int k = 0; k < this.k; k++) {
      try {
        final MetricSlice sliceCurrent = MetricSlice
            .from(metric.getId(), anomaly.getStart(), anomaly.getEnd(), filters);
        final MetricSlice sliceBaseline = MetricSlice
            .from(metric.getId(), baseline.getStart(), baseline.getEnd(), filters);

        final double subTotal = getTotal(sliceCurrent);

        final DataFrame dfScoresRaw = getDimensionScores(sliceCurrent, sliceBaseline);

        final double percentage = Math.round(subTotal / total * 10000) / 100.0;
        LOG.info("Iteration {}: analyzing '{}' ({} %)\n{}", k, filters, percentage,
            dfScoresRaw.head(20));

        // ignore zero scores, known combinations
        final DataFrame dfScores = dfScoresRaw
            .filter(new Series.DoubleConditional() {
              @Override
              public boolean apply(double... values) {
                return values[0] > 0;
              }
            }, COL_SCORE)
            .dropNull();

        if (dfScores.isEmpty()) {
          break;
        }

        String name = dfScores.getString(COL_DIM_NAME, 0);
        String value = dfScores.getString(COL_DIM_VALUE, 0);
        // double score = Math.abs(dfScores.getDouble(COL_DELTA, 0)); // scaling issue
        double score = subTotal / total;

        rawDimensions.put(name, value);
        dimensions.add(DimensionEntity
            .fromDimension(score * metric.getScore(), name, value, DimensionEntity.TYPE_GENERATED));

        filters.put(name, "!" + value);
      } catch (Exception e) {
        LOG.warn("Error calculating dimension scores for '{}'. Skipping.", metric.getUrn(), e);
      }
    }

    return new PipelineResult(context, dimensions);
  }

  private double getTotal(MetricSlice slice) throws Exception {
    String ref = String.format("%d", slice.getMetricId());
    ThirdEyeRequest thirdEyeRequest = DataFrameUtils.makeAggregateRequest(slice,
        Collections.emptyList(),
        -1,
        ref,
        metricDAO,
        this.datasetDAO);
    ThirdEyeResponse res = this.cache.getQueryResult(thirdEyeRequest);

    DataFrame raw = DataFrameUtils.evaluateResponse(res,
        thirdEyeRequest.getMetricFunctions().get(0));

    return raw.getDoubles(DataFrame.COL_VALUE).doubleValue();
  }

  private DataFrame getContribution(MetricSlice slice, String dimension) throws Exception {
    String ref = String.format("%d-%s", slice.getMetricId(), dimension);
    ThirdEyeRequest thirdEyeRequest = DataFrameUtils.makeAggregateRequest(slice,
        Collections.singletonList(dimension),
        -1,
        ref,
        metricDAO,
        this.datasetDAO);
    ThirdEyeResponse res = this.cache.getQueryResult(thirdEyeRequest);

    DataFrame raw = DataFrameUtils.evaluateResponse(res,
        thirdEyeRequest.getMetricFunctions().get(0));

    DataFrame out = new DataFrame();
    out.addSeries(dimension, raw.getStrings(dimension));
    out.addSeries(COL_CONTRIB, raw.getDoubles(DataFrame.COL_VALUE).normalizeSum());
    out.addSeries(COL_RAW, raw.getDoubles(DataFrame.COL_VALUE));
    out.setIndex(dimension);

    return out;
  }

  private DataFrame getContributionDelta(MetricSlice current, MetricSlice baseline,
      String dimension) throws Exception {
    DataFrame curr = getContribution(current, dimension);
    DataFrame base = getContribution(baseline, dimension);

    DataFrame joined = curr.joinOuter(base)
        .fillNull(COL_CONTRIB + DataFrame.COLUMN_JOIN_LEFT)
        .fillNull(COL_CONTRIB + DataFrame.COLUMN_JOIN_RIGHT)
        .fillNull(COL_RAW + DataFrame.COLUMN_JOIN_LEFT)
        .fillNull(COL_RAW + DataFrame.COLUMN_JOIN_RIGHT);

    DoubleSeries diff = joined.getDoubles(COL_CONTRIB + DataFrame.COLUMN_JOIN_LEFT)
        .subtract(joined.getDoubles(COL_CONTRIB + DataFrame.COLUMN_JOIN_RIGHT));

    DoubleSeries diffRaw = joined.getDoubles(COL_RAW + DataFrame.COLUMN_JOIN_LEFT)
        .subtract(joined.getDoubles(COL_RAW + DataFrame.COLUMN_JOIN_RIGHT));

    DataFrame df = new DataFrame();
    df.addSeries(dimension, joined.getStrings(dimension));
    df.addSeries(COL_CONTRIB, joined.getDoubles(COL_CONTRIB + DataFrame.COLUMN_JOIN_LEFT));
    df.addSeries(COL_DELTA, diff);
    df.addSeries(COL_RAW, diffRaw);
    df.setIndex(dimension);

    return df;
  }

  private DataFrame packDimension(DataFrame dfDelta, String dimension) {
    DataFrame df = new DataFrame();
    df.addSeries(COL_CONTRIB, dfDelta.get(COL_CONTRIB));
    df.addSeries(COL_DELTA, dfDelta.get(COL_DELTA));
    df.addSeries(COL_RAW, dfDelta.get(COL_RAW));
    df.addSeries(COL_DIM_NAME, StringSeries.fillValues(dfDelta.size(), dimension));
    df.addSeries(COL_DIM_VALUE, dfDelta.get(dimension));
    return df;
  }

  private Future<DataFrame> getContributionDeltaPackedAsync(final MetricSlice current,
      final MetricSlice baseline, final String dimension) throws Exception {
    return this.executor.submit(new Callable<DataFrame>() {
      @Override
      public DataFrame call() throws Exception {
        return packDimension(getContributionDelta(current, baseline, dimension), dimension);
      }
    });
  }

  private DataFrame getDimensionScores(MetricSlice current, MetricSlice baseline) throws Exception {
    if (current.getMetricId() != baseline.getMetricId()) {
      throw new IllegalArgumentException("current and baseline must reference the same metric id");
    }

    MetricConfigDTO metric = this.metricDAO.findById(current.getMetricId());
    if (metric == null) {
      throw new IllegalArgumentException(
          String.format("Could not resolve metric id '%d'", current.getMetricId()));
    }

    DatasetConfigDTO dataset = this.datasetDAO.findByDataset(metric.getDataset());
    if (dataset == null) {
      throw new IllegalArgumentException(String
          .format("Could not resolve dataset '%s' for metric id '%d'", metric.getDataset(),
              metric.getId()));
    }

    if (!dataset.isAdditive()) {
      LOG.warn("Contribution analysis on non-additive dataset");

      // TODO read additive from metric property when available
      //throw new IllegalArgumentException(String.format("Requires additive dataset, but '%s' isn't.", dataset.getDataset()));
    }

    Collection<Future<DataFrame>> futures = new ArrayList<>();
    for (String dimension : dataset.getDimensions()) {
      // don't explore dimensions that are excluded
      if (this.excludeDimensions.contains(dimension)) {
        continue;
      }

      futures.add(getContributionDeltaPackedAsync(current, baseline, dimension));
    }

    final long timeout = System.currentTimeMillis() + TIMEOUT;
    List<DataFrame> contributors = new ArrayList<>();
    for (Future<DataFrame> future : futures) {
      final long timeLeft = Math.max(timeout - System.currentTimeMillis(), 0);
      contributors.add(future.get(timeLeft, TimeUnit.MILLISECONDS));
    }

    DataFrame combined = DataFrame.builder(
        COL_DIM_NAME + ":STRING",
        COL_DIM_VALUE + ":STRING",
        COL_CONTRIB + ":DOUBLE",
        COL_DELTA + ":DOUBLE",
        COL_RAW + ":DOUBLE").build();

    combined = combined.append(contributors);
    combined.addSeries(COL_SCORE, combined.getDoubles(COL_DELTA).abs());

    return combined.sortedBy(COL_SCORE).reverse();
  }
}
