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

import static org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries.POSITIVE_INFINITY;
import static org.apache.pinot.thirdeye.spi.dataframe.Series.DoubleFunction;
import static org.apache.pinot.thirdeye.spi.dataframe.Series.map;

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
import org.apache.pinot.thirdeye.spi.detection.BaselineParsingUtils;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.DetectorException;
import org.apache.pinot.thirdeye.spi.detection.Pattern;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.joda.time.Interval;

public class PercentageChangeRuleDetectorV2 implements
    AnomalyDetectorV2<PercentageChangeRuleDetectorSpec> {

  private static final String COL_CHANGE = "change";
  private static final String COL_ANOMALY = "anomaly";
  private static final String COL_PATTERN = "pattern";
  private static final String COL_CHANGE_VIOLATION = "change_violation";
  private double percentageChange;
  private Pattern pattern;
  private TimeGranularity timeGranularity;
  private String timestamp = "timestamp";
  private String metric = "value";

  @Override
  public void init(PercentageChangeRuleDetectorSpec spec) {
    this.percentageChange = spec.getPercentageChange();
    String timezone = spec.getTimezone();
    String offset = spec.getOffset();
    this.timestamp = spec.getTimestamp();
    this.metric = spec.getMetric();

    BaselineParsingUtils.parseOffset(offset, timezone);
    this.pattern = Pattern.valueOf(spec.getPattern().toUpperCase());

    String monitoringGranularity = spec.getMonitoringGranularity();
    if (monitoringGranularity.endsWith(TimeGranularity.MONTHS) || monitoringGranularity
        .endsWith(TimeGranularity.WEEKS)) {
      this.timeGranularity = MetricSlice.NATIVE_GRANULARITY;
    } else {
      this.timeGranularity = TimeGranularity.fromString(spec.getMonitoringGranularity());
    }
  }

  @Override
  public DetectionPipelineResult runDetection(final Interval window,
      final Map<String, DataTable> timeSeriesMap) throws DetectorException {
    DataTable baseline = timeSeriesMap.get(KEY_BASELINE);
    DataTable current = timeSeriesMap.get(KEY_CURRENT);
    final Map<DimensionInfo, DataTable> baselineDataTableMap = DataTableUtils.splitDataTable(
        baseline);
    final Map<DimensionInfo, DataTable> currentDataTableMap = DataTableUtils.splitDataTable(
        current);
    List<DetectionResult> detectionResults = new ArrayList<>();
    for (DimensionInfo dimensionInfo : baselineDataTableMap.keySet()) {
      final DetectionResult detectionResult = runDetectionOnSingleDataTable(window,
          baselineDataTableMap.get(dimensionInfo),
          currentDataTableMap.get(dimensionInfo));

      detectionResults.add(detectionResult);
    }
    return new GroupedDetectionResults(detectionResults);
  }

  private DetectionResult runDetectionOnSingleDataTable(final Interval window,
      final DataTable baseline, final DataTable current) {
    final DataFrame currentDf = current.getDataFrame();
    final DataFrame baselineDf = baseline.getDataFrame();
    DataFrame df = new DataFrame();
    df.addSeries(DataFrame.COL_TIME, currentDf.get(this.timestamp));
    df.addSeries(DataFrame.COL_CURRENT, currentDf.get(this.metric));
    df.addSeries(DataFrame.COL_VALUE, baselineDf.get(this.metric));
    // calculate percentage change
    df.addSeries(COL_CHANGE, map((DoubleFunction) values -> {
      if (Double.compare(values[1], 0.0) == 0) {
        return Double.compare(values[0], 0.0) == 0 ? 0.0
            : (values[0] > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
      }
      return (values[0] - values[1]) / values[1];
    }, df.getDoubles(DataFrame.COL_CURRENT), df.get(DataFrame.COL_VALUE)));

    // defaults
    df.addSeries(COL_ANOMALY, BooleanSeries.fillValues(df.size(), false));

    // relative change
    if (!Double.isNaN(this.percentageChange)) {
      // consistent with pattern
      if (pattern.equals(Pattern.UP_OR_DOWN)) {
        df.addSeries(COL_PATTERN, BooleanSeries.fillValues(df.size(), true));
      } else {
        df.addSeries(COL_PATTERN,
            this.pattern.equals(Pattern.UP) ? df.getDoubles(COL_CHANGE).gt(0)
                : df.getDoubles(COL_CHANGE).lt(0));
      }
      df.addSeries(COL_CHANGE_VIOLATION,
          df.getDoubles(COL_CHANGE).abs().gte(this.percentageChange));
      df.mapInPlace(BooleanSeries.ALL_TRUE, COL_ANOMALY, COL_PATTERN, COL_CHANGE_VIOLATION);
    }

    MetricSlice slice = MetricSlice
        .from(-1, window.getStartMillis(), window.getEndMillis(), null,
            timeGranularity);

    List<MergedAnomalyResultDTO> anomalies = DetectionUtils.makeAnomalies(slice, df, COL_ANOMALY);
    DataFrame baselineWithBoundaries = constructPercentageChangeBoundaries(df);
    return DetectionResult.from(anomalies, TimeSeries.fromDataFrame(baselineWithBoundaries));
  }

  private DataFrame constructPercentageChangeBoundaries(DataFrame dfBase) {
    if (!Double.isNaN(this.percentageChange)) {
      switch (this.pattern) {
        case UP:
          fillPercentageChangeBound(dfBase, DataFrame.COL_UPPER_BOUND, 1 + this.percentageChange);
          dfBase.addSeries(DataFrame.COL_LOWER_BOUND, DoubleSeries.zeros(dfBase.size()));
          break;
        case DOWN:
          dfBase.addSeries(
              DataFrame.COL_UPPER_BOUND, DoubleSeries.fillValues(dfBase.size(), POSITIVE_INFINITY));
          fillPercentageChangeBound(dfBase, DataFrame.COL_LOWER_BOUND, 1 - this.percentageChange);
          break;
        case UP_OR_DOWN:
          fillPercentageChangeBound(dfBase, DataFrame.COL_UPPER_BOUND, 1 + this.percentageChange);
          fillPercentageChangeBound(dfBase, DataFrame.COL_LOWER_BOUND, 1 - this.percentageChange);
          break;
        default:
          throw new IllegalArgumentException();
      }
    }
    return dfBase;
  }

  private void fillPercentageChangeBound(DataFrame dfBase, String colBound, double multiplier) {
    dfBase.addSeries(colBound,
        map((DoubleFunction) values -> values[0] * multiplier, dfBase.getDoubles(
            DataFrame.COL_VALUE)));
  }
}
