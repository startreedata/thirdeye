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
package ai.startree.thirdeye.plugins.detection.components.detectors;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.plugins.detection.components.detectors.HoltWintersDetector;
import ai.startree.thirdeye.plugins.detection.components.detectors.HoltWintersDetectorSpec;
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
import org.assertj.core.data.Offset;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.annotations.Test;

public class HoltWintersDetectorTest {

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
  private static final long JANUARY_8_2021 = 1610064000000L;

  private static final DataFrame HISTORICAL_DATA = new DataFrame()
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
        .append(HISTORICAL_DATA)
        .sortedBy(Constants.COL_TIME);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setLookbackPeriod("P10D");
    spec.setSeasonalityPeriod("P3D");
    spec.setSensitivity(0); // corresponds to zscore of 1
    HoltWintersDetector detector = new HoltWintersDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    LongSeries outputTimeSeries = outputDf.getLongs(Constants.COL_TIME);
    LongSeries expectedTimeSeries = currentDf.getLongs(Constants.COL_TIME);
    assertThat(outputTimeSeries).isEqualTo(expectedTimeSeries);

    DoubleSeries outputValueSeries = outputDf.getDoubles(Constants.COL_VALUE);
    assertThat(outputValueSeries.sliceTo(10)).isEqualTo(DoubleSeries.nulls(10));
    Offset<Double> predictionPrecision = Offset.offset(0.1);
    double valueDay1 = outputValueSeries.getDouble(10);
    assertThat(valueDay1).isCloseTo(120, predictionPrecision);
    double valueDay2 = outputValueSeries.getDouble(10 + 1);
    assertThat(valueDay2).isCloseTo(80, predictionPrecision);
    double valueDay3 = outputValueSeries.getDouble(10 + 2);
    assertThat(valueDay3).isCloseTo(100, predictionPrecision);
    double valueDay4 = outputValueSeries.getDouble(10 + 3);
    assertThat(valueDay4).isCloseTo(120, predictionPrecision);
    double valueDay5 = outputValueSeries.getDouble(10 + 4);
    assertThat(valueDay5).isCloseTo(80, predictionPrecision);

    DoubleSeries outputCurrentSeries = outputDf.getDoubles(Constants.COL_CURRENT);
    DoubleSeries expectedCurrentSeries = currentDf.getDoubles(Constants.COL_VALUE);
    assertThat(outputCurrentSeries).isEqualTo(expectedCurrentSeries);

    DoubleSeries outputUpperBoundSeries = outputDf.getDoubles(Constants.COL_UPPER_BOUND);
    assertThat(outputUpperBoundSeries.sliceTo(10)).isEqualTo(DoubleSeries.nulls(10));
    // todo cyril not checking values because the error behavior is strange and should be fixed
    assertThat(outputUpperBoundSeries.sliceFrom(10).hasNull()).isFalse();

    DoubleSeries outputLowerBoundSeries = outputDf.getDoubles(Constants.COL_LOWER_BOUND);
    assertThat(outputLowerBoundSeries.sliceTo(10)).isEqualTo(DoubleSeries.nulls(10));
    assertThat(outputLowerBoundSeries.sliceFrom(10).hasNull()).isFalse();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(10)
        .append(BooleanSeries.fillValues(5, false));
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testZeroesInHistoricalData() throws DetectorException {
    final DataFrame historicalDataWithZeroes = new DataFrame()
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
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0);
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
            JANUARY_7_2021,
            JANUARY_8_2021)
        .addSeries(Constants.COL_VALUE, 0, 0, 100, 120, 80, 100, 120, 80)
        .append(historicalDataWithZeroes)
        .sortedBy(Constants.COL_TIME);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setLookbackPeriod("P10D");
    spec.setSeasonalityPeriod("P3D");
    spec.setSensitivity(0); // corresponds to zscore of 1
    HoltWintersDetector detector = new HoltWintersDetector();
    detector.init(spec);

    DataFrame outputSkippingZeroes = detector.runDetection(interval, timeSeriesMap).getDataFrame();
    detector.FAST_SKIP_ZEROES = false;
    DataFrame outputComputingWhenZeroes = detector.runDetection(interval, timeSeriesMap).getDataFrame();

    // ensure fast zeroes skipping does not change the behaviour
    assertThat(outputSkippingZeroes).isEqualTo(outputComputingWhenZeroes);
    // check rule of thumb error when only one element to compute variance
    assertThat(outputSkippingZeroes.get("error").getDoubles().get(13)).isEqualTo(50);
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
        .append(HISTORICAL_DATA)
        .sortedBy(Constants.COL_TIME);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setLookbackPeriod("P10D");
    spec.setSeasonalityPeriod("P3D");
    spec.setSensitivity(0); // corresponds to zscore of 1
    HoltWintersDetector detector = new HoltWintersDetector();
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
        .addSeries(Constants.COL_VALUE, 180, 80, 100, 70, 80)
        .append(HISTORICAL_DATA)
        .sortedBy(Constants.COL_TIME);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setLookbackPeriod("P10D");
    spec.setSeasonalityPeriod("P3D");
    spec.setSensitivity(0); // corresponds to zscore of 1
    HoltWintersDetector detector = new HoltWintersDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(10)
        .append(BooleanSeries.buildFrom(
            BooleanSeries.TRUE, // change is up
            BooleanSeries.FALSE,
            BooleanSeries.FALSE,
            BooleanSeries.TRUE, // change is down
            BooleanSeries.FALSE));
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testAnomaliesUpOnly() throws DetectorException {
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
        .addSeries(Constants.COL_VALUE, 180, 80, 100, 70, 80)
        .append(HISTORICAL_DATA)
        .sortedBy(Constants.COL_TIME);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setPattern(Pattern.UP);
    spec.setLookbackPeriod("P10D");
    spec.setSeasonalityPeriod("P3D");
    spec.setSensitivity(0); // corresponds to zscore of 1
    HoltWintersDetector detector = new HoltWintersDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(10)
        .append(BooleanSeries.buildFrom(
            BooleanSeries.TRUE, // change is up
            BooleanSeries.FALSE,
            BooleanSeries.FALSE,
            BooleanSeries.FALSE, // change is down
            BooleanSeries.FALSE));
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testAnomaliesDownOnly() throws DetectorException {
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
        .addSeries(Constants.COL_VALUE, 180, 80, 100, 70, 80)
        .append(HISTORICAL_DATA)
        .sortedBy(Constants.COL_TIME);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setPattern(Pattern.DOWN);
    spec.setLookbackPeriod("P10D");
    spec.setSeasonalityPeriod("P3D");
    spec.setSensitivity(0); // corresponds to zscore of 1
    HoltWintersDetector detector = new HoltWintersDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.nulls(10)
        .append(BooleanSeries.buildFrom(
            BooleanSeries.FALSE, // change is up
            BooleanSeries.FALSE,
            BooleanSeries.FALSE,
            BooleanSeries.TRUE, // change is down
            BooleanSeries.FALSE));
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }
}
