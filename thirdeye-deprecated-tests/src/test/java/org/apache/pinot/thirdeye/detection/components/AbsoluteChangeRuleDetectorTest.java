/*
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pinot.thirdeye.detection.components;

import static org.apache.pinot.thirdeye.spi.dataframe.DataFrame.COL_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.detection.DefaultInputDataFetcher;
import org.apache.pinot.thirdeye.detection.MockDataProvider;
import org.apache.pinot.thirdeye.detection.components.detectors.AbsoluteChangeRuleDetector;
import org.apache.pinot.thirdeye.detection.components.detectors.AbsoluteChangeRuleDetectorSpec;
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
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.model.TimeSeries;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.SimpleDataTable;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class AbsoluteChangeRuleDetectorTest {

  private static final long JANUARY_1_2021 = 1609459200000L;
  private static final long JANUARY_2_2021 = 1609545600000L;
  private static final long JANUARY_3_2021 = 1609632000000L;
  private static final long JANUARY_4_2021 = 1609718400000L;
  private static final long JANUARY_5_2021 = 1609804800000L;

  @Deprecated
  private DataProvider provider;
  @Deprecated
  private DataFrame data;

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
    DataFrame baselineDf = new DataFrame()
        .addSeries(DataFrame.COL_VALUE, 99., 199., 299., 399., 499.);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));
    timeSeriesMap.put(AnomalyDetectorV2.KEY_BASELINE, SimpleDataTable.fromDataFrame(baselineDf));

    AbsoluteChangeRuleDetectorSpec spec = new AbsoluteChangeRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    double absoluteChange = 50.0;
    spec.setAbsoluteChange(absoluteChange);
    AbsoluteChangeRuleDetector detector = new AbsoluteChangeRuleDetector();
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
    DoubleSeries expectedValueSeries = baselineDf.getDoubles(DataFrame.COL_VALUE);
    assertThat(outputValueSeries).isEqualTo(expectedValueSeries);

    DoubleSeries outputCurrentSeries = outputDf.getDoubles(DataFrame.COL_CURRENT);
    DoubleSeries expectedCurrentSeries = currentDf.getDoubles(DataFrame.COL_VALUE);
    assertThat(outputCurrentSeries).isEqualTo(expectedCurrentSeries);

    DoubleSeries outputUpperBoundSeries = outputDf.getDoubles(DataFrame.COL_UPPER_BOUND);
    DoubleSeries expectedUpperBoundSeries = baselineDf.getDoubles(COL_VALUE).add(absoluteChange);
    assertThat(outputUpperBoundSeries).isEqualTo(expectedUpperBoundSeries);

    DoubleSeries outputLowerBoundSeries = outputDf.getDoubles(DataFrame.COL_LOWER_BOUND);
    DoubleSeries expectedLowerBoundSeries = baselineDf.getDoubles(COL_VALUE).add(-absoluteChange);
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
    DataFrame baselineDf = new DataFrame()
        .addSeries(DataFrame.COL_VALUE, 99., 199., 299., 399., 499.);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));
    timeSeriesMap.put(AnomalyDetectorV2.KEY_BASELINE, SimpleDataTable.fromDataFrame(baselineDf));

    AbsoluteChangeRuleDetectorSpec spec = new AbsoluteChangeRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    double absoluteChange = 50.0;
    spec.setAbsoluteChange(absoluteChange);
    AbsoluteChangeRuleDetector detector = new AbsoluteChangeRuleDetector();
    detector.init(spec);

    AnomalyDetectorV2Result output = detector.runDetection(interval, timeSeriesMap);
    DataFrame outputDf = output.getDataFrame();
    LongSeries outputTimeSeries = outputDf.getLongs(DataFrame.COL_TIME);
    LongSeries expectedTimeSeries = LongSeries.buildFrom(JANUARY_3_2021,
        JANUARY_4_2021,
        JANUARY_5_2021);
    // fixme cyril test that out of frame anomaly value is null is better
    assertThat(outputTimeSeries).isEqualTo(expectedTimeSeries);
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
        .addSeries(DataFrame.COL_VALUE, 100., 200., 300., 400., 500.);
    DataFrame baselineDf = new DataFrame()
        .addSeries(DataFrame.COL_VALUE, 211, 199., 299., 311., 411.);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));
    timeSeriesMap.put(AnomalyDetectorV2.KEY_BASELINE, SimpleDataTable.fromDataFrame(baselineDf));

    AbsoluteChangeRuleDetectorSpec spec = new AbsoluteChangeRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    double absoluteChange = 50.0;
    spec.setAbsoluteChange(absoluteChange);
    AbsoluteChangeRuleDetector detector = new AbsoluteChangeRuleDetector();
    detector.init(spec);

    AnomalyDetectorV2Result output = detector.runDetection(interval, timeSeriesMap);
    //assert time fields
    assertThat(output.getTimeZone()).isEqualTo(AbstractSpec.DEFAULT_TIMEZONE);
    assertThat(output.getMonitoringGranularityPeriod().toStandardDuration().getMillis())
        .isEqualTo(Period.days(1).toStandardDuration().getMillis());

    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(DataFrame.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.buildFrom(
        BooleanSeries.TRUE,
        BooleanSeries.FALSE,
        BooleanSeries.FALSE,
        BooleanSeries.TRUE,
        BooleanSeries.TRUE);
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
        .addSeries(DataFrame.COL_VALUE, 100., 200., 300., 400., 500.);
    DataFrame baselineDf = new DataFrame()
        .addSeries(DataFrame.COL_VALUE, 211, 199., 299., 311., 411.);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));
    timeSeriesMap.put(AnomalyDetectorV2.KEY_BASELINE, SimpleDataTable.fromDataFrame(baselineDf));

    AbsoluteChangeRuleDetectorSpec spec = new AbsoluteChangeRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    spec.setPattern("UP");
    double absoluteChange = 50.0;
    spec.setAbsoluteChange(absoluteChange);
    AbsoluteChangeRuleDetector detector = new AbsoluteChangeRuleDetector();
    detector.init(spec);

    AnomalyDetectorV2Result output = detector.runDetection(interval, timeSeriesMap);
    //assert time fields
    assertThat(output.getTimeZone()).isEqualTo(AbstractSpec.DEFAULT_TIMEZONE);
    assertThat(output.getMonitoringGranularityPeriod().toStandardDuration().getMillis())
        .isEqualTo(Period.days(1).toStandardDuration().getMillis());

    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(DataFrame.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.buildFrom(
        BooleanSeries.FALSE, // change is down
        BooleanSeries.FALSE,
        BooleanSeries.FALSE,
        BooleanSeries.TRUE, // change is up
        BooleanSeries.TRUE); // change is up
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
        .addSeries(DataFrame.COL_VALUE, 100., 200., 300., 400., 500.);
    DataFrame baselineDf = new DataFrame()
        .addSeries(DataFrame.COL_VALUE, 211, 199., 299., 311., 411.);
    timeSeriesMap.put(AnomalyDetectorV2.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));
    timeSeriesMap.put(AnomalyDetectorV2.KEY_BASELINE, SimpleDataTable.fromDataFrame(baselineDf));

    AbsoluteChangeRuleDetectorSpec spec = new AbsoluteChangeRuleDetectorSpec();
    spec.setMonitoringGranularity("1_DAYS");
    spec.setPattern("DOWN");
    double absoluteChange = 50.0;
    spec.setAbsoluteChange(absoluteChange);
    AbsoluteChangeRuleDetector detector = new AbsoluteChangeRuleDetector();
    detector.init(spec);

    AnomalyDetectorV2Result output = detector.runDetection(interval, timeSeriesMap);
    //assert time fields
    assertThat(output.getTimeZone()).isEqualTo(AbstractSpec.DEFAULT_TIMEZONE);
    assertThat(output.getMonitoringGranularityPeriod().toStandardDuration().getMillis())
        .isEqualTo(Period.days(1).toStandardDuration().getMillis());

    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(DataFrame.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.buildFrom(
        BooleanSeries.TRUE, // change is down
        BooleanSeries.FALSE,
        BooleanSeries.FALSE,
        BooleanSeries.FALSE, // change is up
        BooleanSeries.FALSE); // change is up
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Deprecated
  @BeforeMethod
  public void beforeMethod() throws Exception {
    try (Reader dataReader = new InputStreamReader(
        AlgorithmUtils.class.getResourceAsStream("/csv/timeseries-4w.csv"))) {
      this.data = DataFrame.fromCsv(dataReader);
      this.data.setIndex(DataFrame.COL_TIME);
      this.data
          .addSeries(DataFrame.COL_TIME, this.data.getLongs(DataFrame.COL_TIME).multiply(1000));
    }

    MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    metricConfigDTO.setId(1L);
    metricConfigDTO.setName("thirdeye-test");
    metricConfigDTO.setDataset("thirdeye-test-dataset");

    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setTimeUnit(TimeUnit.HOURS);
    datasetConfigDTO.setDataset("thirdeye-test-dataset");
    datasetConfigDTO.setTimeDuration(1);

    Map<MetricSlice, DataFrame> timeseries = new HashMap<>();
    timeseries.put(MetricSlice.from(1L, 0L, 604800000L), this.data);
    timeseries.put(MetricSlice.from(1L, 604800000L, 1209600000L), this.data);
    timeseries.put(MetricSlice.from(1L, 1209600000L, 1814400000L), this.data);
    timeseries.put(MetricSlice.from(1L, 1814400000L, 2419200000L), this.data);

    this.provider = new MockDataProvider()
        .setTimeseries(timeseries)
        .setMetrics(Collections.singletonList(metricConfigDTO))
        .setDatasets(Collections.singletonList(datasetConfigDTO));
  }

  @Deprecated
  @Test
  public void testWeekOverWeekDifference() {
    AbsoluteChangeRuleDetector detector = new AbsoluteChangeRuleDetector();
    AbsoluteChangeRuleDetectorSpec spec = new AbsoluteChangeRuleDetectorSpec();
    double absoluteChange = 400;
    spec.setAbsoluteChange(absoluteChange);
    spec.setPattern("up");
    detector.init(spec, new DefaultInputDataFetcher(this.provider, -1));
    DetectionResult result = detector
        .runDetection(new Interval(1814400000L, 2419200000L), "thirdeye:metric:1");
    List<MergedAnomalyResultDTO> anomalies = result.getAnomalies();

    Assert.assertEquals(anomalies.size(), 1);
    Assert.assertEquals(anomalies.get(0).getStartTime(), 2372400000L);
    Assert.assertEquals(anomalies.get(0).getEndTime(), 2376000000L);
    TimeSeries ts = result.getTimeseries();
    checkAbsoluteUpperBounds(ts, absoluteChange);
    Assert.assertEquals(ts.getPredictedLowerBound(), DoubleSeries.zeros(ts.size()));
  }

  private void checkAbsoluteUpperBounds(TimeSeries ts, double absoluteChange) {
    for (int i = 0; i < ts.getDataFrame().size(); i++) {
      Assert.assertEquals(ts.getPredictedUpperBound().get(i),
          ts.getPredictedBaseline().get(i) + absoluteChange);
    }
  }
}
