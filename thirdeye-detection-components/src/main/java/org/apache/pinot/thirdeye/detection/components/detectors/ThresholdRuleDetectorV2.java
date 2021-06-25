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
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.DetectorException;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.joda.time.Interval;

/**
 * Simple threshold rule algorithm with (optional) upper and lower bounds on a metric value.
 */
public class ThresholdRuleDetectorV2 implements AnomalyDetectorV2<ThresholdRuleDetectorSpec> {

  private final String COL_TOO_HIGH = "tooHigh";
  private final String COL_TOO_LOW = "tooLow";
  private final String COL_ANOMALY = "anomaly";

  private double min;
  private double max;
  private String monitoringGranularity;
  private TimeGranularity timeGranularity;
  private String timestamp;
  private String metric;

  @Override
  public void init(ThresholdRuleDetectorSpec spec) {
    this.min = spec.getMin();
    this.max = spec.getMax();
    this.timestamp = "ts";
    this.metric = "met";
    this.monitoringGranularity = spec.getMonitoringGranularity();
    if (this.monitoringGranularity.equals("1_MONTHS")) {
      this.timeGranularity = MetricSlice.NATIVE_GRANULARITY;
    } else {
      this.timeGranularity = TimeGranularity.fromString(spec.getMonitoringGranularity());
    }
  }

  @Override
  public DetectionPipelineResult runDetection(final Interval interval,
      final Map<String, DataTable> timeSeriesMap) throws DetectorException {
    final DataTable current = requireNonNull(timeSeriesMap.get(KEY_CURRENT), "current is null");
    final Map<DimensionInfo, DataTable> currentDataTableMap = DataTableUtils.splitDataTable(
        current);
    List<DetectionResult> detectionResults = new ArrayList<>();
    for (DimensionInfo dimensionInfo : currentDataTableMap.keySet()) {
      final DetectionResult detectionResult = runDetectionOnSingleDataTable(interval,
          currentDataTableMap.get(dimensionInfo));

      detectionResults.add(detectionResult);
    }
    return new GroupedDetectionResults(detectionResults);
  }

  private DetectionResult runDetectionOnSingleDataTable(final Interval window,
      final DataTable current) {
    final DataFrame df = current.getDataFrame();

    df.addSeries(DataFrame.COL_TIME, df.get(this.timestamp));
    df.addSeries(DataFrame.COL_CURRENT, df.get(this.metric));

    // defaults
    df.addSeries(COL_TOO_HIGH, BooleanSeries.fillValues(df.size(), false));
    df.addSeries(COL_TOO_LOW, BooleanSeries.fillValues(df.size(), false));

    // max
    if (!Double.isNaN(this.max)) {
      df.addSeries(COL_TOO_HIGH, df.getDoubles(DataFrame.COL_CURRENT).gt(this.max));
    }

    // min
    if (!Double.isNaN(this.min)) {
      df.addSeries(COL_TOO_LOW, df.getDoubles(DataFrame.COL_CURRENT).lt(this.min));
    }
    df.mapInPlace(BooleanSeries.HAS_TRUE, COL_ANOMALY, COL_TOO_HIGH, COL_TOO_LOW);

    MetricSlice slice = MetricSlice
        .from(-1, window.getStartMillis(), window.getEndMillis(), null,
            timeGranularity);

    List<MergedAnomalyResultDTO> anomalies = DetectionUtils.makeAnomalies(slice, df, COL_ANOMALY);
    DataFrame baselineWithBoundaries = constructBaselineAndBoundaries(df);
    final DetectionResult detectionResult = DetectionResult.from(anomalies,
        TimeSeries.fromDataFrame(baselineWithBoundaries));
    return detectionResult;
  }

  /**
   * Populate the dataframe with upper/lower boundaries and baseline
   */
  private DataFrame constructBaselineAndBoundaries(DataFrame df) {
    // Set default baseline as the actual value
    df.addSeries(DataFrame.COL_VALUE, df.get(DataFrame.COL_CURRENT));
    if (!Double.isNaN(this.min)) {
      df.addSeries(DataFrame.COL_LOWER_BOUND, DoubleSeries.fillValues(df.size(), this.min));
      // set baseline value as the lower bound when actual value across below the mark
      df.mapInPlace(DoubleSeries.MAX, DataFrame.COL_VALUE, DataFrame.COL_LOWER_BOUND,
          DataFrame.COL_VALUE);
    }
    if (!Double.isNaN(this.max)) {
      df.addSeries(DataFrame.COL_UPPER_BOUND, DoubleSeries.fillValues(df.size(), this.max));
      // set baseline value as the upper bound when actual value across above the mark
      df.mapInPlace(DoubleSeries.MIN, DataFrame.COL_VALUE, DataFrame.COL_UPPER_BOUND,
          DataFrame.COL_VALUE);
    }
    return df;
  }
}
