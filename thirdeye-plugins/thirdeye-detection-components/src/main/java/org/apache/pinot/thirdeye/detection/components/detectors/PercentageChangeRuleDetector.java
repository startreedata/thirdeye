/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.detection.components.detectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.detection.components.detectors.AbsoluteChangeRuleDetector.windowMatch;
import static org.apache.pinot.thirdeye.detection.components.detectors.MeanVarianceRuleDetector.patternMatch;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_ANOMALY;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_CURRENT;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_DIFF;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_DIFF_VIOLATION;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_IN_WINDOW;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_LOWER_BOUND;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_PATTERN;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_TIME;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_UPPER_BOUND;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_VALUE;
import static org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries.POSITIVE_INFINITY;
import static org.apache.pinot.thirdeye.spi.dataframe.Series.DoubleFunction;
import static org.apache.pinot.thirdeye.spi.dataframe.Series.map;
import static org.apache.pinot.thirdeye.spi.detection.DetectionUtils.aggregateByPeriod;
import static org.apache.pinot.thirdeye.spi.detection.DetectionUtils.buildDetectionResult;
import static org.apache.pinot.thirdeye.spi.detection.DetectionUtils.filterIncompleteAggregation;
import static org.apache.pinot.thirdeye.spi.detection.Pattern.DOWN;
import static org.apache.pinot.thirdeye.spi.detection.Pattern.UP;
import static org.apache.pinot.thirdeye.spi.detection.Pattern.UP_OR_DOWN;
import static org.apache.pinot.thirdeye.spi.detection.Pattern.valueOf;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.pinot.thirdeye.detection.components.SimpleAnomalyDetectorV2Result;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.Series;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetector;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Result;
import org.apache.pinot.thirdeye.spi.detection.BaselineParsingUtils;
import org.apache.pinot.thirdeye.spi.detection.BaselineProvider;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.DetectorException;
import org.apache.pinot.thirdeye.spi.detection.InputDataFetcher;
import org.apache.pinot.thirdeye.spi.detection.MetricAggFunction;
import org.apache.pinot.thirdeye.spi.detection.Pattern;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.model.InputData;
import org.apache.pinot.thirdeye.spi.detection.model.InputDataSpec;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.rootcause.impl.MetricEntity;
import org.apache.pinot.thirdeye.spi.rootcause.timeseries.Baseline;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.ReadableInterval;

/**
 * Computes a multi-week aggregate baseline and compares the current value based on relative change.
 */
