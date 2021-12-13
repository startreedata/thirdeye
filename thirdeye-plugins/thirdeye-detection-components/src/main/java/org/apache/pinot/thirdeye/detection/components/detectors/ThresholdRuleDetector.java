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
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_CURRENT;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_LOWER_BOUND;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_TIME;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_UPPER_BOUND;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_VALUE;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.components.detectors.results.DataTableUtils;
import org.apache.pinot.thirdeye.detection.components.detectors.results.DimensionInfo;
import org.apache.pinot.thirdeye.detection.components.detectors.results.GroupedDetectionResults;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
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
import org.joda.time.ReadableInterval;

/**
 * Simple threshold rule algorithm with (optional) upper and lower bounds on a metric value.
 */
public class ThresholdRuleDetector implements AnomalyDetector<ThresholdRuleDetectorSpec>,
    AnomalyDetectorV2<ThresholdRuleDetectorSpec>,
    BaselineProvider<ThresholdRuleDetectorSpec> {

  private static final String COL_TOO_HIGH = "tooHigh";
  private static final String COL_TOO_LOW = "tooLow";
  private static final String COL_ERROR = "error";

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
      // todo cyril this is translate to generic col names
      final DataFrame currentDf = currentDataTableMap.get(dimensionInfo).getDataFrame();
      currentDf
          .addSeries(COL_TIME, currentDf.get(spec.getTimestamp()))
          .setIndex(COL_TIME)
          .addSeries(COL_VALUE, currentDf.get(spec.getMetric()));

      final DetectionResult detectionResult = runDetectionOnSingleDataTable(currentDf, interval);
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

    final DataFrame df = data.getTimeseries().get(slice);

    return runDetectionOnSingleDataTable(df, window);
  }

  private BooleanSeries valueTooHigh(DoubleSeries values) {
    if (Double.isNaN(spec.getMax())) {
      return BooleanSeries.fillValues(values.size(), false);
    }
    return values.gt(spec.getMax());
  }

  private BooleanSeries valueTooLow(DoubleSeries values) {
    if (Double.isNaN(spec.getMin())) {
      return BooleanSeries.fillValues(values.size(), false);
    }
    return values.lt(spec.getMin());
  }

  private DetectionResult runDetectionOnSingleDataTable(final DataFrame inputDf,
      final ReadableInterval window) {
    DataFrame baselineDf = computeBaseline(inputDf);
    inputDf
        .renameSeries(COL_VALUE, COL_CURRENT)
        // left join baseline values
        .addSeries(baselineDf, COL_VALUE, COL_ERROR, COL_LOWER_BOUND, COL_UPPER_BOUND)
        .addSeries(COL_TOO_HIGH, valueTooHigh(inputDf.getDoubles(COL_CURRENT)))
        .addSeries(COL_TOO_LOW, valueTooLow(inputDf.getDoubles(COL_CURRENT)))
        .mapInPlace(BooleanSeries.HAS_TRUE, COL_ANOMALY, COL_TOO_HIGH, COL_TOO_LOW);

    return getDetectionResultTemp(inputDf, window);
  }

  private DetectionResult getDetectionResultTemp(final DataFrame inputDf,
      final ReadableInterval window) {
    final MetricSlice slice = MetricSlice.from(-1,
        window.getStartMillis(),
        window.getEndMillis(),
        ArrayListMultimap.create(),
        timeGranularity);

    final List<MergedAnomalyResultDTO> anomalies = DetectionUtils.buildAnomalies(slice,
        inputDf,
        COL_ANOMALY,
        spec.getTimezone(),
        monitoringGranularityPeriod);

    return DetectionResult.from(anomalies, TimeSeries.fromDataFrame(inputDf.sortedBy(COL_TIME)));
  }

  @Override
  public TimeSeries computePredictedTimeSeries(final MetricSlice slice) {
    final InputData data = dataFetcher.fetchData(new InputDataSpec()
        .withTimeseriesSlices(singletonList(slice)));
    final DataFrame df = data.getTimeseries().get(slice);
    final DataFrame baselineDf = computeBaseline(df);
    df
        .renameSeries(COL_VALUE, COL_CURRENT)
        .addSeries(baselineDf, COL_VALUE, COL_ERROR, COL_LOWER_BOUND, COL_UPPER_BOUND);

    return TimeSeries.fromDataFrame(df);
  }

  private DataFrame computeBaseline(final DataFrame inputDf) {
    final DataFrame resultDF = new DataFrame();
    resultDF
        .addSeries(COL_TIME, inputDf.getDoubles(COL_TIME)).setIndex(COL_TIME)
        .addSeries(COL_VALUE, inputDf.getDoubles(COL_VALUE))
        // error cannot be computed - added for consistency with other methods
        .addSeries(COL_ERROR, DoubleSeries.nulls(resultDF.size()));
    if (!Double.isNaN(spec.getMin())) {
      resultDF.addSeries(COL_LOWER_BOUND, DoubleSeries.fillValues(resultDF.size(), spec.getMin()));
      // set baseline value as the lower bound when actual value across below the mark
      resultDF.mapInPlace(DoubleSeries.MAX, COL_VALUE, COL_LOWER_BOUND, COL_VALUE);
    }
    if (!Double.isNaN(spec.getMax())) {
      resultDF.addSeries(COL_UPPER_BOUND, DoubleSeries.fillValues(resultDF.size(), spec.getMax()));
      // set baseline value as the upper bound when actual value across above the mark
      resultDF.mapInPlace(DoubleSeries.MIN, COL_VALUE, COL_UPPER_BOUND, COL_VALUE);
    }
    return resultDF;
  }
}
