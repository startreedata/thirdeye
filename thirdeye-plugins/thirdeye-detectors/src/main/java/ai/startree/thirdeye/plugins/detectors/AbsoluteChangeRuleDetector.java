/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.plugins.detectors;

import static ai.startree.thirdeye.spi.Constants.COL_ANOMALY;
import static ai.startree.thirdeye.spi.Constants.COL_CURRENT;
import static ai.startree.thirdeye.spi.Constants.COL_DIFF;
import static ai.startree.thirdeye.spi.Constants.COL_DIFF_VIOLATION;
import static ai.startree.thirdeye.spi.Constants.COL_IN_WINDOW;
import static ai.startree.thirdeye.spi.Constants.COL_LOWER_BOUND;
import static ai.startree.thirdeye.spi.Constants.COL_PATTERN;
import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.Constants.COL_UPPER_BOUND;
import static ai.startree.thirdeye.spi.Constants.COL_VALUE;
import static ai.startree.thirdeye.spi.dataframe.DoubleSeries.POSITIVE_INFINITY;
import static ai.startree.thirdeye.spi.detection.Pattern.DOWN;
import static ai.startree.thirdeye.spi.detection.Pattern.UP;
import static ai.startree.thirdeye.spi.detection.Pattern.UP_OR_DOWN;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.Series.LongConditional;
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
 * Absolute change rule detection
 */
public class AbsoluteChangeRuleDetector implements
    AnomalyDetector<AbsoluteChangeRuleDetectorSpec>,
    BaselineProvider<AbsoluteChangeRuleDetectorSpec> {

  private double absoluteChange;
  private Pattern pattern;
  private AbsoluteChangeRuleDetectorSpec spec;

  public static BooleanSeries windowMatch(LongSeries times, ReadableInterval window) {
    // only check start for consistency with other detectors
    return times.map((LongConditional) values -> values[0] >= window.getStartMillis());
  }

  @Override
  public void init(final AbsoluteChangeRuleDetectorSpec spec) {
    this.spec = spec;
    checkArgument(!Double.isNaN(spec.getAbsoluteChange()), "Absolute change is not set.");
    absoluteChange = spec.getAbsoluteChange();
    pattern = Pattern.valueOf(spec.getPattern().toUpperCase());
  }

  @Override
  public AnomalyDetectorResult runDetection(final Interval window,
      final Map<String, DataTable> dataTableMap) throws DetectorException {
    final DataTable baseline = requireNonNull(dataTableMap.get(KEY_BASELINE), "baseline is null");
    final DataTable current = requireNonNull(dataTableMap.get(KEY_CURRENT), "current is null");
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
    // calculate absolute change
    inputDf
        .addSeries(COL_DIFF, inputDf.getDoubles(COL_CURRENT).subtract(inputDf.get(COL_VALUE)))
        .addSeries(COL_PATTERN, MeanVarianceRuleDetector.patternMatch(pattern, inputDf))
        .addSeries(COL_DIFF_VIOLATION, inputDf.getDoubles(COL_DIFF).abs().gte(absoluteChange))
        .addSeries(COL_IN_WINDOW, windowMatch(inputDf.getLongs(COL_TIME), window))
        .mapInPlace(BooleanSeries.ALL_TRUE, COL_ANOMALY,
            COL_PATTERN,
            COL_DIFF_VIOLATION,
            COL_IN_WINDOW);
    addBoundaries(inputDf);

    return
        new SimpleAnomalyDetectorResult(inputDf);
  }

  private void addBoundaries(final DataFrame inputDf) {
    //default bounds
    DoubleSeries upperBound = DoubleSeries.fillValues(inputDf.size(), POSITIVE_INFINITY);
    //fixme cyril this not consistent with threshold rule detector default values
    DoubleSeries lowerBound = DoubleSeries.zeros(inputDf.size());
    if (pattern == UP || pattern == UP_OR_DOWN) {
      upperBound = inputDf.getDoubles(COL_VALUE).add(absoluteChange);
    }
    if (pattern == DOWN || pattern == UP_OR_DOWN) {
      lowerBound = inputDf.getDoubles(COL_VALUE).add(-absoluteChange);
    }
    inputDf.addSeries(COL_UPPER_BOUND, upperBound);
    inputDf.addSeries(COL_LOWER_BOUND, lowerBound);
  }
}
