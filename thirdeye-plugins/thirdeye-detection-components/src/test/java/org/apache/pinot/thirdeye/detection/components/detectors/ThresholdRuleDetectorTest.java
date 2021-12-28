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
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.SimpleDataTable;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class ThresholdRuleDetectorTest {

  private static final long JANUARY_1_2021 = 1609459200000L;
  private static final long JANUARY_2_2021 = 1609545600000L;
  private static final long JANUARY_3_2021 = 1609632000000L;
  private static final long JANUARY_4_2021 = 1609718400000L;
  private static final long JANUARY_5_2021 = 1609804800000L;

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
        .addSeries(DataFrame.COL_VALUE, 100., 200., 300., 400., 500.);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    ThresholdRuleDetectorSpec spec = new ThresholdRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    double minValue = 50.;
    double maxValue = 600.;
    spec.setMin(minValue);
    spec.setMax(maxValue);
    ThresholdRuleDetector detector = new ThresholdRuleDetector();
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
    DoubleSeries expectedValueSeries = currentDf.getDoubles(DataFrame.COL_VALUE);
    assertThat(outputValueSeries).isEqualTo(expectedValueSeries);

    DoubleSeries outputCurrentSeries = outputDf.getDoubles(DataFrame.COL_CURRENT);
    DoubleSeries expectedCurrentSeries = currentDf.getDoubles(DataFrame.COL_VALUE);
    assertThat(outputCurrentSeries).isEqualTo(expectedCurrentSeries);

    DoubleSeries outputUpperBoundSeries = outputDf.getDoubles(DataFrame.COL_UPPER_BOUND);
    DoubleSeries expectedUpperBoundSeries = DoubleSeries.fillValues(currentDf.size(), maxValue);
    assertThat(outputUpperBoundSeries).isEqualTo(expectedUpperBoundSeries);

    DoubleSeries outputLowerBoundSeries = outputDf.getDoubles(DataFrame.COL_LOWER_BOUND);
    DoubleSeries expectedLowerBoundSeries = DoubleSeries.fillValues(currentDf.size(), minValue);
    assertThat(outputLowerBoundSeries).isEqualTo(expectedLowerBoundSeries);

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(DataFrame.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.fillValues(currentDf.size(), false);
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  @Ignore
  //fixme cyril this test should pass - change the current behavior
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
        .addSeries(DataFrame.COL_VALUE, 100., 200., 300., 400., 500.);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    ThresholdRuleDetectorSpec spec = new ThresholdRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    // min max not set - not relevant for test
    ThresholdRuleDetector detector = new ThresholdRuleDetector();
    detector.init(spec);

    AnomalyDetectorV2Result output = detector.runDetection(interval, timeSeriesMap);
    DataFrame outputDf = output.getDataFrame();
    LongSeries outputTimeSeries = outputDf.getLongs(DataFrame.COL_TIME);
    LongSeries expectedTimeSeries = LongSeries.buildFrom(JANUARY_3_2021,
        JANUARY_4_2021,
        JANUARY_5_2021);
    assertThat(outputTimeSeries).isEqualTo(expectedTimeSeries);
  }

  @Test
  public void testAnomaliesUpAndDown() throws DetectorException {
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(DataFrame.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(DataFrame.COL_VALUE, 100., 200., 300., 400., 500.);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    ThresholdRuleDetectorSpec spec = new ThresholdRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    double minValue = 150;
    double maxValue = 350.;
    spec.setMin(minValue);
    spec.setMax(maxValue);
    ThresholdRuleDetector detector = new ThresholdRuleDetector();
    detector.init(spec);

    AnomalyDetectorV2Result output = detector.runDetection(interval, timeSeriesMap);
    DataFrame outputDf = output.getDataFrame();

    DoubleSeries outputValueSeries = outputDf.getDoubles(DataFrame.COL_VALUE);
    DoubleSeries expectedValueSeries = DoubleSeries.buildFrom(
        minValue,
        200,
        300,
        maxValue,
        maxValue);
    assertThat(outputValueSeries).isEqualTo(expectedValueSeries);

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(DataFrame.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.buildFrom(
        BooleanSeries.TRUE,
        BooleanSeries.FALSE,
        BooleanSeries.FALSE,
        BooleanSeries.TRUE,
        BooleanSeries.TRUE);
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }
}
