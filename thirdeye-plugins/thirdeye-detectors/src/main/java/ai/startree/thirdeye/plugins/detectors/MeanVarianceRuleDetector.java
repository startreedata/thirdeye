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
import static ai.startree.thirdeye.spi.Constants.COL_MASK;
import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.Constants.COL_UPPER_BOUND;
import static ai.startree.thirdeye.spi.Constants.COL_VALUE;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.detection.AnomalyDetector;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorResult;
import ai.startree.thirdeye.spi.detection.Pattern;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.ReadableInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * History mean and standard deviation based forecasting and detection.
 * Forecast using history mean and standard deviation.
 */
public class MeanVarianceRuleDetector implements AnomalyDetector<MeanVarianceRuleDetectorSpec> {

  private static final Logger LOG = LoggerFactory.getLogger(MeanVarianceRuleDetector.class);
  private static final Set<Period> SUPPORTED_SEASONALITIES = Set.of(
      Period.days(7), // weekly seasonality
      Period.days(1), // daily seasonality
      Period.ZERO     // special value for no seasonality
  );

  private Pattern pattern;
  private double lowerSensitivity;
  private double upperSensitivity;
  private double lowerBoundMultiplier;
  private double upperBoundMultiplier;
  private int lookback;
  private MeanVarianceRuleDetectorSpec spec;
  private Period seasonality = Period.ZERO; // PT0S: special period for no seasonality

  private double metricMaximumValue;
  private double metricMinimumValue;

  /**
   * Mapping of sensitivity to sigma on range of 0.5 - 1.5
   * 10 corresponds to sigma of 0.5
   * 0 corresponds to sigma of 1.5
   * Sensitivity value has no bounds, but it is recommended to be between 0 and 10.
   */
  private static double sigma(final double sensitivity) {
    return 0.5 + 0.1 * (10 - sensitivity);
  }

  @VisibleForTesting
  protected static int computeSteps(final String periodString,
      final String monitoringGranularityString) {
    // mind that computing lookback only once is not exactly correct when a day has 25 hours or 23 hours - but very minor issue
    final Period lookbackPeriod = isoPeriod(periodString);
    final Period monitoringGranularity = isoPeriod(monitoringGranularityString);

    return (int) (lookbackPeriod.toStandardDuration().getMillis()
        / monitoringGranularity.toStandardDuration().getMillis());
  }

  @Override
  public void init(final MeanVarianceRuleDetectorSpec spec) {
    this.spec = spec;
    this.pattern = spec.getPattern();
    if (spec.getLowerSensitivity() != null || spec.getUpperSensitivity() != null) {
      checkArgument(spec.getLowerSensitivity() != null, "lowerSensitivity is null. lowerSensitivity must be set when upperSensitivity is set.");
      checkArgument(spec.getUpperSensitivity() != null, "upperSensitivity is null. upperSensitivity must be set when lowerSensitivity is set.");
      this.lowerSensitivity = spec.getLowerSensitivity();
      this.upperSensitivity = spec.getUpperSensitivity();
    } else {
      this.lowerSensitivity = spec.getSensitivity();
      this.upperSensitivity = spec.getSensitivity();
    }
    this.metricMinimumValue = optional(spec.getMetricMinimumValue()).orElse(Double.NEGATIVE_INFINITY);
    this.metricMaximumValue = optional(spec.getMetricMaximumValue()).orElse(Double.POSITIVE_INFINITY);
    this.lowerBoundMultiplier = optional(spec.getLowerBoundMultiplier()).orElse(1.);
    this.upperBoundMultiplier = optional(spec.getUpperBoundMultiplier()).orElse(1.);

    if (spec.getLookbackPeriod() != null) {
      checkArgument(spec.getMonitoringGranularity() != null,
          "monitoringGranularity is required when lookbackPeriod is used");
      this.lookback = computeSteps(spec.getLookbackPeriod(), spec.getMonitoringGranularity());
    } else {
      // fixme cyril remove deprecated lookback in spec and only use lookbackPeriod in 2 months (mid-May)
      // use default or set lookback - not recommended
      lookback = spec.getLookback();
    }

    if (spec.getSeasonalityPeriod() != null) {
      checkArgument(spec.getMonitoringGranularity() != null,
          "monitoringGranularity is required when seasonalityPeriod is used");
      final Period seasonality = isoPeriod(spec.getSeasonalityPeriod());
      checkArgument(SUPPORTED_SEASONALITIES.contains(seasonality),
          "Unsupported period %s. Supported periods are P7D and P1D, or PTOS for no seasonality.",
          seasonality);
      int minimumLookbackRequired =
          2 * computeSteps(spec.getSeasonalityPeriod(), spec.getMonitoringGranularity());
      checkArgument(minimumLookbackRequired <= lookback,
          String.format(
              "Not enough history to compute variance with seasonality. Lookback: %s. LookbackPeriod: %s, SeasonalityPeriod: %s. Minimum lookback steps required: %s",
              lookback,
              spec.getLookbackPeriod(),
              spec.getSeasonalityPeriod(),
              minimumLookbackRequired));

      this.seasonality = seasonality;
    }

    checkArgument(lookback >= 5, "Lookback is %s points. Lookback should be greater than 5 points.",
        lookback);
  }

