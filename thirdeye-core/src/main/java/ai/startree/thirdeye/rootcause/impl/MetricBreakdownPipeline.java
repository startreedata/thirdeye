/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.rootcause.MaxScoreSet;
import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineContext;
import ai.startree.thirdeye.rootcause.PipelineInitContext;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.rootcause.entity.MetricEntity;
import ai.startree.thirdeye.rootcause.entity.TimeRangeEntity;
import ai.startree.thirdeye.rootcause.util.EntityUtils;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.util.DataFrameUtils;
import ai.startree.thirdeye.util.RequestContainer;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
 * Pipeline for identifying relevant dimensions by performing
 * contribution analysis. The pipeline first fetches the Current and Baseline entities and
 * MetricEntities in the search context. It then maps the metrics to ThirdEye's internal database
 * and performs contribution analysis.
 *
 * <br/><b>NOTE:</b> This is the successor to DimensionAnalysisPipeline. It relies on MetricEntities
 * with filters in the URN tail instead of DimensionEntities.
 *
 * @see DimensionAnalysisPipeline
 */
public class MetricBreakdownPipeline extends Pipeline {

  private static final Logger LOG = LoggerFactory.getLogger(MetricBreakdownPipeline.class);

  public static final String COL_CONTRIB = "contribution";
  public static final String COL_DELTA = "delta";
  public static final String COL_DIM_NAME = "dimension";
  public static final String COL_DIM_VALUE = "value";
  public static final String COL_SCORE = "score";

  public static final String PROP_PARALLELISM = "parallelism";
  public static final int PROP_PARALLELISM_DEFAULT = 1;

  public static final String PROP_K = "k";
  public static final int PROP_K_DEFAULT = -1;

  public static final String PROP_INCLUDE_DIMENSIONS = "includeDimensions";
  public static final String PROP_EXCLUDE_DIMENSIONS = "excludeDimensions";

  public static final String PROP_IGNORE_SCORE = "ignoreScore";
  public static final boolean PROP_IGNORE_SCORE_TRUE = true;
  public static final boolean PROP_IGNORE_SCORE_FALSE = false;

  public static final long TIMEOUT = 120000;

  private DataSourceCache cache;
  private MetricConfigManager metricDAO;
  private DatasetConfigManager datasetDAO;
  private ExecutorService executor;
  private Set<String> includeDimensions;
  private Set<String> excludeDimensions;
  private int k;
  private boolean ignoreScore;
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

    if (properties.containsKey(PROP_INCLUDE_DIMENSIONS)) {
      this.includeDimensions = new HashSet<>((Collection<String>) properties.get(PROP_INCLUDE_DIMENSIONS));
    } else {
      this.includeDimensions = new HashSet<>();
    }

    if (properties.containsKey(PROP_EXCLUDE_DIMENSIONS)) {
      this.excludeDimensions = new HashSet<>((Collection<String>) properties.get(PROP_EXCLUDE_DIMENSIONS));
    } else {
      this.excludeDimensions = new HashSet<>();
    }

    if (properties.containsKey(PROP_IGNORE_SCORE)) {
      this.ignoreScore = PROP_IGNORE_SCORE_TRUE;
    } else {
      this.ignoreScore = PROP_IGNORE_SCORE_FALSE;
    }
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    Set<MetricEntity> metricsEntities = context.filter(MetricEntity.class);

    final TimeRangeEntity anomaly = TimeRangeEntity.getTimeRangeAnomaly(context);
    final TimeRangeEntity baseline = TimeRangeEntity.getTimeRangeBaseline(context);

    Set<MetricEntity> output = new MaxScoreSet<>();

