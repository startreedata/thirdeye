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
package ai.startree.thirdeye.plugins.postprocessor;

import static ai.startree.thirdeye.plugins.postprocessor.ThresholdPostProcessor.labelName;
import static ai.startree.thirdeye.spi.Constants.COL_CURRENT;
import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.Constants.COL_VALUE;
import static ai.startree.thirdeye.spi.detection.AbstractSpec.DEFAULT_METRIC;
import static ai.startree.thirdeye.spi.detection.AbstractSpec.DEFAULT_TIMESTAMP;
import static ai.startree.thirdeye.spi.detection.AnomalyDetector.KEY_CURRENT;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.detectionpipeline.operator.AnomalyDetectorOperatorResult;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ThresholdPostProcessorTest {

  // time is not used
  private static final Interval UTC_DETECTION_INTERVAL = new Interval(0L, 0L, DateTimeZone.UTC);

  private static final long JANUARY_1_2022 = 1640995200000L;
  private static final long JANUARY_2_2022 = 1641081600000L;
  private static final long JANUARY_3_2022 = 1641168000000L;
  private static final long JANUARY_4_2022 = 1641254400000L;
  private static final long JANUARY_5_2022 = 1641340800000L;
  private static final String RES_1_KEY = "res1";
  private static final DataFrame TEST_DF = new DataFrame().addSeries(COL_TIME,
          JANUARY_1_2022,
          JANUARY_2_2022,
          JANUARY_3_2022,
          JANUARY_4_2022).addSeries(COL_CURRENT, 5, 15, 85, 105)
      // not used but necessary to construct a TimeSeries
      .addSeries(COL_VALUE, 0, 0, 0, 0);
  private static final TimeSeries TEST_TIMESERIES = TimeSeries.fromDataFrame(TEST_DF);

  private AnomalyDTO january1Anomaly;
  private AnomalyDTO january2Anomaly;
  private AnomalyDTO january4Anomaly;

  @BeforeMethod
  public void initAnomalies() {
    january1Anomaly = new AnomalyDTO().setStartTime(JANUARY_1_2022)
        .setEndTime(JANUARY_2_2022);
    january2Anomaly = new AnomalyDTO().setStartTime(JANUARY_2_2022)
        .setEndTime(JANUARY_3_2022);
    january4Anomaly = new AnomalyDTO().setStartTime(JANUARY_4_2022)
        .setEndTime(JANUARY_5_2022);
  }

  @DataProvider(name = "defaultInputTestCases")
  public static Object[][] defaultInputTestCases() {
    final Object[] minMax = {true, 10, 100, List.of(true, false, true)};
    final Object[] minMaxIgnoreFalse = {false, 10, 100, List.of(true, false, true)};
    final Object[] maxOnly = {true, -1, 100, List.of(false, false, true)};
    final Object[] minOnly = {true, 10, -1, List.of(true, false, false)};
    final Object[] noThreshold = {true, -1, -1, List.of(false, false, false)};

    return new Object[][]{minMax, minMaxIgnoreFalse, maxOnly, minOnly, noThreshold};
  }

  @Test(dataProvider = "defaultInputTestCases")
  public void testPostProcessWithDefaultInput(final boolean ignoreMode, final double min,
      final double max, final List<Boolean> expectedIsLabelled) throws Exception {
    final ThresholdPostProcessorSpec spec = new ThresholdPostProcessorSpec();
    final String metric1 = "Metric1";
    spec.setMin(min);
    spec.setMax(max);
    spec.setValueName(metric1);
    spec.setIgnore(ignoreMode);

    final ThresholdPostProcessor postProcessor = new ThresholdPostProcessor(spec);

    final List<AnomalyDTO> anomalies = List.of(january1Anomaly,
        january2Anomaly,
        january4Anomaly);
    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setTimeseries(TEST_TIMESERIES)
        .setAnomalies(anomalies)
        .build();
    final Map<String, OperatorResult> resultMap = new HashMap<>();
    resultMap.put(RES_1_KEY, res1);
    final Map<String, OperatorResult> outputResultMap = postProcessor.postProcess(
        UTC_DETECTION_INTERVAL,
        resultMap);

    assertThat(resultMap.size()).isEqualTo(1);
    final OperatorResult outputResult = outputResultMap.get(RES_1_KEY);
    final List<AnomalyDTO> outputAnomalies = outputResult.getAnomalies();
    assertThat(outputAnomalies).hasSize(3);

    for (int i = 0; i < outputAnomalies.size(); i++) {
      final AnomalyDTO anomaly = outputAnomalies.get(i);
      final boolean isLabeled = expectedIsLabelled.get(i);
      if (isLabeled) {
        final List<AnomalyLabelDTO> anomalyLabels = anomaly.getAnomalyLabels();
        assertThat(anomalyLabels).hasSize(1);
        final AnomalyLabelDTO label = anomalyLabels.get(0);
        assertThat(label.isIgnore()).isEqualTo(ignoreMode);
        assertThat(label.getName()).isEqualTo(labelName(min, max, metric1));
      } else {
        assertThat(anomaly.getAnomalyLabels()).isNull();
      }
    }
  }

  @DataProvider(name = "customInputTestCases")
  public static Object[][] customInputDataTableTestCases() {
    final DataFrame dfWithDefaultColNames = new DataFrame().addSeries(DEFAULT_TIMESTAMP,
            JANUARY_1_2022,
            JANUARY_2_2022,
            JANUARY_3_2022,
            JANUARY_4_2022)
        .addSeries(DEFAULT_METRIC, 1.1, 2.5, 0.5, 1.4);
    final DataTable dtWithDefaultColNames = SimpleDataTable.fromDataFrame(dfWithDefaultColNames);
    final Object[] withDefaultColumnNames = {null, null, dtWithDefaultColNames,
        List.of(false, true, false)};

    final DataFrame dfWithCustomColNames = new DataFrame().addSeries("ts",
            JANUARY_1_2022,
            JANUARY_2_2022,
            JANUARY_3_2022,
            JANUARY_4_2022)
        .addSeries("met", 1.1, 2.5, 0.5, 1.4);
    final DataTable dtWithCustomColNames = SimpleDataTable.fromDataFrame(dfWithCustomColNames);
    final Object[] withCustomColumnNames = {"ts", "met", dtWithCustomColNames,
        List.of(false, true, false)};

    final DataFrame detectionResultDf = new DataFrame().addSeries(COL_TIME,
            JANUARY_1_2022,
            JANUARY_2_2022,
            JANUARY_3_2022,
            JANUARY_4_2022)
        .addSeries(COL_CURRENT, 1.1, 2.5, 0.5, 1.4)
        // not used in test
        .addSeries(COL_VALUE, 0., 0., 0., 0.);
    final OperatorResult detectionResultWithCustomColName = AnomalyDetectorOperatorResult.builder()
        .setTimeseries(TimeSeries.fromDataFrame(detectionResultDf))
        .build();
    final Object[] withDetectionResult = {COL_TIME, COL_CURRENT, detectionResultWithCustomColName,
        List.of(false, true, false)};

    return new Object[][]{withDefaultColumnNames, withCustomColumnNames, withDetectionResult};
  }

  @Test(dataProvider = "customInputTestCases")
  public void testPostProcessWithCustomInput(final @Nullable String timestampColumn,
      final @Nullable String metricColum, final OperatorResult customInput,
      final List<Boolean> expectedIsLabelled) throws Exception {
    final ThresholdPostProcessorSpec spec = new ThresholdPostProcessorSpec();
    final String metric1 = "Metric1";
    final Double min = 1.;
    final Double max = 2.;
    spec.setMin(min);
    spec.setMax(max);
    spec.setValueName(metric1);
    spec.setIgnore(true);
    spec.setTimestamp(timestampColumn);
    spec.setMetric(metricColum);

    final ThresholdPostProcessor postProcessor = new ThresholdPostProcessor(spec);

    final List<AnomalyDTO> anomalies = List.of(january1Anomaly,
        january2Anomaly,
        january4Anomaly);
    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setTimeseries(TEST_TIMESERIES)
        .setAnomalies(anomalies)
        .build();
    final Map<String, OperatorResult> resultMap = new HashMap<>();
    resultMap.put(RES_1_KEY, res1);
    resultMap.put(KEY_CURRENT, customInput);
    final Map<String, OperatorResult> outputResultMap = postProcessor.postProcess(
        UTC_DETECTION_INTERVAL,
        resultMap);

    assertThat(resultMap.size()).isEqualTo(1);
    final OperatorResult outputResult = outputResultMap.get(RES_1_KEY);
    final List<AnomalyDTO> outputAnomalies = outputResult.getAnomalies();
    assertThat(outputAnomalies).hasSize(3);

    for (int i = 0; i < outputAnomalies.size(); i++) {
      final AnomalyDTO anomaly = outputAnomalies.get(i);
      final boolean isLabeled = expectedIsLabelled.get(i);
      if (isLabeled) {
        final List<AnomalyLabelDTO> anomalyLabels = anomaly.getAnomalyLabels();
        assertThat(anomalyLabels).hasSize(1);
        final AnomalyLabelDTO label = anomalyLabels.get(0);
        assertThat(label.isIgnore()).isEqualTo(true);
        assertThat(label.getName()).isEqualTo(labelName(min, max, metric1));
      } else {
        assertThat(anomaly.getAnomalyLabels()).isNull();
      }
    }
  }
}
