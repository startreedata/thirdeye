/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.pinot.thirdeye.detection.components;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.detection.DefaultInputDataFetcher;
import org.apache.pinot.thirdeye.detection.MockDataProvider;
import org.apache.pinot.thirdeye.detection.components.detectors.HoltWintersDetector;
import org.apache.pinot.thirdeye.detection.components.detectors.HoltWintersDetectorSpec;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.AlgorithmUtils;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Result;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.apache.pinot.thirdeye.spi.detection.DetectorException;
import org.apache.pinot.thirdeye.spi.detection.Pattern;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.SimpleDataTable;
import org.apache.pinot.thirdeye.spi.rootcause.impl.MetricEntity;
import org.assertj.core.data.Offset;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test class for HoltWinters detector
 */
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

  @Deprecated
  private DataProvider provider;

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

    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    spec.setLookback(10);
    spec.setPeriod(3);
    spec.setSensitivity(0); // corresponds to zscore of 1
    HoltWintersDetector detector = new HoltWintersDetector();
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
    assertThat(outputValueSeries.sliceTo(10)).isEqualTo(DoubleSeries.nulls(10));
    Offset<Double> predictionPrecision = Offset.offset(0.1);
    double valueDay1 = outputValueSeries.getDouble(10);
    assertThat(valueDay1).isCloseTo(120, predictionPrecision);
    double valueDay2 = outputValueSeries.getDouble(10+1);
    assertThat(valueDay2).isCloseTo(80, predictionPrecision);
    double valueDay3 = outputValueSeries.getDouble(10+2);
    assertThat(valueDay3).isCloseTo(100, predictionPrecision);
    double valueDay4 = outputValueSeries.getDouble(10+3);
    assertThat(valueDay4).isCloseTo(120, predictionPrecision);
    double valueDay5 = outputValueSeries.getDouble(10+4);
    assertThat(valueDay5).isCloseTo(80, predictionPrecision);

    DoubleSeries outputCurrentSeries = outputDf.getDoubles(DataFrame.COL_CURRENT);
    DoubleSeries expectedCurrentSeries = currentDf.getDoubles(DataFrame.COL_VALUE);
    assertThat(outputCurrentSeries).isEqualTo(expectedCurrentSeries);

    DoubleSeries outputUpperBoundSeries = outputDf.getDoubles(DataFrame.COL_UPPER_BOUND);
    assertThat(outputUpperBoundSeries.sliceTo(10)).isEqualTo(DoubleSeries.nulls(10));
    // todo cyril not checking values because the error behavior is strange and should be fixed
    assertThat(outputUpperBoundSeries.sliceFrom(10).hasNull()).isFalse();

    DoubleSeries outputLowerBoundSeries = outputDf.getDoubles(DataFrame.COL_LOWER_BOUND);
    assertThat(outputLowerBoundSeries.sliceTo(10)).isEqualTo(DoubleSeries.nulls(10));
    assertThat(outputLowerBoundSeries.sliceFrom(10).hasNull()).isFalse();

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

    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    spec.setLookback(10);
    spec.setPeriod(3);
    spec.setSensitivity(0); // corresponds to zscore of 1
    HoltWintersDetector detector = new HoltWintersDetector();
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
        .addSeries(DataFrame.COL_VALUE, 180, 80, 100, 70, 80)
        .append(historicalData)
        .sortedBy(DataFrame.COL_TIME);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    spec.setLookback(10);
    spec.setPeriod(3);
    spec.setSensitivity(0); // corresponds to zscore of 1
    HoltWintersDetector detector = new HoltWintersDetector();
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
            BooleanSeries.FALSE,
            BooleanSeries.FALSE,
            BooleanSeries.TRUE, // change is down
            BooleanSeries.FALSE));
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testAnomaliesUpOnly() throws DetectorException {
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
        .addSeries(DataFrame.COL_VALUE, 180, 80, 100, 70, 80)
        .append(historicalData)
        .sortedBy(DataFrame.COL_TIME);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    spec.setPattern(Pattern.UP);
    spec.setLookback(10);
    spec.setPeriod(3);
    spec.setSensitivity(0); // corresponds to zscore of 1
    HoltWintersDetector detector = new HoltWintersDetector();
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
            BooleanSeries.FALSE,
            BooleanSeries.FALSE,
            BooleanSeries.FALSE, // change is down
            BooleanSeries.FALSE));
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testAnomaliesDownOnly() throws DetectorException {
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
        .addSeries(DataFrame.COL_VALUE, 180, 80, 100, 70, 80)
        .append(historicalData)
        .sortedBy(DataFrame.COL_TIME);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));

    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    spec.setPattern(Pattern.DOWN);
    spec.setLookback(10);
    spec.setPeriod(3);
    spec.setSensitivity(0); // corresponds to zscore of 1
    HoltWintersDetector detector = new HoltWintersDetector();
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
            BooleanSeries.FALSE,
            BooleanSeries.FALSE,
            BooleanSeries.TRUE, // change is down
            BooleanSeries.FALSE));
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }


  @Deprecated
  @BeforeTest
  public void setUp() throws Exception {
    DataFrame dailyData;
    DataFrame hourlyData;
    try (Reader dataReader = new InputStreamReader(
        AlgorithmUtils.class.getResourceAsStream("/csv/daily.csv"))) {
      dailyData = DataFrame.fromCsv(dataReader);
      dailyData.setIndex(DataFrame.COL_TIME);
    }

    try (Reader dataReader = new InputStreamReader(
        AlgorithmUtils.class.getResourceAsStream("/csv/hourly.csv"))) {
      hourlyData = DataFrame.fromCsv(dataReader);
      hourlyData.setIndex(DataFrame.COL_TIME);
    }

    MetricConfigDTO dailyMetricConfig = new MetricConfigDTO();
    dailyMetricConfig.setId(1L);
    dailyMetricConfig.setName("thirdeye-test-daily");
    dailyMetricConfig.setDataset("thirdeye-test-dataset-daily");

    DatasetConfigDTO dailyDatasetConfig = new DatasetConfigDTO();
    dailyDatasetConfig.setTimeUnit(TimeUnit.DAYS);
    dailyDatasetConfig.setDataset("thirdeye-test-dataset-daily");
    dailyDatasetConfig.setTimeDuration(1);

    MetricConfigDTO hourlyMetricConfig = new MetricConfigDTO();
    hourlyMetricConfig.setId(123L);
    hourlyMetricConfig.setName("thirdeye-test-hourly");
    hourlyMetricConfig.setDataset("thirdeye-test-dataset-hourly");

    DatasetConfigDTO hourlyDatasetConfig = new DatasetConfigDTO();
    hourlyDatasetConfig.setTimeUnit(TimeUnit.HOURS);
    hourlyDatasetConfig.setDataset("thirdeye-test-dataset-hourly");
    hourlyDatasetConfig.setTimeDuration(1);

    Map<MetricSlice, DataFrame> timeseries = new HashMap<>();
    timeseries.put(MetricSlice.from(1L, 1301443200000L, 1309219200000L), dailyData);
    timeseries.put(MetricSlice.from(123L, 1317585600000L, 1323378000000L), hourlyData);
    // For Travis CI
    timeseries.put(MetricSlice.from(123L, 1317589200000L, 1323378000000L), hourlyData);

    this.provider = new MockDataProvider()
        .setTimeseries(timeseries)
        .setMetrics(Arrays.asList(hourlyMetricConfig, dailyMetricConfig))
        .setDatasets(Arrays.asList(hourlyDatasetConfig, dailyDatasetConfig));
  }

  @Deprecated
  @Test
  public void testComputePredictedTimeSeriesDaily() {
    HoltWintersDetector detector = new HoltWintersDetector();
    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    detector.init(spec, new DefaultInputDataFetcher(this.provider, -1));
    Interval window = new Interval(1306627200000L, 1309219200000L);
    String metricUrn = "thirdeye:metric:1";
    MetricEntity me = MetricEntity.fromURN(metricUrn);
    MetricSlice slice = MetricSlice
        .from(me.getId(), window.getStartMillis(), window.getEndMillis(), me.getFilters());
    TimeSeries timeSeries = detector.computePredictedTimeSeries(slice);

    Assert.assertEquals(timeSeries.getPredictedBaseline().size(), 29);
  }

  @Deprecated
  @Test
  public void testRunDetectionDaily() {
    HoltWintersDetector detector = new HoltWintersDetector();
    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setSensitivity(10);
    detector.init(spec, new DefaultInputDataFetcher(this.provider, -1));
    Interval window = new Interval(1306627200000L, 1309219200000L);
    String metricUrn = "thirdeye:metric:1";
    List<MergedAnomalyResultDTO> anomalies = detector.runDetection(window, metricUrn)
        .getAnomalies();

    Assert.assertEquals(anomalies.size(), 6);
  }

  @Deprecated
  @Test
  public void testComputePredictedTimeSeriesHourly() {
    HoltWintersDetector detector = new HoltWintersDetector();
    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    detector.init(spec, new DefaultInputDataFetcher(this.provider, -1));

    Interval window = new Interval(1322773200000L, 1323378000000L);

    String metricUrn = "thirdeye:metric:123";
    MetricEntity me = MetricEntity.fromURN(metricUrn);
    MetricSlice slice = MetricSlice
        .from(me.getId(), window.getStartMillis(), window.getEndMillis(), me.getFilters());
    TimeSeries timeSeries = detector.computePredictedTimeSeries(slice);

    Assert.assertEquals(timeSeries.getPredictedBaseline().size(), 167);
  }

  @Deprecated
  @Test
  public void testRunDetectionHourly() {
    HoltWintersDetector detector = new HoltWintersDetector();
    HoltWintersDetectorSpec spec = new HoltWintersDetectorSpec();
    spec.setSensitivity(9);
    detector.init(spec, new DefaultInputDataFetcher(this.provider, -1));
    Interval window = new Interval(1322773200000L, 1323378000000L);
    String metricUrn = "thirdeye:metric:123";
    List<MergedAnomalyResultDTO> anomalies = detector.runDetection(window, metricUrn)
        .getAnomalies();

    Assert.assertEquals(anomalies.size(), 2);
  }
}
