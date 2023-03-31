/*
 * Copyright 2023 StarTree Inc
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

import static ai.startree.thirdeye.plugins.detectors.AbsoluteChangeRuleDetector.windowMatch;
import static ai.startree.thirdeye.spi.Constants.COL_ANOMALY;
import static ai.startree.thirdeye.spi.Constants.COL_CURRENT;
import static ai.startree.thirdeye.spi.Constants.COL_LOWER_BOUND;
import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.Constants.COL_UPPER_BOUND;
import static ai.startree.thirdeye.spi.Constants.COL_VALUE;
import static ai.startree.thirdeye.spi.detection.Pattern.valueOf;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.detection.AnomalyDetector;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorResult;
import ai.startree.thirdeye.spi.detection.Pattern;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import java.util.Map;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;

/**
 * Computes the current value to a baseline value.
 * The lower/upper bounds are computed with: [baseline * (1-percentage), baseline * (1 + percentage)].
 * If a value is outside of this range, it is detected as an anomaly.
 */
public class PercentageChangeRuleDetector implements
    AnomalyDetector<PercentageChangeRuleDetectorSpec> {

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
      final Map<String, DataTable> dataTableMap) {
    final DataTable baseline = requireNonNull(dataTableMap.get(KEY_BASELINE), "baseline is null");
    final DataTable current = requireNonNull(dataTableMap.get(KEY_CURRENT), "current is null");
    final DataFrame baselineDf = baseline.getDataFrame();
    final DataFrame currentDf = current.getDataFrame().copy();
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
        .addSeries(COL_UPPER_BOUND, inputDf.getDoubles(COL_VALUE).multiply(1 + percentageChange))
        .addSeries(COL_LOWER_BOUND, inputDf.getDoubles(COL_VALUE).multiply(1 - percentageChange))
        .addSeries(COL_ANOMALY,
            pattern.isAnomaly(inputDf.getDoubles(COL_CURRENT), inputDf.getDoubles(COL_LOWER_BOUND),
                    inputDf.getDoubles(COL_UPPER_BOUND))
                .and(windowMatch(inputDf.getLongs(COL_TIME), window)));

    return new SimpleAnomalyDetectorResult(inputDf);
  }
}
