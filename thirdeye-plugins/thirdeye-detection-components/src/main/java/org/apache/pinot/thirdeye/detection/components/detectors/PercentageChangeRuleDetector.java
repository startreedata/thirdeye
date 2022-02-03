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
import static org.apache.pinot.thirdeye.spi.detection.Pattern.DOWN;
import static org.apache.pinot.thirdeye.spi.detection.Pattern.UP;
import static org.apache.pinot.thirdeye.spi.detection.Pattern.UP_OR_DOWN;
import static org.apache.pinot.thirdeye.spi.detection.Pattern.valueOf;

import java.util.Map;
import org.apache.pinot.thirdeye.detection.components.SimpleAnomalyDetectorV2Result;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.Series;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Result;
import org.apache.pinot.thirdeye.spi.detection.BaselineParsingUtils;
import org.apache.pinot.thirdeye.spi.detection.BaselineProvider;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.DetectorException;
import org.apache.pinot.thirdeye.spi.detection.Pattern;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.rootcause.timeseries.Baseline;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.ReadableInterval;

/**
 * Computes a multi-week aggregate baseline and compares the current value based on relative change.
 */
public class PercentageChangeRuleDetector implements
    AnomalyDetectorV2<PercentageChangeRuleDetectorSpec>,
    BaselineProvider<PercentageChangeRuleDetectorSpec> {

  private double percentageChange;
  private Baseline baseline;
  private Pattern pattern;
  // todo cyril refactor this
  private String monitoringGranularity;
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
