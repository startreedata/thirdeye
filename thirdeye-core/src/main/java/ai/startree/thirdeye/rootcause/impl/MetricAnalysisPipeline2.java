/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.rootcause.BaselineAggregate;
import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineContext;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.rootcause.entity.MetricEntity;
import ai.startree.thirdeye.rootcause.entity.TimeRangeEntity;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.detection.BaselineAggregateType;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.util.DataFrameUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The MetricAnalysisPipeline ranks metrics based on the degree of anomalous behavior during
 * the anomaly period. It scores anomalous behavior with user-configured strategies.
 *
 * <br/><b>NOTE:</b> this is the successor to {@code MetricAnalysisPipeline}. It supports
 * computation
 * of complex, named training windows.
 *
 * @see MetricAnalysisPipeline
 */
public class MetricAnalysisPipeline2 extends Pipeline {

  private static final Logger LOG = LoggerFactory.getLogger(MetricAnalysisPipeline2.class);

  private static final long TIMEOUT = 60000;

  private static final long TRAINING_WINDOW = TimeUnit.DAYS.toMillis(7);

  private static final String COL_TIME = DataFrame.COL_TIME;
  private static final String COL_VALUE = DataFrame.COL_VALUE;
  private static final String COL_CURRENT = "current";
  private static final String COL_BASELINE = "baseline";

  private final DataSourceCache cache;
  private final MetricConfigManager metricDAO;
  private final DatasetConfigManager datasetDAO;
  private final ScoringStrategyFactory strategyFactory;
  private final TimeGranularity granularity;

