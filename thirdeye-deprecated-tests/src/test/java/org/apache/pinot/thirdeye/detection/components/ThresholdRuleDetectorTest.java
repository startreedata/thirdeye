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

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.detection.DefaultInputDataFetcher;
import org.apache.pinot.thirdeye.detection.MockDataProvider;
import org.apache.pinot.thirdeye.detection.components.detectors.ThresholdRuleDetector;
import org.apache.pinot.thirdeye.detection.components.detectors.ThresholdRuleDetectorSpec;
import org.apache.pinot.thirdeye.spi.dataframe.BooleanSeries;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetector;
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

public class ThresholdRuleDetectorTest {

  private static final long JANUARY_1_2021 = 1609459200000L;
  private static final long JANUARY_2_2021 = 1609545600000L;
  private static final long JANUARY_3_2021 = 1609632000000L;
  private static final long JANUARY_4_2021 = 1609718400000L;
  private static final long JANUARY_5_2021 = 1609804800000L;

  @Deprecated
  private DataProvider testDataProvider;

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

  @Deprecated
  @BeforeMethod
  public void beforeMethod() {
    Map<MetricSlice, DataFrame> timeSeries = new HashMap<>();
    timeSeries.put(MetricSlice.from(123L, 0, 10),
        new DataFrame()
            .addSeries(DataFrame.COL_VALUE, 0, 100, 200, 500, 1000)
            .addSeries(DataFrame.COL_TIME, 0, 2, 4, 6, 8));
    timeSeries.put(MetricSlice.from(123L, 1546214400000L, 1551398400000L),
        new DataFrame()
            .addSeries(DataFrame.COL_TIME, 1546214400000L, 1548892800000L, 1551312000000L)
            .addSeries(DataFrame.COL_VALUE, 100, 200, 300));

    MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    metricConfigDTO.setId(123L);
    metricConfigDTO.setName("thirdeye-test");
    metricConfigDTO.setDataset("thirdeye-test-dataset");

    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setId(124L);
    datasetConfigDTO.setDataset("thirdeye-test-dataset");
    datasetConfigDTO.setTimeDuration(2);
    datasetConfigDTO.setTimeUnit(TimeUnit.MILLISECONDS);
    datasetConfigDTO.setTimezone("UTC");

    AlertDTO alertDTO = new AlertDTO();
    alertDTO.setId(125L);
    Map<String, Object> detectorSpecs = new HashMap<>();
    detectorSpecs.put("className", ThresholdRuleDetector.class.getName());
    Map<String, Object> properties = new HashMap<>();
    properties.put("metricUrn", "thirdeye:metric:123");
    properties.put("detector", "$threshold");
    alertDTO.setProperties(properties);
    Map<String, Object> componentSpecs = new HashMap<>();
    componentSpecs.put("threshold", detectorSpecs);
    alertDTO.setComponentSpecs(componentSpecs);

    this.testDataProvider = new MockDataProvider()
        .setMetrics(Collections.singletonList(metricConfigDTO))
        .setDatasets(Collections.singletonList(datasetConfigDTO))
        .setTimeseries(timeSeries);
  }

  @Deprecated
  @Test
  public void testThresholdAlgorithmRun() throws DetectorException {
    AnomalyDetector thresholdAlgorithm = new ThresholdRuleDetector();
    ThresholdRuleDetectorSpec spec = new ThresholdRuleDetectorSpec();
    spec.setMin(100);
    spec.setMax(500);
    thresholdAlgorithm.init(spec, new DefaultInputDataFetcher(testDataProvider, -1));
    DetectionResult result = thresholdAlgorithm
        .runDetection(new Interval(0, 10), "thirdeye:metric:123");
    List<MergedAnomalyResultDTO> anomalies = result.getAnomalies();
    Assert.assertEquals(anomalies.size(), 2);
    Assert.assertEquals(anomalies.get(0).getStartTime(), 0);
    Assert.assertEquals(anomalies.get(0).getEndTime(), 2);
    Assert.assertEquals(anomalies.get(1).getStartTime(), 8);
    Assert.assertEquals(anomalies.get(1).getEndTime(), 10);

    TimeSeries ts = result.getTimeseries();
    Assert.assertEquals(ts.getPredictedUpperBound(), DoubleSeries.fillValues(ts.size(), 500));
    Assert.assertEquals(ts.getPredictedLowerBound(), DoubleSeries.fillValues(ts.size(), 100));
    Assert.assertEquals(ts.getPredictedBaseline().values(),
        new double[]{100, 100L, 200L, 500L, 500L});
  }

  @Deprecated
  @Test
  public void testMonthlyDetectionThreshold() throws DetectorException {
    AnomalyDetector thresholdRule = new ThresholdRuleDetector();
    ThresholdRuleDetectorSpec spec = new ThresholdRuleDetectorSpec();
    spec.setMin(200);
    spec.setMonitoringGranularity("1_MONTHS");
    thresholdRule.init(spec, new DefaultInputDataFetcher(testDataProvider, -1));
    List<MergedAnomalyResultDTO> anomalies = thresholdRule
        .runDetection(new Interval(1546214400000L, 1551398400000L), "thirdeye:metric:123")
        .getAnomalies();
    Assert.assertEquals(anomalies.size(), 1);
    Assert.assertEquals(anomalies.get(0).getStartTime(), 1546214400000L);
    Assert.assertEquals(anomalies.get(0).getEndTime(), 1548892800000L);
  }

  @Deprecated
  @Test
  public void testMonthlyDetectionThresholdMax() throws DetectorException {
    AnomalyDetector thresholdRule = new ThresholdRuleDetector();
    ThresholdRuleDetectorSpec spec = new ThresholdRuleDetectorSpec();
    spec.setMax(200);
    spec.setMonitoringGranularity("1_MONTHS");
    thresholdRule.init(spec, new DefaultInputDataFetcher(testDataProvider, -1));
    List<MergedAnomalyResultDTO> anomalies = thresholdRule
        .runDetection(new Interval(1546214400000L, 1551398400000L), "thirdeye:metric:123")
        .getAnomalies();
    Assert.assertEquals(anomalies.size(), 1);
    Assert.assertEquals(anomalies.get(0).getStartTime(), 1551312000000L);
    Assert.assertEquals(anomalies.get(0).getEndTime(), 1553731200000L);
  }
}
