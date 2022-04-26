/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.detectors;

import static ai.startree.thirdeye.detection.components.detectors.MeanVarianceRuleDetector.computeSteps;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.detection.AnomalyDetector;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorResult;
import ai.startree.thirdeye.spi.detection.DetectorException;
import ai.startree.thirdeye.spi.detection.Pattern;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.annotations.Test;

public class MeanVarianceRuleDetectorTest {

  private static final long DECEMBER_18_2020 = 1608249600000L;
  private static final long DECEMBER_19_2020 = 1608336000000L;
  private static final long DECEMBER_20_2020 = 1608422400000L;
  private static final long DECEMBER_21_2020 = 1608508800000L;
  private static final long DECEMBER_22_2020 = 1608595200000L;
  private static final long DECEMBER_23_2020 = 1608681600000L;
  private static final long DECEMBER_24_2020 = 1608768000000L;
  private static final long DECEMBER_25_2020 = 1608854400000L;
  private static final long DECEMBER_26_2020 = 1608940800000L;
  private static final long DECEMBER_27_2020 = 1609027200000L;
  private static final long DECEMBER_28_2020 = 1609113600000L;
  private static final long DECEMBER_29_2020 = 1609200000000L;
  private static final long DECEMBER_30_2020 = 1609286400000L;
  private static final long DECEMBER_31_2020 = 1609372800000L;
  private static final long JANUARY_1_2021 = 1609459200000L;
  private static final long JANUARY_2_2021 = 1609545600000L;
  private static final long JANUARY_3_2021 = 1609632000000L;
  private static final long JANUARY_4_2021 = 1609718400000L;
  private static final long JANUARY_5_2021 = 1609804800000L;
  private static final long JANUARY_6_2021 = 1609891200000L;
  private static final long JANUARY_7_2021 = 1609977600000L;

  private static final DataFrame historicalData = new DataFrame()
      .addSeries(Constants.COL_TIME,
          DECEMBER_22_2020,
          DECEMBER_23_2020,
          DECEMBER_24_2020,
          DECEMBER_25_2020,
          DECEMBER_26_2020,
          DECEMBER_27_2020,
          DECEMBER_28_2020,
          DECEMBER_29_2020,
          DECEMBER_30_2020,
          DECEMBER_31_2020)
      // mean 100, std 16.329932
      .addSeries(Constants.COL_VALUE,
          100.,
          120.,
          80.,
          100.,
          120.,
          80.,
          100.,
          120.,
          80.,
          100.);