  /**
   * Constructor for dependency injection
   *
   * @param strategyFactory scoring strategy for differences
   * @param granularity time series target granularity
   * @param cache query cache
   * @param metricDAO metric config DAO
   * @param datasetDAO datset config DAO
   */
  public MetricAnalysisPipeline2(ScoringStrategyFactory strategyFactory,
      TimeGranularity granularity,
      DataSourceCache cache,
      MetricConfigManager metricDAO,
      DatasetConfigManager datasetDAO) {
    super();
    this.cache = cache;
    this.metricDAO = metricDAO;
    this.datasetDAO = datasetDAO;
    this.strategyFactory = strategyFactory;
    this.granularity = granularity;
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    Set<MetricEntity> metrics = context.filter(MetricEntity.class);

    TimeRangeEntity anomalyRange = TimeRangeEntity.getTimeRangeAnomaly(context);
    TimeRangeEntity baselineRange = TimeRangeEntity.getTimeRangeBaseline(context);

    BaselineAggregate rangeCurrent = BaselineAggregate.fromOffsets(BaselineAggregateType.MEDIAN,
        Collections.singletonList(new Period(0, PeriodType.weeks())), DateTimeZone.UTC);
    BaselineAggregate rangeBaseline = BaselineAggregate
        .fromWeekOverWeek(BaselineAggregateType.MEDIAN, 4, 0, DateTimeZone.UTC);

    Map<MetricEntity, MetricSlice> trainingSet = new HashMap<>();
    Map<MetricEntity, MetricSlice> testSet = new HashMap<>();
    Set<MetricSlice> slicesRaw = new HashSet<>();
    for (MetricEntity me : metrics) {
      MetricSlice sliceTrain = MetricSlice
          .from(me.getId(), anomalyRange.getStart() - TRAINING_WINDOW, anomalyRange.getStart(),
              me.getFilters(), this.granularity);
      MetricSlice sliceTest = MetricSlice
          .from(me.getId(), anomalyRange.getStart(), anomalyRange.getEnd(), me.getFilters(),
              this.granularity);

      trainingSet.put(me, sliceTrain);
      testSet.put(me, sliceTest);

      // TODO make training data cacheable (e.g. align to hours, days)

      slicesRaw.addAll(rangeCurrent.scatter(sliceTest));
      slicesRaw.addAll(rangeCurrent.scatter(sliceTrain));
      slicesRaw.addAll(rangeBaseline.scatter(sliceTest));
      slicesRaw.addAll(rangeBaseline.scatter(sliceTrain));
    }

    // cyril - after refactoring - will not work - use a non deprecated metricSlice.from
    List<ThirdEyeRequest> requestList = makeRequests(slicesRaw);

    LOG.info("Requesting {} time series", requestList.size());
    List<ThirdEyeRequest> thirdeyeRequests = new ArrayList<>();
    Map<String, ThirdEyeRequest> requests = new HashMap<>();
    for (ThirdEyeRequest thirdEyeRequest : requestList) {
      requests.put(thirdEyeRequest.getRequestReference(), thirdEyeRequest);
      thirdeyeRequests.add(thirdEyeRequest);
    }

    Collection<Future<ThirdEyeResponse>> futures = submitRequests(thirdeyeRequests);

    // fetch responses and calculate derived metrics
    int i = 0;
    Map<String, DataFrame> responses = new HashMap<>();
    for (Future<ThirdEyeResponse> future : futures) {
      ThirdEyeResponse response;
      try {
        response = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } catch (Exception e) {
        LOG.warn("Error executing request '{}'. Skipping.",
            requestList.get(i).getRequestReference(), e);
        continue;
      } finally {
        i++;
      }

      // parse time series
      String id = response.getRequest().getRequestReference();
      DataFrame df;
      try {
        df = DataFrameUtils.evaluateResponse(response);
      } catch (Exception e) {
        LOG.warn("Could not parse response for '{}'. Skipping.", id, e);
        continue;
      }

      // store time series
      responses.put(id, df);
    }

    // Collect slices and requests
    Map<String, MetricSlice> id2slice = new HashMap<>();
    for (MetricSlice slice : slicesRaw) {
      id2slice.put(makeIdentifier(slice), slice);
    }

    Map<MetricSlice, DataFrame> data = new HashMap<>();
    for (Map.Entry<String, DataFrame> entry : responses.entrySet()) {
      MetricSlice slice = id2slice.get(entry.getKey());
      if (slice == null) {
        LOG.warn("Could not associate response id '{}' with request. Skipping.", entry.getKey());
        continue;
      }

      data.put(slice, entry.getValue());
    }

    // score metrics
    Set<MetricEntity> output = new HashSet<>();
    for (MetricEntity me : metrics) {
      try {
        MetricSlice sliceTest = testSet.get(me);
        MetricSlice sliceTrain = trainingSet.get(me);

        boolean isDailyData = this.isDailyData(sliceTest.getMetricId());

        DataFrame testCurrent = rangeCurrent.gather(sliceTest, data);
        DataFrame testBaseline = rangeBaseline.gather(sliceTest, data);
        DataFrame trainingCurrent = rangeCurrent.gather(sliceTrain, data);
        DataFrame trainingBaseline = rangeBaseline.gather(sliceTrain, data);

        DataFrame trainingDiff = diffTimeseries(trainingCurrent, trainingBaseline);
        DataFrame testDiff = diffTimeseries(testCurrent, testBaseline);

        LOG.info("trainingBaseline ({} rows):\n{}", trainingBaseline.size(), trainingBaseline);
        LOG.info("trainingCurrent ({} rows):\n{}", trainingCurrent.size(), trainingCurrent);
        LOG.info("testBaseline ({} rows):\n{}", testBaseline.size(), testBaseline);
        LOG.info("testCurrent ({} rows):\n{}", testCurrent.size(), testCurrent);
        LOG.info("trainingDiff ({} rows):\n{}", trainingDiff.size(), trainingDiff);
        LOG.info("testDiff ({} rows):\n{}", testDiff.size(), testDiff);

        double score = this.strategyFactory.fit(trainingDiff).apply(testDiff);

        List<Entity> related = new ArrayList<>();
        related.add(anomalyRange);
        related.add(baselineRange);

        output.add(me.withScore(score).withRelated(related));
      } catch (Exception e) {
        LOG.warn("Error processing '{}'. Skipping", me.getUrn(), e);
      }
    }

    LOG.info("Generated {} MetricEntities with valid scores", output.size());

    return new PipelineResult(context, output);
  }

