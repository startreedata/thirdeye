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

import static ai.startree.thirdeye.spi.Constants.COL_ANOMALY;
import static ai.startree.thirdeye.spi.Constants.COL_CURRENT;
import static ai.startree.thirdeye.spi.Constants.COL_DIFF;
import static ai.startree.thirdeye.spi.Constants.COL_DIFF_VIOLATION;
import static ai.startree.thirdeye.spi.Constants.COL_ERROR;
import static ai.startree.thirdeye.spi.Constants.COL_LOWER_BOUND;
import static ai.startree.thirdeye.spi.Constants.COL_PATTERN;
import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.Constants.COL_UPPER_BOUND;
import static ai.startree.thirdeye.spi.Constants.COL_VALUE;
import static ai.startree.thirdeye.spi.dataframe.Series.LongConditional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.detection.AnomalyDetector;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorResult;
import ai.startree.thirdeye.spi.detection.BaselineProvider;
import ai.startree.thirdeye.spi.detection.DetectorException;
import ai.startree.thirdeye.spi.detection.Pattern;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
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
public class MeanVarianceRuleDetector implements AnomalyDetector<MeanVarianceRuleDetectorSpec>,
    BaselineProvider<MeanVarianceRuleDetectorSpec> {

  private static final Logger LOG = LoggerFactory.getLogger(MeanVarianceRuleDetector.class);
  private static final Set<Period> SUPPORTED_SEASONALITIES = Set.of(
      Period.days(7), // weekly seasonality
      Period.days(1), // daily seasonality
      Period.ZERO     // special value for no seasonality
  );

  private Pattern pattern;
  private double sensitivity;
  private int lookback;
  private MeanVarianceRuleDetectorSpec spec;
  private Period seasonality = Period.ZERO; // PT0S: special period for no seasonality

  /**
   * Mapping of sensitivity to sigma on range of 0.5 - 1.5
   * 10 corresponds to sigma of 0.5
   * 0 corresponds to sigma of 1.5
   * Sensitivity value has no bounds, but it is recommended to be between 0 and 10.
   */
  private static double sigma(final double sensitivity) {
    return 0.5 + 0.1 * (10 - sensitivity);
  }

  protected static int computeSteps(final String periodString,
      final String monitoringGranularityString) {
    // mind that computing lookback only once is not exactly correct when a day has 25 hours or 23 hours - but very minor issue
    final Period lookbackPeriod = isoPeriod(periodString);
    final Period monitoringGranularity = isoPeriod(monitoringGranularityString);

    return (int) (lookbackPeriod.toStandardDuration().getMillis()
        / monitoringGranularity.toStandardDuration().getMillis());
  }

  //todo cyril move this as utils/shared method
  public static BooleanSeries patternMatch(final Pattern pattern, final DataFrame dfInput) {
    // series of boolean that are true if the anomaly direction matches the pattern
    if (pattern.equals(Pattern.UP_OR_DOWN)) {
      return BooleanSeries.fillValues(dfInput.size(), true);
    }
    return pattern.equals(Pattern.UP) ?
        dfInput.getDoubles(COL_DIFF).gt(0) :
        dfInput.getDoubles(COL_DIFF).lt(0);
  }

  @Override
  public void init(final MeanVarianceRuleDetectorSpec spec) {
    this.spec = spec;
    pattern = spec.getPattern();
    sensitivity = spec.getSensitivity();

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
          String.format(
              "Unsupported period %s. Supported periods are P7D and P1D, or PTOS for no seasonality.",
              seasonality));
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

    checkArgument(lookback >= 5, "Lookback is %s points. Lookback should be greater than 5 points.", lookback);
  }

  @Override
  public AnomalyDetectorResult runDetection(final Interval window,
      final Map<String, DataTable> dataTableMap
  ) throws DetectorException {
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
    final DataFrame baselineDf = computeBaseline(inputDf, window.getStartMillis());
    inputDf
        // rename current which is still called "value" to "current"
        .renameSeries(COL_VALUE, COL_CURRENT)
        // left join baseline values
        .addSeries(baselineDf, COL_VALUE, COL_ERROR, COL_LOWER_BOUND, COL_UPPER_BOUND)
        .addSeries(COL_DIFF, inputDf.getDoubles(COL_CURRENT).subtract(inputDf.get(COL_VALUE)))
        .addSeries(COL_PATTERN, patternMatch(pattern, inputDf))
        .addSeries(COL_DIFF_VIOLATION,
            inputDf.getDoubles(COL_DIFF).abs().gt(inputDf.getDoubles(COL_ERROR)))
        .mapInPlace(BooleanSeries.ALL_TRUE, COL_ANOMALY, COL_PATTERN, COL_DIFF_VIOLATION);

    return new SimpleAnomalyDetectorResult(inputDf);
  }

  private DataFrame computeBaseline(final DataFrame inputDF, final long windowStartTime) {

    final DataFrame resultDF = new DataFrame();
    //filter the data inside window for current values.
    final DataFrame forecastDF = inputDF
        .filter((LongConditional) values -> values[0] >= windowStartTime, COL_TIME)
        .dropNull();

    final int size = forecastDF.size();
    final double[] baselineArray = new double[size];
    final double[] upperBoundArray = new double[size];
    final double[] lowerBoundArray = new double[size];
    final long[] resultTimeArray = new long[size];
    final double[] errorArray = new double[size];

    // todo cyril compute mean and std in a single pass
    // https://nestedsoftware.com/2018/03/20/calculating-a-moving-average-on-streaming-data-5a7k.22879.html
    // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm
    for (int k = 0; k < size; k++) {
      final long forecastTime = forecastDF.getLong(COL_TIME, k);
      final DataFrame lookbackDf = getLookbackDf(inputDF, forecastTime);
      final DoubleSeries periodMask = buildPeriodMask(lookbackDf, forecastTime);
      // todo cyril implement median
      final double mean = lookbackDf.getDoubles(COL_VALUE).multiply(periodMask).mean().value();
      final double std = lookbackDf.getDoubles(COL_VALUE).multiply(periodMask).std().value();
      //calculate baseline, error , upper and lower bound for prediction window.
      resultTimeArray[k] = forecastTime;
      baselineArray[k] = mean;
      errorArray[k] = sigma(sensitivity) * std;
      upperBoundArray[k] = baselineArray[k] + errorArray[k];
      lowerBoundArray[k] = baselineArray[k] - errorArray[k];
    }
    //Construct the dataframe.
    resultDF
        .addSeries(COL_TIME, LongSeries.buildFrom(resultTimeArray))
        .setIndex(COL_TIME);

    resultDF
        .addSeries(COL_VALUE, DoubleSeries.buildFrom(baselineArray))
        .addSeries(COL_UPPER_BOUND, DoubleSeries.buildFrom(upperBoundArray))
        .addSeries(COL_LOWER_BOUND, DoubleSeries.buildFrom(lowerBoundArray))
        .addSeries(COL_ERROR, DoubleSeries.buildFrom(errorArray));

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
        "Could not find index of endTime. endTime should exist in inputDf. This should not happen.");

    DataFrame loobackDf = DataFrame.builder(COL_TIME, COL_VALUE).build();
    final int indexStart = indexEnd - lookback;
    checkArgument(indexStart >= 0,
        "Invalid index. Insufficient data to compute mean/variance on lookback. index: "
            + indexStart);
    loobackDf = loobackDf.append(inputDF.slice(indexStart, indexEnd));
    return loobackDf;
  }
}
