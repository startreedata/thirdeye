package org.apache.pinot.thirdeye.detection.components.detectors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Result;
import org.apache.pinot.thirdeye.spi.detection.DetectorException;
import org.apache.pinot.thirdeye.spi.detection.Pattern;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.SimpleDataTable;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.annotations.Test;

public class MeanVarianceRuleDetectorTest {

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

  private static final DataFrame historicalData = new DataFrame()
      .addSeries(DataFrame.COL_TIME,
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
      .addSeries(DataFrame.COL_VALUE,
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
    // test all dataframes columns expected in a AnomalyDetectorV2Result dataframe
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(DataFrame.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(DataFrame.COL_VALUE, 120, 80, 100, 120, 80)
        .append(historicalData)
        .sortedBy(DataFrame.COL_TIME);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    MeanVarianceRuleDetectorSpec spec = new MeanVarianceRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    spec.setLookback(10);
    spec.setSensitivity(0); // corresponds to multiplying std by 1.5 to get the bounds
    MeanVarianceRuleDetector detector = new MeanVarianceRuleDetector();
    detector.init(spec);

    AnomalyDetectorV2Result output = detector.runDetection(interval, timeSeriesMap);
    //assert time fields
    assertThat(output.getTimeZone()).isEqualTo(AbstractSpec.DEFAULT_TIMEZONE);
    assertThat(output.getMonitoringGranularityPeriod().toStandardDuration().getMillis())
        .isEqualTo(Period.days(1).toStandardDuration().getMillis());

    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    LongSeries outputTimeSeries = outputDf.getLongs(DataFrame.COL_TIME);
    LongSeries expectedTimeSeries = currentDf.getLongs(DataFrame.COL_TIME);
    assertThat(outputTimeSeries).isEqualTo(expectedTimeSeries);

    DoubleSeries outputValueSeries = outputDf.getDoubles(DataFrame.COL_VALUE);
    DoubleSeries expectedValueSeries = DoubleSeries.nulls(10)
        .append(DoubleSeries.buildFrom(100.0, 102., 98., 100., 102.));
    assertThat(outputValueSeries).isEqualTo(expectedValueSeries);

    DoubleSeries outputCurrentSeries = outputDf.getDoubles(DataFrame.COL_CURRENT);
    DoubleSeries expectedCurrentSeries = currentDf.getDoubles(DataFrame.COL_VALUE);
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
    DoubleSeries outputUpperBoundSeries = outputDf.getDoubles(DataFrame.COL_UPPER_BOUND);
    DoubleSeries expectedUpperBoundSeries = DoubleSeries.nulls(10)
        .append(DoubleSeries.buildFrom(
            100.0 + errorMargin1,
            102. + errorMargin2,
            98. + errorMargin3,
            100. + errorMargin4,
            102. + errorMargin5));
    assertThat(outputUpperBoundSeries).isEqualTo(expectedUpperBoundSeries);

    DoubleSeries outputLowerBoundSeries = outputDf.getDoubles(DataFrame.COL_LOWER_BOUND);
    DoubleSeries expectedLowerBoundSeries = DoubleSeries.nulls(10)
        .append(DoubleSeries.buildFrom(
            100.0 - errorMargin1,
            102. - errorMargin2,
            98. - errorMargin3,
            100. - errorMargin4,
            102. - errorMargin5));
    assertThat(outputLowerBoundSeries).isEqualTo(expectedLowerBoundSeries);

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(DataFrame.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(10)
        .append(BooleanSeries.fillValues(5, false));
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testDetectionRunsOnIntervalOnly() throws DetectorException {
    // test anomaly analysis is only conducted on the interval
    // notice the interval is smaller than the dataframe data
    Interval interval = new Interval(JANUARY_3_2021, JANUARY_5_2021);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(DataFrame.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(DataFrame.COL_VALUE, 120, 80, 100, 120, 80)
        .append(historicalData)
        .sortedBy(DataFrame.COL_TIME);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    MeanVarianceRuleDetectorSpec spec = new MeanVarianceRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    spec.setLookback(10);
    spec.setSensitivity(0);
    MeanVarianceRuleDetector detector = new MeanVarianceRuleDetector();
    detector.init(spec);

    AnomalyDetectorV2Result output = detector.runDetection(interval, timeSeriesMap);
    DataFrame outputDf = output.getDataFrame();
    BooleanSeries outputAnomalySeries = outputDf.getBooleans(DataFrame.COL_ANOMALY);
    // out of window is null
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(10 + 2)
        // only 3 non null
        .append(BooleanSeries.fillValues(3, false));
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testAnomaliesUpAndDown() throws DetectorException {
    // test pattern UP_AND_DOWN works
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(DataFrame.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(DataFrame.COL_VALUE, 120, 80, 100, 120, 80)
        .append(historicalData)
        .sortedBy(DataFrame.COL_TIME);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    MeanVarianceRuleDetectorSpec spec = new MeanVarianceRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    spec.setLookback(10);
    spec.setSensitivity(5); // corresponds to multiplying std by 1 to get the bounds
    MeanVarianceRuleDetector detector = new MeanVarianceRuleDetector();
    detector.init(spec);

    AnomalyDetectorV2Result output = detector.runDetection(interval, timeSeriesMap);
    //assert time fields
    assertThat(output.getTimeZone()).isEqualTo(AbstractSpec.DEFAULT_TIMEZONE);
    assertThat(output.getMonitoringGranularityPeriod().toStandardDuration().getMillis())
        .isEqualTo(Period.days(1).toStandardDuration().getMillis());

    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(DataFrame.COL_ANOMALY);
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
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(DataFrame.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(DataFrame.COL_VALUE, 120, 80, 100, 120, 80)
        .append(historicalData)
        .sortedBy(DataFrame.COL_TIME);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    MeanVarianceRuleDetectorSpec spec = new MeanVarianceRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    spec.setPattern(Pattern.UP);
    spec.setLookback(10);
    spec.setSensitivity(5); // corresponds to multiplying std by 1 to get the bounds
    MeanVarianceRuleDetector detector = new MeanVarianceRuleDetector();
    detector.init(spec);

    AnomalyDetectorV2Result output = detector.runDetection(interval, timeSeriesMap);
    //assert time fields
    assertThat(output.getTimeZone()).isEqualTo(AbstractSpec.DEFAULT_TIMEZONE);
    assertThat(output.getMonitoringGranularityPeriod().toStandardDuration().getMillis())
        .isEqualTo(Period.days(1).toStandardDuration().getMillis());

    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(DataFrame.COL_ANOMALY);
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
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(DataFrame.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(DataFrame.COL_VALUE, 120, 80, 100, 120, 80)
        .append(historicalData)
        .sortedBy(DataFrame.COL_TIME);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    MeanVarianceRuleDetectorSpec spec = new MeanVarianceRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    spec.setPattern(Pattern.DOWN);
    spec.setLookback(10);
    spec.setSensitivity(5); // corresponds to multiplying std by 1 to get the bounds
    MeanVarianceRuleDetector detector = new MeanVarianceRuleDetector();
    detector.init(spec);

    AnomalyDetectorV2Result output = detector.runDetection(interval, timeSeriesMap);
    //assert time fields
    assertThat(output.getTimeZone()).isEqualTo(AbstractSpec.DEFAULT_TIMEZONE);
    assertThat(output.getMonitoringGranularityPeriod().toStandardDuration().getMillis())
        .isEqualTo(Period.days(1).toStandardDuration().getMillis());

    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(DataFrame.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(10)
        .append(BooleanSeries.buildFrom(
            BooleanSeries.FALSE, // change is up
            BooleanSeries.TRUE, // change is down
            BooleanSeries.FALSE,
            BooleanSeries.FALSE, // change is up
            BooleanSeries.TRUE)); // change is down
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }
}