  private boolean isDailyData(long metricId) {
    MetricConfigDTO metric = this.metricDAO.findById(metricId);
    if (metric == null) {
      return false;
    }

    DatasetConfigDTO dataset = this.datasetDAO.findByDataset(metric.getDataset());
    if (dataset == null) {
      return false;
    }

    return TimeUnit.DAYS.equals(dataset.bucketTimeGranularity().getUnit());
  }

  private DataFrame diffTimeseries(DataFrame current, DataFrame baseline) {
    DataFrame offsetCurrent = new DataFrame(COL_TIME, current.getLongs(COL_TIME))
        .addSeries(COL_CURRENT, current.getDoubles(COL_VALUE));
    DataFrame offsetBaseline = new DataFrame(COL_TIME, baseline.getLongs(COL_TIME))
        .addSeries(COL_BASELINE, baseline.getDoubles(COL_VALUE));
    DataFrame joined = offsetCurrent.joinInner(offsetBaseline);
    joined.addSeries(COL_VALUE,
        joined.getDoubles(COL_CURRENT).subtract(joined.getDoubles(COL_BASELINE)));
    return joined;
  }

  private Collection<Future<ThirdEyeResponse>> submitRequests(
      List<ThirdEyeRequest> thirdeyeRequests) {
    try {
      return this.cache.getQueryResultsAsync(thirdeyeRequests).values();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<ThirdEyeRequest> makeRequests(Collection<MetricSlice> slices) {
    List<ThirdEyeRequest> requests = new ArrayList<>();
    for (MetricSlice slice : slices) {
      try {
        // cyril - after refactoring - will not work - use a non deprecated metricSlice.from
        requests.add(DataFrameUtils.makeTimeSeriesRequestAligned(slice, makeIdentifier(slice)));
      } catch (Exception ex) {
        LOG.warn("Could not make request. Skipping. ", ex);
      }
    }
    return requests;
  }

  private static String makeIdentifier(MetricSlice slice) {
    return String.valueOf(slice.hashCode());
  }

  private interface ScoringStrategyFactory {

    ScoringStrategy fit(DataFrame training);
  }

  private interface ScoringStrategy {

    double apply(DataFrame data);
  }

  private static class ThresholdStrategyFactory implements ScoringStrategyFactory {

    final double[] quantiles;

    ThresholdStrategyFactory(double... quantiles) {
      this.quantiles = quantiles;
    }

    @Override
    public ScoringStrategy fit(DataFrame training) {
      DoubleSeries train = training.getDoubles(COL_VALUE);
      double[] thresholds = new double[this.quantiles.length];
      for (int i = 0; i < thresholds.length; i++) {
        thresholds[i] = train.abs().quantile(this.quantiles[i]).fillNull(Double.POSITIVE_INFINITY)
            .doubleValue();
      }
      return new ThresholdStrategy(thresholds);
    }
  }

  private static class ThresholdStrategy implements ScoringStrategy {

    final double[] thresholds;

    ThresholdStrategy(double... thresholds) {
      this.thresholds = thresholds;
    }

    @Override
    public double apply(DataFrame data) {
      DoubleSeries test = data.getDoubles(COL_VALUE);

      LOG.info("thresholds: {}", this.thresholds);

      long sumViolations = 0;
      for (final double t : thresholds) {
        sumViolations += test.abs().map(new Series.DoubleConditional() {
          @Override
          public boolean apply(double... values) {
            return values[0] > t;
          }
        }).sum().fillNull().longValue();
      }

      final int max = test.size() * this.thresholds.length;
      if (max <= 0) {
        return 0;
      }

      return sumViolations / (double) max;
    }
  }
}
