/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */
package ai.startree.thirdeye.detection.components.detectors;

import static ai.startree.thirdeye.detection.components.detectors.MeanVarianceRuleDetector.computeLookbackSteps;
import static ai.startree.thirdeye.detection.components.detectors.MeanVarianceRuleDetector.patternMatch;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_ANOMALY;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_CURRENT;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_DIFF;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_DIFF_VIOLATION;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_LOWER_BOUND;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_PATTERN;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_TIME;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_UPPER_BOUND;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_VALUE;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detection.components.SimpleAnomalyDetectorResult;
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
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.joda.time.Interval;
import org.joda.time.ReadableInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holt-Winters forecasting algorithm with multiplicative method
 * Supports seasonality and trend detection
 * Forecast with holt winters triple exponential smoothing and generate anomalies
 *
 * https://otexts.com/fpp2/holt-winters.html
 */
public class HoltWintersDetector implements BaselineProvider<HoltWintersDetectorSpec>,
    AnomalyDetector<HoltWintersDetectorSpec> {

  private static final Logger LOG = LoggerFactory.getLogger(HoltWintersDetector.class);
  private static final String COL_ERROR = "error";
  @VisibleForTesting
  protected boolean FAST_SKIP_ZEROES = true;

  private int period;
  private double alpha;
  private double beta;
  private double gamma;
  private Pattern pattern;
  private double sensitivity;
  private HoltWintersDetectorSpec spec;
  private int lookback = 60;

  @Override
  public void init(final HoltWintersDetectorSpec spec) {
    this.spec = spec;
    period = spec.getPeriod();
    alpha = spec.getAlpha();
    beta = spec.getBeta();
    gamma = spec.getGamma();
    pattern = requireNonNull(spec.getPattern(),
        "pattern is null. Allowed values : " + Arrays.toString(Pattern.values()));
    sensitivity = spec.getSensitivity();

    if (spec.getLookbackPeriod() != null) {
      checkArgument(spec.getMonitoringGranularity() != null, "monitoringGranularity is required when lookbackPeriod is used");
      this.lookback = computeLookbackSteps(spec.getLookbackPeriod(), spec.getMonitoringGranularity());
    } else if (spec.getLookback() != null) {
      this.lookback = spec.getLookback();
    } // else uses default value - but not a good idea - maybe throw error
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

  private AnomalyDetectorResult runDetectionOnSingleDataTable(final DataFrame inputDf,
      final ReadableInterval window) {
    DataFrame baselineDf = computeBaseline(inputDf, window.getStartMillis());
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

    return
        new SimpleAnomalyDetectorResult(inputDf);
  }

  /**
   * Returns a data frame containing lookback number of data before prediction time
   *
   * @param originalDF the original dataframe
   * @param time the prediction time, in unix timestamp
   * @return DataFrame containing lookback number of data
   */
  private DataFrame getLookbackDF(final DataFrame originalDF, final Long time) {
    final LongSeries longSeries = (LongSeries) originalDF.get(COL_TIME);
    final int indexFinish = longSeries.find(time);
    DataFrame df = DataFrame.builder(COL_TIME, COL_VALUE).build();

    if (indexFinish != -1) {
      final int indexStart = Math.max(0, indexFinish - lookback);
      df = df.append(originalDF.slice(indexStart, indexFinish));
    }
    return df;
  }

  /**
   * Holt Winters forecasting method
   *
   * @param y Timeseries to be forecasted
   * @param alpha level smoothing factor
   * @param beta trend smoothing factor
   * @param gamma seasonality smoothing factor
   * @return ForecastResults containing predicted value, SSE(sum of squared error) and error bound
   */
  private ForecastResults forecast(
      final double[] y, final double alpha, final double beta, final double gamma) {
    final double[] seasonal = new double[y.length + 1];
    final double[] forecast = new double[y.length + 1];

    final double a0 = calculateInitialLevel(y);
    final double b0 = calculateInitialTrend(y, period);

    final int seasons = y.length / period;
    final double[] initialSeasonalIndices = calculateSeasonalIndices(y, period,
        seasons);

    if (period >= 0) {
      System.arraycopy(initialSeasonalIndices, 0, seasonal, 0, period);
    }

    // s is level and t is trend
    double s = a0;
    double t = b0;
    double predictedValue = 0;

    for (int i = 0; i < y.length; i++) {
      final double sNew;
      final double tNew;
      forecast[i] = (s + t) * seasonal[i];
      sNew = alpha * (y[i] / seasonal[i]) + (1 - alpha) * (s + t);
      tNew = beta * (sNew - s) + (1 - beta) * t;
      if (i + period <= y.length) {
        // history of zero case
        if (sNew == 0) {
          seasonal[i + period] = 1;
        } else {
          seasonal[i + period] = gamma * (y[i] / (sNew * seasonal[i])) + (1 - gamma) * seasonal[i];
        }
      }
      s = sNew;
      t = tNew;
      if (i == y.length - 1) {
        predictedValue = (s + t) * seasonal[i + 1];
      }
    }

    final List<Double> diff = new ArrayList<>();
    double sse = 0;
    for (int i = 0; i < y.length; i++) {
      if (forecast[i] != 0 || (forecast[i] == 0 && y[i] != 0)) {
        sse += Math.pow(y[i] - forecast[i], 2);
        diff.add(forecast[i] - y[i]);
      }
    }

    final double error = calculateErrorBound(diff, zscore(sensitivity));
    return new ForecastResults(predictedValue, sse, error);
  }

  /**
   * Compute the baseline and error bound for given data
   *
   * @param inputDF training dataframe
   * @param windowStartTime prediction start time
   * @return DataFrame with timestamp, baseline, error bound
   */
  private DataFrame computeBaseline(final DataFrame inputDF, final long windowStartTime) {

    final DataFrame resultDF = new DataFrame();
    final DataFrame forecastDF = inputDF
        .filter((LongConditional) values -> values[0] >= windowStartTime, COL_TIME)
        .dropNull();

    final int size = forecastDF.size();
    final double[] baselineArray = new double[size];
    final double[] upperBoundArray = new double[size];
    final double[] lowerBoundArray = new double[size];
    final long[] resultTimeArray = new long[size];
    final double[] errorArray = new double[size];

    double lastAlpha = alpha;
    double lastBeta = beta;
    double lastGamma = gamma;

    for (int k = 0; k < size; k++) {
      final DataFrame trainingDF = getLookbackDF(inputDF, forecastDF.getLong(COL_TIME, k));

      // We need at least 2 periods of data
      // fixme cyril period is in number of observations - prefer ISO 8601 or auto period
      if (trainingDF.size() < 2 * period) {
        // fixme cyril adding warn only to not change the behavior but I think this should throw an exception - it will fail later
        LOG.warn("Not enough historical data available for Holt-Winters algorithm. Alert configuration may be incorrect.");
        continue;
      }

      resultTimeArray[k] = forecastDF.getLong(COL_TIME, k);

      final double[] y = trainingDF.getDoubles(COL_VALUE).values();
      // fixme cyril only a quickfix - rewrite the whole algo
      if (FAST_SKIP_ZEROES && Arrays.stream(y).allMatch(value -> value == 0)) {
        // training data is only 0 - just predict 0 -
        baselineArray[k] = 0;
        errorArray[k] = 0;
        upperBoundArray[k] = 0;
        lowerBoundArray[k] = 0;
        continue;
      }

      final HoltWintersParams params;
      if (alpha < 0 && beta < 0 && gamma < 0) {
        params = fitModelWithBOBYQA(y, lastAlpha, lastBeta, lastGamma);
      } else {
        params = new HoltWintersParams(alpha, beta, gamma);
      }

      lastAlpha = params.getAlpha();
      lastBeta = params.getBeta();
      lastGamma = params.getGamma();

      final ForecastResults result = forecast(y,
          params.getAlpha(),
          params.getBeta(),
          params.getGamma());
      final double predicted = result.getPredictedValue();
      final double error = result.getErrorBound();

      baselineArray[k] = predicted;
      errorArray[k] = error;
      upperBoundArray[k] = predicted + error;
      lowerBoundArray[k] = predicted - error;
    }

    resultDF.addSeries(COL_TIME, LongSeries.buildFrom(resultTimeArray)).setIndex(COL_TIME);
    resultDF.addSeries(COL_VALUE, DoubleSeries.buildFrom(baselineArray));
    resultDF.addSeries(COL_UPPER_BOUND, DoubleSeries.buildFrom(upperBoundArray));
    resultDF.addSeries(COL_LOWER_BOUND, DoubleSeries.buildFrom(lowerBoundArray));
    resultDF.addSeries(COL_ERROR, DoubleSeries.buildFrom(errorArray));
    return resultDF;
  }

  /**
   * Fit alpha, beta, gamma by optimizing SSE (Sum of squared errors) using BOBYQA
   * It is a derivative free bound constrained optimization algorithm
   * https://en.wikipedia.org/wiki/BOBYQA
   *
   * @param y the data
   * @param lastAlpha last alpha value
   * @param lastBeta last beta value
   * @param lastGamma last gamma value
   * @return double array containing fitted alpha, beta and gamma
   */
  private HoltWintersParams fitModelWithBOBYQA(final double[] y, double lastAlpha, double lastBeta,
      double lastGamma) {
    final BOBYQAOptimizer optimizer = new BOBYQAOptimizer(7);
    if (lastAlpha < 0) {
      lastAlpha = 0.1;
    }
    if (lastBeta < 0) {
      lastBeta = 0.01;
    }
    if (lastGamma < 0) {
      lastGamma = 0.001;
    }
    final InitialGuess initGuess = new InitialGuess(new double[]{lastAlpha, lastBeta, lastGamma});
    final MaxIter maxIter = new MaxIter(30000);
    final MaxEval maxEval = new MaxEval(30000);
    final GoalType goal = GoalType.MINIMIZE;
    final ObjectiveFunction objectiveFunction = new ObjectiveFunction(params -> forecast(y,
        params[0],
        params[1],
        params[2]).getSse());
    final SimpleBounds bounds = new SimpleBounds(new double[]{0.001, 0.001, 0.001},
        new double[]{0.999, 0.999, 0.999});

    HoltWintersParams params;
    try {
      final PointValuePair optimal = optimizer
          .optimize(objectiveFunction, goal, bounds, initGuess, maxIter, maxEval);
      LOG.debug("Performed {} iterations to optimize Holt-Winters", optimizer.getIterations());
      params = new HoltWintersParams(optimal.getPoint()[0], optimal.getPoint()[1],
          optimal.getPoint()[2]);
    } catch (final Exception e) {
      LOG.error(e.toString());
      params = new HoltWintersParams(lastAlpha, lastBeta, lastGamma);
    }
    return params;
  }

  /**
   * Container class to store holt winters parameters
   */
  final static class HoltWintersParams {

    private final double alpha;
    private final double beta;
    private final double gamma;

    HoltWintersParams(final double alpha, final double beta, final double gamma) {
      this.alpha = alpha;
      this.beta = beta;
      this.gamma = gamma;
    }

    double getAlpha() {
      return alpha;
    }

    double getBeta() {
      return beta;
    }

    double getGamma() {
      return gamma;
    }
  }

  private static double calculateInitialLevel(final double[] y) {
    return y[0];
  }

  /**
   * See: http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm
   *
   * @return - Initial trend - Bt[1]
   */
  private static double calculateInitialTrend(final double[] y, final int period) {
    double sum = 0;

    for (int i = 0; i < period; i++) {
      sum += y[period + i] - y[i];
    }

    return sum / (period * period);
  }

  /**
   * See: http://www.itl.nist.gov/div898/handbook/pmc/section4/pmc435.htm
   *
   * @return - Seasonal Indices.
   */
  private static double[] calculateSeasonalIndices(final double[] y, final int period,
      final int seasons) {
    final double[] seasonalMean = new double[seasons];
    final double[] seasonalIndices = new double[period];

    final double[] averagedObservations = new double[y.length];

    for (int i = 0; i < seasons; i++) {
      for (int j = 0; j < period; j++) {
        seasonalMean[i] += y[(i * period) + j];
      }
      seasonalMean[i] /= period;
    }

    for (int i = 0; i < seasons; i++) {
      for (int j = 0; j < period; j++) {
        // zero case
        if (seasonalMean[i] == 0 && y[(i * period) + j] == 0) {
          // no seasonality
          averagedObservations[(i * period) + j] = 1;
        }
        // case seasonalMean = 0 and y[(i * period) + j] != 0 cannot happen if all values are positive
        // very unlikely to happen if at least one value is not null
        // fixme cyril add logging if this happens (or reimplement HW) - for the moment returns a nan
        else {
          averagedObservations[(i * period) + j] = y[(i * period) + j] / seasonalMean[i];
        }
      }
    }

    for (int i = 0; i < period; i++) {
      for (int j = 0; j < seasons; j++) {
        seasonalIndices[i] += averagedObservations[(j * period) + i];
      }
      seasonalIndices[i] /= seasons;
    }

    return seasonalIndices;
  }

  /**
   * Returns the error bound of given list based on mean, std and given zscore
   *
   * @param givenNumbers double list
   * @param zscore zscore used to multiply by std
   * @return the error bound
   */
  private static double calculateErrorBound(final List<Double> givenNumbers, final double zscore) {
    // no data: cannot compute mean and variance
    if (givenNumbers.size() == 0) {
      return 0;
    }
    // one point: cannot compute variance - apply rule of thumb
    if (givenNumbers.size() == 1) {
      return Math.abs(givenNumbers.get(0)) / 2;
    }
    // calculate the mean value (= average)
    double sum = 0.0;
    for (final double num : givenNumbers) {
      sum += num;
    }
    final double mean = sum / givenNumbers.size();

    // calculate standard deviation
    double squaredDifferenceSum = 0.0;
    for (final double num : givenNumbers) {
      squaredDifferenceSum += (num - mean) * (num - mean);
    }
    final double variance = squaredDifferenceSum / givenNumbers.size();
    final double standardDeviation = Math.sqrt(variance);

    return zscore * standardDeviation;
  }

  /**
   * Mapping of sensitivity to zscore on range of 1 - 3
   *
   * @param sensitivity double from 0 to 10. Values outside this range are clipped to 0, 10
   * @return zscore
   */
  private static double zscore(double sensitivity) {
    return 1 + 0.2 * (10 - Math.max(Math.min(sensitivity, 10), 0));
  }

  /**
   * Container class to store forecasting results
   */
  final static class ForecastResults {

    private final double predictedValue;
    private final double sse;
    private final double errorBound;

    ForecastResults(final double predictedValue, final double sse, final double errorBound) {
      this.predictedValue = predictedValue;
      this.sse = sse;
      this.errorBound = errorBound;
    }

    double getPredictedValue() {
      return predictedValue;
    }

    double getSse() {
      return sse;
    }

    double getErrorBound() {
      return errorBound;
    }
  }
}
