/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.detectors;

import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_ANOMALY;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_CURRENT;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_DIFF;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_DIFF_VIOLATION;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_ERROR;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_LOWER_BOUND;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_PATTERN;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_TIME;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_UPPER_BOUND;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_VALUE;
import static ai.startree.thirdeye.spi.dataframe.Series.LongConditional;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detection.components.SimpleAnomalyDetectorResult;
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
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.ReadableInterval;
import org.joda.time.format.ISOPeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * History mean and standard deviation based forecasting and detection.
 * Forecast using history mean and standard deviation.
 */
public class MeanVarianceRuleDetector implements AnomalyDetector<MeanVarianceRuleDetectorSpec>,
    BaselineProvider<MeanVarianceRuleDetectorSpec> {

  private static final Logger LOG = LoggerFactory.getLogger(MeanVarianceRuleDetector.class);

  private Pattern pattern;
  private double sensitivity;
  private int lookback;
  private MeanVarianceRuleDetectorSpec spec;

  /**
   * Mapping of sensitivity to sigma on range of 0.5 - 1.5
   *
   * @param sensitivity double from 0 to 10. Values outside this range are clipped to 0, 10
   * @return sigma
   */
  private static double sigma(final double sensitivity) {
    return 0.5 + 0.1 * (10 - Math.max(Math.min(sensitivity, 10), 0));
  }

  @Override
  public void init(final MeanVarianceRuleDetectorSpec spec) {
    this.spec = spec;
    pattern = spec.getPattern();
    lookback = spec.getLookback();
    sensitivity = spec.getSensitivity();

    if (spec.getLookbackPeriod() != null) {
      checkArgument(spec.getMonitoringGranularity() != null, "monitoringGranularity is required when lookbackPeriod is used");
      this.lookback = computeLookbackSteps(spec.getLookbackPeriod(), spec.getMonitoringGranularity());
    } // else use default or set lookback - but not a good idea

    checkArgument(lookback >= 5,
        String.format("Lookback is %d. Lookback should be greater than 5.", lookback));
  }

  protected static int computeLookbackSteps(final String lookbackPeriodString,
      final String monitoringGranularityString) {
    // mind that computing lookback only once is not exactly correct when a day has 25 hours or 23 hours - but very minor issue
    final Period lookbackPeriod = Period.parse(lookbackPeriodString, ISOPeriodFormat.standard());
    final Period monitoringGranularity = Period.parse(monitoringGranularityString, ISOPeriodFormat.standard());

    return (int) (lookbackPeriod.toStandardDuration().getMillis()/monitoringGranularity.toStandardDuration().getMillis());
  }

  @Override
  public AnomalyDetectorResult runDetection(final Interval window,
      final Map<String, DataTable> timeSeriesMap
  ) throws DetectorException {
    final DataTable current = requireNonNull(timeSeriesMap.get(KEY_CURRENT), "current is null");
    final DataFrame currentDf = current.getDataFrame();
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
            inputDf.getDoubles(COL_DIFF).abs().gte(inputDf.getDoubles(COL_ERROR)))
        .mapInPlace(BooleanSeries.ALL_TRUE, COL_ANOMALY, COL_PATTERN, COL_DIFF_VIOLATION);

    return new SimpleAnomalyDetectorResult(inputDf);
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
      final DataFrame lookbackDf = getLookbackDf(inputDF, forecastDF.getLong(COL_TIME, k));
      double mean = lookbackDf.getDoubles(COL_VALUE).mean().value();
      double std = lookbackDf.getDoubles(COL_VALUE).std().value();
      //calculate baseline, error , upper and lower bound for prediction window.
      resultTimeArray[k] = forecastDF.getLong(COL_TIME, k);
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