    for (MetricEntity me : metricsEntities) {
      try {
        final MetricSlice sliceCurrent = MetricSlice
            .from(me.getId(), anomaly.getStart(), anomaly.getEnd(), me.getFilters());
        final MetricSlice sliceBaseline = MetricSlice
            .from(me.getId(), baseline.getStart(), baseline.getEnd(), me.getFilters());

        DataFrame dfScores = getDimensionScores(sliceCurrent, sliceBaseline);

        for (int i = 0; i < dfScores.size(); i++) {
          String name = dfScores.getString(COL_DIM_NAME, i);
          String value = dfScores.getString(COL_DIM_VALUE, i);
          double score = dfScores.getDouble(COL_SCORE, i);

          if (!this.ignoreScore && score <= 0) {
            continue;
          }

          Multimap<String, String> newFilters = TreeMultimap.create(me.getFilters());
          newFilters.put(name, value);

          output.add(me.withScore(me.getScore() * score).withFilters(newFilters));
        }
      } catch (Exception e) {
        LOG.warn("Error calculating dimension scores for '{}'. Skipping.", me.getUrn(), e);
      }
    }

    return new PipelineResult(context, EntityUtils.topkNormalized(output, this.k));
  }

  private DataFrame getContribution(MetricSlice slice, String dimension) throws Exception {
    String ref = String.format("%d-%s", slice.getMetricId(), dimension);
    RequestContainer rc = DataFrameUtils
        .makeAggregateRequest(slice, Collections.singletonList(dimension), -1, ref, metricDAO,
            this.datasetDAO);
    ThirdEyeResponse res = this.cache.getQueryResult(rc.getRequest());

    DataFrame raw = DataFrameUtils.evaluateResponse(res, rc.getRequest().getMetricFunctions().get(0));

    DataFrame out = new DataFrame();
    out.addSeries(dimension, raw.getStrings(dimension));
    out.addSeries(COL_CONTRIB, raw.getDoubles(DataFrame.COL_VALUE).normalizeSum());
    out.setIndex(dimension);

    return out;
  }

  private DataFrame getContributionDelta(MetricSlice current, MetricSlice baseline,
      String dimension) throws Exception {
    DataFrame curr = getContribution(current, dimension);
    DataFrame base = getContribution(baseline, dimension);

    DataFrame joined = curr.joinOuter(base)
        .fillNull(COL_CONTRIB + DataFrame.COLUMN_JOIN_LEFT)
        .fillNull(COL_CONTRIB + DataFrame.COLUMN_JOIN_RIGHT);

    DoubleSeries diff = joined.getDoubles(COL_CONTRIB + DataFrame.COLUMN_JOIN_LEFT)
        .subtract(joined.getDoubles(COL_CONTRIB + DataFrame.COLUMN_JOIN_RIGHT));

    DataFrame df = new DataFrame();
    df.addSeries(dimension, joined.getStrings(dimension));
    df.addSeries(COL_CONTRIB, joined.getDoubles(COL_CONTRIB + DataFrame.COLUMN_JOIN_LEFT));
    df.addSeries(COL_DELTA, diff);
    df.setIndex(dimension);

    return df;
  }

  private DataFrame packDimension(DataFrame dfDelta, String dimension) {
    DataFrame df = new DataFrame();
    df.addSeries(COL_CONTRIB, dfDelta.get(COL_CONTRIB));
    df.addSeries(COL_DELTA, dfDelta.get(COL_DELTA));
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
      if (this.excludeDimensions.contains(dimension)) {
        continue;
      }

      if (this.includeDimensions.isEmpty() || this.includeDimensions.contains(dimension)) {
        futures.add(getContributionDeltaPackedAsync(current, baseline, dimension));
      }
    }

    final long timeout = System.currentTimeMillis() + TIMEOUT;
    Collection<DataFrame> contributors = new ArrayList<>();
    for (Future<DataFrame> future : futures) {
      final long timeLeft = Math.max(timeout - System.currentTimeMillis(), 0);
      contributors.add(future.get(timeLeft, TimeUnit.MILLISECONDS));
    }

    DataFrame combined = DataFrame.builder(
        COL_DIM_NAME + ":STRING",
        COL_DIM_VALUE + ":STRING",
        COL_CONTRIB + ":DOUBLE",
        COL_DELTA + ":DOUBLE").build();

    combined = combined.append(contributors.toArray(new DataFrame[contributors.size()]));
    combined.addSeries(COL_SCORE, combined.getDoubles(COL_DELTA).abs());

    return combined.sortedBy(COL_SCORE).reverse();
  }
}
