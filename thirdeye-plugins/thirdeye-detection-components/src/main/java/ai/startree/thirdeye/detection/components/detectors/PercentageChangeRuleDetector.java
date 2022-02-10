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

package ai.startree.thirdeye.detection.components.detectors;

import static ai.startree.thirdeye.detection.components.detectors.AbsoluteChangeRuleDetector.windowMatch;
import static ai.startree.thirdeye.detection.components.detectors.MeanVarianceRuleDetector.patternMatch;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_ANOMALY;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_CURRENT;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_DIFF;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_DIFF_VIOLATION;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_IN_WINDOW;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_LOWER_BOUND;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_PATTERN;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_TIME;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_UPPER_BOUND;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_VALUE;
import static ai.startree.thirdeye.spi.dataframe.DoubleSeries.POSITIVE_INFINITY;
import static ai.startree.thirdeye.spi.dataframe.Series.DoubleFunction;
import static ai.startree.thirdeye.spi.dataframe.Series.map;
import static ai.startree.thirdeye.spi.detection.Pattern.DOWN;
import static ai.startree.thirdeye.spi.detection.Pattern.UP;
import static ai.startree.thirdeye.spi.detection.Pattern.UP_OR_DOWN;
import static ai.startree.thirdeye.spi.detection.Pattern.valueOf;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detection.components.SimpleAnomalyDetectorResult;
import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.detection.AnomalyDetector;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorResult;
import ai.startree.thirdeye.spi.detection.BaselineProvider;
import ai.startree.thirdeye.spi.detection.DetectorException;
import ai.startree.thirdeye.spi.detection.Pattern;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import java.util.Map;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;

/**
 * Computes a multi-week aggregate baseline and compares the current value based on relative change.
 */
public class PercentageChangeRuleDetector implements
    AnomalyDetector<PercentageChangeRuleDetectorSpec>,
    BaselineProvider<PercentageChangeRuleDetectorSpec> {

  private double percentageChange;
  private Pattern pattern;
  private PercentageChangeRuleDetectorSpec spec;

  @Override
  public void init(final PercentageChangeRuleDetectorSpec spec) {
    this.spec = spec;
    checkArgument(!Double.isNaN(spec.getPercentageChange()), "Percentage change is not set.");
    percentageChange = spec.getPercentageChange();
    pattern = valueOf(spec.getPattern().toUpperCase());
  }

  @Override
  public AnomalyDetectorResult runDetection(final Interval window,
      final Map<String, DataTable> timeSeriesMap) throws DetectorException {
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

  private AnomalyDetectorResult runDetectionOnSingleDataTable(final DataFrame inputDf,
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
        new SimpleAnomalyDetectorResult(inputDf);
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