  @Override
  public AnomalyDetectorResult runDetection(final Interval window,
      final Map<String, DataTable> dataTableMap
  ) {
    final DataTable current = requireNonNull(dataTableMap.get(KEY_CURRENT), "current is null");
    final DataFrame currentDf = current.getDataFrame().copy();
    currentDf
        .renameSeries(spec.getTimestamp(), COL_TIME)
        .renameSeries(spec.getMetric(), COL_VALUE)
        .setIndex(COL_TIME);

    return runDetectionOnSingleDataTable(currentDf, window);
  }

  private AnomalyDetectorResult runDetectionOnSingleDataTable(final DataFrame inputDf,
      final ReadableInterval window) {
    final DataFrame baselineDf = computeBaseline(inputDf, window);
    inputDf
        // rename current which is still called "value" to "current"
        .renameSeries(COL_VALUE, COL_CURRENT)
        // left join baseline values
        .addSeries(baselineDf, COL_VALUE, COL_LOWER_BOUND, COL_UPPER_BOUND)
        .addSeries(COL_ANOMALY,
            pattern.isAnomaly(inputDf.getDoubles(COL_CURRENT), inputDf.getDoubles(COL_LOWER_BOUND),
                    inputDf.getDoubles(COL_UPPER_BOUND))
                .and(windowMatch(inputDf.getLongs(COL_TIME), window)));

    return new SimpleAnomalyDetectorResult(inputDf);
  }

