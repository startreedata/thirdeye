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
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_ANOMALY;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_CURRENT;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_ERROR;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_LOWER_BOUND;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_TIME;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_UPPER_BOUND;
import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_VALUE;

import java.util.Map;
import org.apache.pinot.thirdeye.detection.components.SimpleAnomalyDetectorV2Result;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Result;
import org.apache.pinot.thirdeye.spi.detection.BaselineProvider;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.DetectorException;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.ReadableInterval;

/**
 * Simple threshold rule algorithm with (optional) upper and lower bounds on a metric value.
 */
public class ThresholdRuleDetector implements AnomalyDetectorV2<ThresholdRuleDetectorSpec>,
    BaselineProvider<ThresholdRuleDetectorSpec> {

  private static final String COL_TOO_HIGH = "tooHigh";
  private static final String COL_TOO_LOW = "tooLow";

  private ThresholdRuleDetectorSpec spec;
  private Period monitoringGranularityPeriod;

  @Override
  public void init(final ThresholdRuleDetectorSpec spec) {
    this.spec = spec;

    // todo cyril refactor this
    final String monitoringGranularity = spec.getMonitoringGranularity();
  }

  @Override
  public AnomalyDetectorV2Result runDetection(final Interval interval,
      final Map<String, DataTable> timeSeriesMap
  ) throws DetectorException {
    setMonitoringGranularityPeriod();
    final DataTable current = requireNonNull(timeSeriesMap.get(KEY_CURRENT), "current is null");
    final DataFrame currentDf = current.getDataFrame();
    currentDf
        .renameSeries(spec.getTimestamp(), COL_TIME)
        .renameSeries(spec.getMetric(), COL_VALUE)
        .setIndex(COL_TIME);

    return runDetectionOnSingleDataTable(currentDf, interval);
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

  private AnomalyDetectorV2Result runDetectionOnSingleDataTable(final DataFrame inputDf,
      final ReadableInterval window) {
    DataFrame baselineDf = computeBaseline(inputDf);
    inputDf
        .renameSeries(COL_VALUE, COL_CURRENT)
        // left join baseline values
        .addSeries(baselineDf, COL_VALUE, COL_ERROR, COL_LOWER_BOUND, COL_UPPER_BOUND)
        .addSeries(COL_TOO_HIGH, valueTooHigh(inputDf.getDoubles(COL_CURRENT)))
        .addSeries(COL_TOO_LOW, valueTooLow(inputDf.getDoubles(COL_CURRENT)))
        .mapInPlace(BooleanSeries.HAS_TRUE, COL_ANOMALY, COL_TOO_HIGH, COL_TOO_LOW);

    return new SimpleAnomalyDetectorV2Result(inputDf,
        spec.getTimezone(),
        monitoringGranularityPeriod);
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
    } else {
      resultDF.addSeries(COL_LOWER_BOUND, DoubleSeries.nulls(resultDF.size()));
    }
    if (!Double.isNaN(spec.getMax())) {
      resultDF.addSeries(COL_UPPER_BOUND, DoubleSeries.fillValues(resultDF.size(), spec.getMax()));
      // set baseline value as the upper bound when actual value across above the mark
      resultDF.mapInPlace(DoubleSeries.MIN, COL_VALUE, COL_UPPER_BOUND, COL_VALUE);
    } else {
      resultDF.addSeries(COL_UPPER_BOUND, DoubleSeries.nulls(resultDF.size()));
    }
    return resultDF;
  }
}
