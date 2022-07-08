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
import static ai.startree.thirdeye.spi.Constants.COL_ERROR;
import static ai.startree.thirdeye.spi.Constants.COL_LOWER_BOUND;
import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.Constants.COL_UPPER_BOUND;
import static ai.startree.thirdeye.spi.Constants.COL_VALUE;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.detection.AnomalyDetector;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorResult;
import ai.startree.thirdeye.spi.detection.BaselineProvider;
import ai.startree.thirdeye.spi.detection.DetectorException;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import java.util.Map;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;

/**
 * Simple threshold rule algorithm with (optional) upper and lower bounds on a metric value.
 */
public class ThresholdRuleDetector implements AnomalyDetector<ThresholdRuleDetectorSpec>,
    BaselineProvider<ThresholdRuleDetectorSpec> {

  private static final String COL_TOO_HIGH = "tooHigh";
  private static final String COL_TOO_LOW = "tooLow";

  private ThresholdRuleDetectorSpec spec;

  @Override
  public void init(final ThresholdRuleDetectorSpec spec) {
    this.spec = spec;
  }

  @Override
  public AnomalyDetectorResult runDetection(final Interval interval,
      final Map<String, DataTable> timeSeriesMap
  ) throws DetectorException {
    final DataTable current = requireNonNull(timeSeriesMap.get(KEY_CURRENT), "current is null");
    final DataFrame currentDf = current.getDataFrame();
    currentDf
        .renameSeries(spec.getTimestamp(), COL_TIME)
        .renameSeries(spec.getMetric(), COL_VALUE)
        .setIndex(COL_TIME);

    return runDetectionOnSingleDataTable(currentDf, interval);
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

  private AnomalyDetectorResult runDetectionOnSingleDataTable(final DataFrame inputDf,
      final ReadableInterval window) {
    DataFrame baselineDf = computeBaseline(inputDf);
    inputDf
        .renameSeries(COL_VALUE, COL_CURRENT)
        // left join baseline values
        .addSeries(baselineDf, COL_VALUE, COL_ERROR, COL_LOWER_BOUND, COL_UPPER_BOUND)
        .addSeries(COL_TOO_HIGH, valueTooHigh(inputDf.getDoubles(COL_CURRENT)))
        .addSeries(COL_TOO_LOW, valueTooLow(inputDf.getDoubles(COL_CURRENT)))
        .mapInPlace(BooleanSeries.HAS_TRUE, COL_ANOMALY, COL_TOO_HIGH, COL_TOO_LOW);

    return new SimpleAnomalyDetectorResult(inputDf);
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