public class PercentageChangeRuleDetector implements
    AnomalyDetector<PercentageChangeRuleDetectorSpec>,
    AnomalyDetectorV2<PercentageChangeRuleDetectorSpec>,
    BaselineProvider<PercentageChangeRuleDetectorSpec> {

  private double percentageChange;
  private InputDataFetcher dataFetcher;
  private Baseline baseline;
  private Pattern pattern;
  private String monitoringGranularity;
  private TimeGranularity timeGranularity;
  private DayOfWeek weekStart;
  private PercentageChangeRuleDetectorSpec spec;
  private Period monitoringGranularityPeriod;

  @Override
  public void init(final PercentageChangeRuleDetectorSpec spec) {
    this.spec = spec;
    checkArgument(!Double.isNaN(spec.getPercentageChange()), "Percentage change is not set.");
    percentageChange = spec.getPercentageChange();
    baseline = BaselineParsingUtils.parseOffset(spec.getOffset(), spec.getTimezone());
    pattern = valueOf(spec.getPattern().toUpperCase());

    monitoringGranularity = spec.getMonitoringGranularity();
    if (monitoringGranularity.endsWith(TimeGranularity.MONTHS) || monitoringGranularity
        .endsWith(TimeGranularity.WEEKS)) {
      timeGranularity = MetricSlice.NATIVE_GRANULARITY;
    } else {
      timeGranularity = TimeGranularity.fromString(spec.getMonitoringGranularity());
    }
    if (monitoringGranularity.endsWith(TimeGranularity.WEEKS)) {
      weekStart = DayOfWeek.valueOf(spec.getWeekStart());
    }
  }

  @Override
  public void init(final PercentageChangeRuleDetectorSpec spec,
      final InputDataFetcher dataFetcher) {
    init(spec);
    this.dataFetcher = dataFetcher;
  }

  @Override
  public AnomalyDetectorV2Result runDetection(final Interval window,
      final Map<String, DataTable> timeSeriesMap) throws DetectorException {
    setMonitoringGranularityPeriod();
    final DataTable baseline = requireNonNull(timeSeriesMap.get(KEY_BASELINE), "baseline is null");
    final DataTable current = requireNonNull(timeSeriesMap.get(KEY_CURRENT), "current is null");
    final DataFrame baselineDf = baseline.getDataFrame();
    final DataFrame currentDf = current.getDataFrame();
    currentDf
        .renameSeries(spec.getTimestamp(), COL_TIME)
        .renameSeries(spec.getMetric(), COL_CURRENT)
        .setIndex(COL_TIME)
        .addSeries(COL_VALUE, baselineDf.get(spec.getMetric()));

    return runDetectionOnSingleDataTable(currentDf, window);
  }

  private void setMonitoringGranularityPeriod() {
    requireNonNull(spec.getMonitoringGranularity(),
        "monitoringGranularity is mandatory in v2 interface");
    checkArgument(!MetricSlice.NATIVE_GRANULARITY.toAggregationGranularityString().equals(
        spec.getMonitoringGranularity()), "NATIVE_GRANULARITY not supported in v2 interface");

    monitoringGranularityPeriod = DetectionUtils.getMonitoringGranularityPeriod(spec.getMonitoringGranularity(),
        null);
  }

  @Override
  public DetectionResult runDetection(final Interval window, final String metricUrn) {
    DateTime windowStart = window.getStart();

    // align start day to the user specified week start
    if (Objects.nonNull(weekStart)) {
      windowStart = window
          .getStart()
          .withTimeAtStartOfDay()
          .withDayOfWeek(weekStart.getValue())
          .minusWeeks(1);
    }

    final MetricEntity me = MetricEntity.fromURN(metricUrn);
    final MetricSlice slice = MetricSlice.from(me.getId(),
        windowStart.getMillis(),
        window.getEndMillis(),
        me.getFilters(),
        timeGranularity);

    final List<MetricSlice> slices = new ArrayList<>(baseline.scatter(slice));
    slices.add(slice);

    final InputData data = dataFetcher
        .fetchData(new InputDataSpec().withTimeseriesSlices(slices)
            .withMetricIdsForDataset(singletonList(slice.getMetricId()))
            .withMetricIds(singletonList(me.getId())));

    DataFrame dfBase = baseline.gather(slice, data.getTimeseries());
    DataFrame dfCurr = data.getTimeseries().get(slice);

    final DatasetConfigDTO datasetConfig = data.getDatasetForMetricId().get(me.getId());
    monitoringGranularityPeriod = DetectionUtils
        .getMonitoringGranularityPeriod(spec.getMonitoringGranularity(), datasetConfig);
    // Hack. To be removed when deprecating v1 pipeline
    spec.setTimezone(datasetConfig.getTimezone());

    // aggregate data to specified weekly granularity
    if (monitoringGranularity.endsWith(TimeGranularity.WEEKS)) {
      final long latestDataTimeStamp = dfCurr.getLong(COL_TIME, dfCurr.size() - 1);

      final MetricConfigDTO metricConfig = data.getMetrics().get(me.getId());
      final MetricAggFunction defaultAggFunction = metricConfig.getDefaultAggFunction();

      dfCurr = aggregateByPeriod(dfCurr,
          windowStart,
          monitoringGranularityPeriod,
          defaultAggFunction);

      dfCurr = filterIncompleteAggregation(dfCurr,
          latestDataTimeStamp,
          datasetConfig.bucketTimeGranularity(),
          monitoringGranularityPeriod);

      dfBase = aggregateByPeriod(dfBase,
          windowStart,
          monitoringGranularityPeriod,
          defaultAggFunction);
    }

    // Renaming the metric column so that the join doesn't overwrite.
    dfCurr.renameSeries(COL_VALUE, COL_CURRENT);

    // Inner Join the current and baseline series
    final DataFrame mergedDf = new DataFrame(dfCurr).addSeries(dfBase);
    final AnomalyDetectorV2Result detectorResult = runDetectionOnSingleDataTable(mergedDf, window);

    return buildDetectionResult(detectorResult);
  }

  private AnomalyDetectorV2Result runDetectionOnSingleDataTable(final DataFrame inputDf,
      final ReadableInterval window) {
    inputDf
        // calculate percentage change
        .addSeries(COL_DIFF, percentageChanges(inputDf))
        .addSeries(COL_PATTERN, patternMatch(pattern, inputDf))
        .addSeries(COL_DIFF_VIOLATION, inputDf.getDoubles(COL_DIFF).abs().gte(percentageChange))
        .addSeries(COL_IN_WINDOW, windowMatch(inputDf.getLongs(COL_TIME), window))
        .mapInPlace(BooleanSeries.ALL_TRUE, COL_ANOMALY,
            COL_PATTERN,
            COL_DIFF_VIOLATION,
            COL_IN_WINDOW);
    addBoundaries(inputDf);

    return
        new SimpleAnomalyDetectorV2Result(inputDf, spec.getTimezone(), monitoringGranularityPeriod);
  }

  private Series percentageChanges(final DataFrame inputDf) {
    return map((DoubleFunction) this::percentageChangeLambda,
        inputDf.getDoubles(COL_CURRENT),
        inputDf.getDoubles(COL_VALUE));
  }

  private double percentageChangeLambda(final double[] values) {
    final double first = values[0];
    final double second = values[1];

    if (Double.compare(second, 0.0) == 0) {
      return Double.compare(first, 0.0) == 0 ? 0.0
          : (first > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
    }
    return (first - second) / second;
  }

  @Override
  public TimeSeries computePredictedTimeSeries(final MetricSlice slice) {
    final DataFrame df = DetectionUtils.buildBaselines(slice, baseline, dataFetcher);
    addBoundaries(df);
    return TimeSeries.fromDataFrame(df);
  }

  private void addBoundaries(final DataFrame inputDf) {
    //default bounds
    DoubleSeries upperBound = DoubleSeries.fillValues(inputDf.size(), POSITIVE_INFINITY);
    //fixme cyril this not consistent with threshold rule detector default values
    DoubleSeries lowerBound = DoubleSeries.zeros(inputDf.size());
    if (pattern == UP || pattern == UP_OR_DOWN) {
      upperBound = inputDf.getDoubles(COL_VALUE).multiply(1 + percentageChange);
    }
    if (pattern == DOWN || pattern == UP_OR_DOWN) {
      lowerBound = inputDf.getDoubles(COL_VALUE).multiply(1 - percentageChange);
    }
    inputDf.addSeries(COL_UPPER_BOUND, upperBound);
    inputDf.addSeries(COL_LOWER_BOUND, lowerBound);
  }
}
