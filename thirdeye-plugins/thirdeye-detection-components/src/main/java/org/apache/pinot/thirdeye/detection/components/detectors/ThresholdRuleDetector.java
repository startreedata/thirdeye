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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.components.detectors.results.DataTableUtils;
import org.apache.pinot.thirdeye.detection.components.detectors.results.DimensionInfo;
import org.apache.pinot.thirdeye.detection.components.detectors.results.GroupedDetectionResults;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetector;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.BaselineProvider;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.DetectorException;
import org.apache.pinot.thirdeye.spi.detection.InputDataFetcher;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.model.InputData;
import org.apache.pinot.thirdeye.spi.detection.model.InputDataSpec;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.rootcause.impl.MetricEntity;
import org.joda.time.Interval;
import org.joda.time.Period;

/**
 * Simple threshold rule algorithm with (optional) upper and lower bounds on a metric value.
 */
public class ThresholdRuleDetector implements AnomalyDetector<ThresholdRuleDetectorSpec>,
    AnomalyDetectorV2<ThresholdRuleDetectorSpec>,
    BaselineProvider<ThresholdRuleDetectorSpec> {

  private static final String COL_TOO_HIGH = "tooHigh";
  private static final String COL_TOO_LOW = "tooLow";

  private InputDataFetcher dataFetcher;
  private TimeGranularity timeGranularity;
  private ThresholdRuleDetectorSpec spec;
  private Period monitoringGranularityPeriod;

  @Override
  public void init(final ThresholdRuleDetectorSpec spec) {
    this.spec = spec;

    final String monitoringGranularity = spec.getMonitoringGranularity();
    if (monitoringGranularity.equals("1_MONTHS")) {
      timeGranularity = MetricSlice.NATIVE_GRANULARITY;
    } else {
      timeGranularity = TimeGranularity.fromString(spec.getMonitoringGranularity());
    }
  }

  @Override
  public void init(final ThresholdRuleDetectorSpec spec, final InputDataFetcher dataFetcher) {
    init(spec);
    this.dataFetcher = dataFetcher;
  }

  @Override
  public DetectionPipelineResult runDetection(final Interval interval,
      final Map<String, DataTable> timeSeriesMap
  ) throws DetectorException {
    setMonitoringGranularityPeriod();

    final DataTable current = requireNonNull(timeSeriesMap.get(KEY_CURRENT), "current is null");
    final Map<DimensionInfo, DataTable> currentDataTableMap = DataTableUtils.splitDataTable(
        current);
    final List<DetectionResult> detectionResults = new ArrayList<>();
    for (final DimensionInfo dimensionInfo : currentDataTableMap.keySet()) {
      final DataFrame currentDf = currentDataTableMap.get(dimensionInfo).getDataFrame();
      currentDf.addSeries(DataFrame.COL_TIME, currentDf.get(spec.getTimestamp()));
      currentDf.addSeries(DataFrame.COL_CURRENT, currentDf.get(spec.getMetric()));

      final DetectionResult detectionResult = runDetectionOnSingleDataTable(interval, currentDf);
      detectionResults.add(detectionResult);
    }
    return new GroupedDetectionResults(detectionResults);
  }

  private void setMonitoringGranularityPeriod() {
    requireNonNull(spec.getMonitoringGranularity(),
        "monitoringGranularity is mandatory in v2 interface");
    checkArgument(!MetricSlice.NATIVE_GRANULARITY.toAggregationGranularityString().equals(
        spec.getMonitoringGranularity()), "NATIVE_GRANULARITY not supported in v2 interface");

    monitoringGranularityPeriod = DetectionUtils.getMonitoringGranularityPeriod(
        spec.getMonitoringGranularity(),
        null);
  }

  @Override
  public DetectionResult runDetection(final Interval window, final String metricUrn) {
    final MetricEntity me = MetricEntity.fromURN(metricUrn);
    final long endTime = window.getEndMillis();
    final MetricSlice slice = MetricSlice
        .from(me.getId(), window.getStartMillis(), endTime, me.getFilters(), timeGranularity);

    final InputData data = dataFetcher.fetchData(new InputDataSpec()
        .withTimeseriesSlices(singletonList(slice))
        .withMetricIdsForDataset(singletonList(me.getId()))
    );

    final DatasetConfigDTO datasetConfig = data.getDatasetForMetricId().get(me.getId());
    monitoringGranularityPeriod = DetectionUtils.getMonitoringGranularityPeriod(spec.getMonitoringGranularity(),
        datasetConfig);

    // Hack. To be removed when deprecating v1 pipeline
    spec.setTimezone(datasetConfig.getTimezone());

    final DataFrame df = data.getTimeseries()
        .get(slice)
        .renameSeries(DataFrame.COL_VALUE, DataFrame.COL_CURRENT);

    return runDetectionOnSingleDataTable(window, df);
  }

  private DetectionResult runDetectionOnSingleDataTable(final Interval window, final DataFrame df) {

    // defaults
    df.addSeries(COL_TOO_HIGH, BooleanSeries.fillValues(df.size(), false));
    df.addSeries(COL_TOO_LOW, BooleanSeries.fillValues(df.size(), false));

    // max
    if (!Double.isNaN(spec.getMax())) {
      df.addSeries(COL_TOO_HIGH, df.getDoubles(DataFrame.COL_CURRENT).gt(spec.getMax()));
    }

    // min
    if (!Double.isNaN(spec.getMin())) {
      df.addSeries(COL_TOO_LOW, df.getDoubles(DataFrame.COL_CURRENT).lt(spec.getMin()));
    }
    df.mapInPlace(BooleanSeries.HAS_TRUE, COL_ANOMALY, COL_TOO_HIGH, COL_TOO_LOW);

    final MetricSlice slice = MetricSlice
        .from(-1, window.getStartMillis(), window.getEndMillis(), null,
            timeGranularity);

    addBaselineAndBoundaries(df);

    final List<MergedAnomalyResultDTO> anomalies = DetectionUtils.buildAnomalies(slice,
        df,
        COL_ANOMALY,
        spec.getTimezone(),
        monitoringGranularityPeriod);

    return DetectionResult.from(anomalies, TimeSeries.fromDataFrame(df));
  }

  @Override
  public TimeSeries computePredictedTimeSeries(final MetricSlice slice) {
    final InputData data = dataFetcher.fetchData(new InputDataSpec()
        .withTimeseriesSlices(singletonList(slice)));
    final DataFrame df = data.getTimeseries().get(slice);
    addBaselineAndBoundaries(df);
    return TimeSeries.fromDataFrame(df);
  }

  /**
   * Populate the dataframe with upper/lower boundaries and baseline
   */
  private void addBaselineAndBoundaries(final DataFrame df) {
    // Set default baseline as the actual value
    df.addSeries(DataFrame.COL_VALUE, df.get(DataFrame.COL_CURRENT));
    if (!Double.isNaN(spec.getMin())) {
      df.addSeries(DataFrame.COL_LOWER_BOUND, DoubleSeries.fillValues(df.size(), spec.getMin()));
      // set baseline value as the lower bound when actual value across below the mark
      df.mapInPlace(DoubleSeries.MAX, DataFrame.COL_VALUE, DataFrame.COL_LOWER_BOUND,
          DataFrame.COL_VALUE);
    }
    if (!Double.isNaN(spec.getMax())) {
      df.addSeries(DataFrame.COL_UPPER_BOUND, DoubleSeries.fillValues(df.size(), spec.getMax()));
      // set baseline value as the upper bound when actual value across above the mark
      df.mapInPlace(DoubleSeries.MIN, DataFrame.COL_VALUE, DataFrame.COL_UPPER_BOUND,
          DataFrame.COL_VALUE);
    }
  }
}