  @Test
  public void testNoAnomalies() throws DetectorException {
    // test all dataframes columns expected in a AnomalyDetectorResult dataframe
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021, DateTimeZone.UTC);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(Constants.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(Constants.COL_VALUE, 120, 80, 100, 120, 80)
        .append(historicalData)
        .sortedBy(Constants.COL_TIME);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    MeanVarianceRuleDetectorSpec spec = new MeanVarianceRuleDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setLookbackPeriod("P10D");
    spec.setSensitivity(0); // corresponds to multiplying std by 1.5 to get the bounds
    MeanVarianceRuleDetector detector = new MeanVarianceRuleDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    LongSeries outputTimeSeries = outputDf.getLongs(Constants.COL_TIME);
    LongSeries expectedTimeSeries = currentDf.getLongs(Constants.COL_TIME);
    assertThat(outputTimeSeries).isEqualTo(expectedTimeSeries);

    DoubleSeries outputValueSeries = outputDf.getDoubles(Constants.COL_VALUE);
    DoubleSeries expectedValueSeries = DoubleSeries.nulls(10)
        .append(DoubleSeries.buildFrom(100.0, 102., 98., 100., 102.));
    assertThat(outputValueSeries).isEqualTo(expectedValueSeries);

    DoubleSeries outputCurrentSeries = outputDf.getDoubles(Constants.COL_CURRENT);
    DoubleSeries expectedCurrentSeries = currentDf.getDoubles(Constants.COL_VALUE);
    assertThat(outputCurrentSeries).isEqualTo(expectedCurrentSeries);

    double stdSensitivityMultiplier = 1.5; // sensitivity is 10. see sigma function of the detector
    // manually computed stds - repeating because values are repeating
    double std1 = 16.32993161855452;
    double std2 = 17.511900715418264;
    double std3 = 17.511900715418264;
    double std4 = 16.32993161855452;
    double std5 = 17.511900715418264;
    double errorMargin1 = std1 * stdSensitivityMultiplier;
    double errorMargin2 = std2 * stdSensitivityMultiplier;
    double errorMargin3 = std3 * stdSensitivityMultiplier;
    double errorMargin4 = std4 * stdSensitivityMultiplier;
    double errorMargin5 = std5 * stdSensitivityMultiplier;
    DoubleSeries outputUpperBoundSeries = outputDf.getDoubles(Constants.COL_UPPER_BOUND);
    DoubleSeries expectedUpperBoundSeries = DoubleSeries.nulls(10)
        .append(DoubleSeries.buildFrom(
            100.0 + errorMargin1,
            102. + errorMargin2,
            98. + errorMargin3,
            100. + errorMargin4,
            102. + errorMargin5));
    assertThat(outputUpperBoundSeries).isEqualTo(expectedUpperBoundSeries);

    DoubleSeries outputLowerBoundSeries = outputDf.getDoubles(Constants.COL_LOWER_BOUND);
    DoubleSeries expectedLowerBoundSeries = DoubleSeries.nulls(10)
        .append(DoubleSeries.buildFrom(
            100.0 - errorMargin1,
            102. - errorMargin2,
            98. - errorMargin3,
            100. - errorMargin4,
            102. - errorMargin5));
    assertThat(outputLowerBoundSeries).isEqualTo(expectedLowerBoundSeries);

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(10)
        .append(BooleanSeries.fillValues(5, false));
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testDetectionRunsOnIntervalOnly() throws DetectorException {
    // test anomaly analysis is only conducted on the interval
    // notice the interval is smaller than the dataframe data
    Interval interval = new Interval(JANUARY_3_2021, JANUARY_5_2021, DateTimeZone.UTC);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(Constants.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(Constants.COL_VALUE, 120, 80, 100, 120, 80)
        .append(historicalData)
        .sortedBy(Constants.COL_TIME);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    MeanVarianceRuleDetectorSpec spec = new MeanVarianceRuleDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setLookbackPeriod("P10D");
    spec.setSensitivity(0);
    MeanVarianceRuleDetector detector = new MeanVarianceRuleDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    DataFrame outputDf = output.getDataFrame();
    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    // out of window is null
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(10 + 2)
        // only 3 non null
        .append(BooleanSeries.fillValues(3, false));
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testAnomaliesUpAndDown() throws DetectorException {
    // test pattern UP_AND_DOWN works
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021, DateTimeZone.UTC);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(Constants.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(Constants.COL_VALUE, 120, 80, 100, 120, 80)
        .append(historicalData)
        .sortedBy(Constants.COL_TIME);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    MeanVarianceRuleDetectorSpec spec = new MeanVarianceRuleDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setLookbackPeriod("P10D");
    spec.setSensitivity(5); // corresponds to multiplying std by 1 to get the bounds
    MeanVarianceRuleDetector detector = new MeanVarianceRuleDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(10)
        .append(BooleanSeries.buildFrom(
            BooleanSeries.TRUE, // change is up
            BooleanSeries.TRUE, // change is down
            BooleanSeries.FALSE,
            BooleanSeries.TRUE, // change is up
            BooleanSeries.TRUE)); // change is down
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testAnomaliesUpOnly() throws DetectorException {
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021, DateTimeZone.UTC);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(Constants.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(Constants.COL_VALUE, 120, 80, 100, 120, 80)
        .append(historicalData)
        .sortedBy(Constants.COL_TIME);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    MeanVarianceRuleDetectorSpec spec = new MeanVarianceRuleDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setPattern(Pattern.UP);
    spec.setLookbackPeriod("P10D");
    spec.setSensitivity(5); // corresponds to multiplying std by 1 to get the bounds
    MeanVarianceRuleDetector detector = new MeanVarianceRuleDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(10)
        .append(BooleanSeries.buildFrom(
            BooleanSeries.TRUE, // change is up
            BooleanSeries.FALSE, // change is down
            BooleanSeries.FALSE,
            BooleanSeries.TRUE, // change is up
            BooleanSeries.FALSE)); // change is down
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testAnomaliesDownOnly() throws DetectorException {
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021, DateTimeZone.UTC);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(Constants.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(Constants.COL_VALUE, 120, 80, 100, 120, 80)
        .append(historicalData)
        .sortedBy(Constants.COL_TIME);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    MeanVarianceRuleDetectorSpec spec = new MeanVarianceRuleDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setPattern(Pattern.DOWN);
    spec.setLookbackPeriod("P10D");
    spec.setSensitivity(5); // corresponds to multiplying std by 1 to get the bounds
    MeanVarianceRuleDetector detector = new MeanVarianceRuleDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(10)
        .append(BooleanSeries.buildFrom(
            BooleanSeries.FALSE, // change is up
            BooleanSeries.TRUE, // change is down
            BooleanSeries.FALSE,
            BooleanSeries.FALSE, // change is up
            BooleanSeries.TRUE)); // change is down
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testWithWeeklySeasonality() throws DetectorException {
    final DataFrame historicalData = new DataFrame()
        .addSeries(Constants.COL_TIME,
            DECEMBER_18_2020,
            DECEMBER_19_2020,
            DECEMBER_20_2020,
            DECEMBER_21_2020,
            DECEMBER_22_2020,
            DECEMBER_23_2020,
            DECEMBER_24_2020,
            DECEMBER_25_2020,
            DECEMBER_26_2020,
            DECEMBER_27_2020,
            DECEMBER_28_2020,
            DECEMBER_29_2020,
            DECEMBER_30_2020,
            DECEMBER_31_2020)
        // mean 100, std 16.329932
        .addSeries(Constants.COL_VALUE,
            4,
            5,
            6,
            7,
            1,
            2.,
            3.,
            4.,
            5.,
            6.,
            7.,
            1.,
            2.,
            3.);

    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021, DateTimeZone.UTC);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(Constants.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021,
            JANUARY_6_2021,
            JANUARY_7_2021)
        .addSeries(Constants.COL_VALUE, 4, 5, 6, 4, 1, 2, 10)
        .append(historicalData)
        .sortedBy(Constants.COL_TIME);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    MeanVarianceRuleDetectorSpec spec = new MeanVarianceRuleDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setLookbackPeriod("P14D");
    spec.setSensitivity(5); // corresponds to multiplying std by 1 to get the bounds
    spec.setSeasonalityPeriod("P7D");
    MeanVarianceRuleDetector detector = new MeanVarianceRuleDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(14)
        .append(BooleanSeries.buildFrom(
            BooleanSeries.FALSE,
            BooleanSeries.FALSE,
            BooleanSeries.FALSE,
            BooleanSeries.TRUE,
            BooleanSeries.FALSE,
            BooleanSeries.FALSE,
            BooleanSeries.TRUE));
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testComputeLookbackStepsWithDayGranularity() {
    String period = "P14D";
    String monitoringGranularity = "P1D";
    int output = computeSteps(period, monitoringGranularity);

    assertThat(output).isEqualTo(14);
  }

  @Test
  public void testComputeLookbackStepsWithHourlyGranularity() {
    String period = "P14D";
    String monitoringGranularity = "PT1H";
    int output = computeSteps(period, monitoringGranularity);

    assertThat(output).isEqualTo(336);
  }

  @Test
  public void testComputeLookbackStepsWithMinutelyGranularity() {
    String period = "P7D";
    String monitoringGranularity = "PT1M";
    int output = computeSteps(period, monitoringGranularity);

    assertThat(output).isEqualTo(10080);
  }

  @Test
  public void testComputeLookbackStepsWith15MinuteGranularity() {
    String period = "P7D";
    String monitoringGranularity = "PT15M";
    int output = computeSteps(period, monitoringGranularity);

    assertThat(output).isEqualTo(672);
  }

  @Test
  public void testComputeLookbackStepsWithLoobackAndPeriodNotDividingToInteger() {
    // 10080minutes / 25 minutes = 403.2
    String period = "P7D";
    String monitoringGranularity = "PT25M";
    int output = computeSteps(period, monitoringGranularity);

    assertThat(output).isEqualTo(403);
  }

  @Test
  public void testComputeLookbackStepsWithYearPeriod() {
    String period = "P365D";
    String monitoringGranularity = "P1D";
    int output = computeSteps(period, monitoringGranularity);
    assertThat(output).isEqualTo(365);
  }
}