  private DataFrame computeBaseline(final DataFrame inputDF, final ReadableInterval detectionInterval) {
    final DataFrame resultDF = new DataFrame();
    final int size = inputDF.size();
    final double[] baselineArray = new double[size];
    Arrays.fill(baselineArray, DoubleSeries.NULL);
    final double[] upperBoundArray = new double[size];
    Arrays.fill(upperBoundArray, DoubleSeries.NULL);
    final double[] lowerBoundArray = new double[size];
    Arrays.fill(lowerBoundArray, DoubleSeries.NULL);

    final LongSeries inputTimes = inputDF.getLongs(COL_TIME);
    final int firstDetectionIndex;
    if (detectionInterval.toDurationMillis() <= 0) {
      // the detection interval is empty - set the firstDetectionIndex such that the loop is not entered, but still build the df of historical data - some consumers in ThirdEye use empty detection interval
      firstDetectionIndex = size;
    } else {
      firstDetectionIndex = inputTimes.find(detectionInterval.getStartMillis());
      checkState(firstDetectionIndex != -1,
          "Runtime error. Could not build training data for the mean-variance algorithm. Expected to run detection on time: %s but this time was not found in the input times. Last 10 input times: %s",
          detectionInterval.getStartMillis(), inputTimes.sliceFrom(Math.max(0,inputTimes.size()-10)).toString());
    }

    // todo cyril compute mean and std in a single pass
    // https://nestedsoftware.com/2018/03/20/calculating-a-moving-average-on-streaming-data-5a7k.22879.html
    // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm
    final boolean applyMask = inputDF.contains(COL_MASK);
    for (int k = firstDetectionIndex; k < size; k++) {
      if (applyMask && BooleanSeries.isTrue(inputDF.getBoolean(COL_MASK, k))) {
        // this point is masked - skip it
        continue;
      }
      final long forecastTime = inputTimes.getLong(k);
      final DataFrame lookbackDf = getLookbackDf(inputDF, forecastTime);
      final DoubleSeries periodMask = buildPeriodMask(lookbackDf, forecastTime);
      DoubleSeries maskedValues = lookbackDf.getDoubles(COL_VALUE).multiply(periodMask);
      if (applyMask) {
        // todo cyril perf - COL_MASK.fillNull().not() can be computed once outside of the loop
        maskedValues = maskedValues.filter(lookbackDf.getBooleans(COL_MASK).fillNull().not());
      }
      double mean = maskedValues.mean().value();
      double std = maskedValues.std().value();
      if (Double.isNaN(mean)) {
        // mean and std can be null if all values are masked or null
        mean = 0.0;
        std = 0.0;
      }
      //calculate baseline, error , upper and lower bound for prediction window.
      baselineArray[k] = bounded(mean);
      final double upperError = sigma(upperSensitivity) * std;
      final double lowerError = sigma(lowerSensitivity) * std;
      upperBoundArray[k] = bounded((baselineArray[k] + upperError) * upperBoundMultiplier);
      lowerBoundArray[k] = bounded((baselineArray[k] - lowerError) * lowerBoundMultiplier);
    }
    //Construct the dataframe.
    resultDF
        .addSeries(COL_TIME, inputDF.getLongs(COL_TIME))
        .setIndex(COL_TIME);

    resultDF
        .addSeries(COL_VALUE, DoubleSeries.buildFrom(baselineArray))
        .addSeries(COL_UPPER_BOUND, DoubleSeries.buildFrom(upperBoundArray))
        .addSeries(COL_LOWER_BOUND, DoubleSeries.buildFrom(lowerBoundArray));

    return resultDF;
  }

  private DoubleSeries buildPeriodMask(final DataFrame lookbackDf, final long forecastTime) {
    if (seasonality.equals(Period.ZERO)) {
      // no seasonality --> no mask
      return DoubleSeries.fillValues(lookbackDf.size(), 1);
    }
    // fixme cyril this implem does not not fail at DST - but datetimeZone is hardcoded to UTC so not DST
    DateTime forecastDateTime = new DateTime(forecastTime, DateTimeZone.UTC);
    DoubleSeries.Builder mask = DoubleSeries.builder();
    LongSeries lookbackEpochs = lookbackDf.get(COL_TIME).getLongs();
    for (int idx = 0; idx < lookbackEpochs.size(); idx++) {
      DateTime lookbackDateTime = new DateTime(lookbackEpochs.get(idx), DateTimeZone.UTC);
      mask.addValues(isSeasonalityMatch(forecastDateTime, lookbackDateTime) ? 1. : null);
    }
    return mask.build();
  }

  private boolean isSeasonalityMatch(final DateTime dt1, final DateTime dt2) {
    final boolean isSameTimeInDay =
        dt1.getHourOfDay() == dt2.getHourOfDay() && dt1.getMinuteOfHour() == dt2.getMinuteOfHour()
            && dt1.getSecondOfMinute() == dt2.getSecondOfMinute()
            && dt1.getMillisOfSecond() == dt2.getMillisOfSecond();
    if (seasonality.equals(Period.days(7))) {
      return isSameTimeInDay && dt1.getDayOfWeek() == dt2.getDayOfWeek();
    } else if (seasonality.equals(Period.days(1))) {
      return isSameTimeInDay;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private DataFrame getLookbackDf(final DataFrame inputDF, final long endTimeMillis) {
    final int indexEnd = inputDF.getLongs(COL_TIME).find(endTimeMillis);
    checkArgument(indexEnd != -1,
        "Could not find index of endTime %s. endTime should exist in inputDf. This should not happen.", endTimeMillis);
    final int indexStart = indexEnd - lookback;
    checkArgument(indexStart >= 0,
        "Invalid index. Insufficient data to compute mean/variance on lookback. index: "
            + indexStart);
    return inputDF.slice(indexStart, indexEnd);
  }

  private double bounded(final double val) {
    return Math.min(metricMaximumValue, Math.max(val, metricMinimumValue));
  }
}
