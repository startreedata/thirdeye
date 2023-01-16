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

import static ai.startree.thirdeye.spi.Constants.COL_CURRENT;
import static ai.startree.thirdeye.spi.Constants.COL_EVENT_END;
import static ai.startree.thirdeye.spi.Constants.COL_EVENT_NAME;
import static ai.startree.thirdeye.spi.Constants.COL_EVENT_START;
import static ai.startree.thirdeye.spi.Constants.COL_TIME;
import static ai.startree.thirdeye.spi.Constants.COL_VALUE;
import static ai.startree.thirdeye.spi.detection.AnomalyDetector.KEY_CURRENT_EVENTS;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.detectionpipeline.operator.AnomalyDetectorOperatorResult;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EventPostProcessorTest {

  private static final Interval UTC_DETECTION_INTERVAL = new Interval(0L, 0L, DateTimeZone.UTC);

  private static final String RES_1_KEY = "res1";

  private static final long HALF_DAY_MILLIS = 43_200_000L;
  private static final long JANUARY_1_2022 = 1640995200000L;
  private static final long JANUARY_2_2022 = 1641081600000L;
  private static final long JANUARY_3_2022 = 1641168000000L;
  private static final long JANUARY_4_2022 = 1641254400000L;
  private static final long JANUARY_5_2022 = 1641340800000L;

  private static final DataFrame TEST_DF = new DataFrame().addSeries(COL_TIME,
          JANUARY_1_2022,
          JANUARY_2_2022,
          JANUARY_3_2022,
          JANUARY_4_2022).addSeries(COL_CURRENT, 5, 15, 85, 105)
      // not used but necessary to construct a TimeSeries
      .addSeries(COL_VALUE, 0, 0, 0, 0);
  private static final TimeSeries TEST_TIMESERIES = TimeSeries.fromDataFrame(TEST_DF);

  private MergedAnomalyResultDTO january1Anomaly;
  private MergedAnomalyResultDTO january2Anomaly;
  private MergedAnomalyResultDTO january4Anomaly;

  @BeforeMethod
  public void initAnomalies() {
    january1Anomaly = new MergedAnomalyResultDTO().setStartTime(JANUARY_1_2022)
        .setEndTime(JANUARY_2_2022);
    january2Anomaly = new MergedAnomalyResultDTO().setStartTime(JANUARY_2_2022)
        .setEndTime(JANUARY_3_2022);
    january4Anomaly = new MergedAnomalyResultDTO().setStartTime(JANUARY_4_2022)
        .setEndTime(JANUARY_5_2022);
  }

  @Test
  public void testPostProcessNoEventsInput() throws Exception {
    final EventPostProcessorSpec spec = new EventPostProcessorSpec();
    final EventPostProcessor postProcessor = new EventPostProcessor(spec);

    final List<MergedAnomalyResultDTO> inputAnomalies = List.of(this.january1Anomaly,
        january2Anomaly, january4Anomaly);
    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(inputAnomalies)
        .build();

    final Map<String, OperatorResult> resultMap = new HashMap<>(Map.of(RES_1_KEY, res1));
    final Map<String, OperatorResult> outputMap = postProcessor.postProcess(UTC_DETECTION_INTERVAL,
        resultMap);
    assertThat(outputMap).hasSize(1);
    final OperatorResult res1Out = outputMap.get(RES_1_KEY);
    final List<MergedAnomalyResultDTO> res1Anomalies = res1Out.getAnomalies();
    assertThat(res1Anomalies).isEqualTo(inputAnomalies);
  }

  @Test
  public void testPostProcessNoEventsInDataFrame() throws Exception {
    final EventPostProcessorSpec spec = new EventPostProcessorSpec();
    final EventPostProcessor postProcessor = new EventPostProcessor(spec);
    final List<MergedAnomalyResultDTO> inputAnomalies = List.of(this.january1Anomaly,
        january2Anomaly, january4Anomaly);
    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(inputAnomalies)
        .build();

    final Map<String, OperatorResult> resultMap = new HashMap<>(
        Map.of(RES_1_KEY, res1,
            KEY_CURRENT_EVENTS, SimpleDataTable.fromDataFrame(new DataFrame())));
    final Map<String, OperatorResult> outputMap = postProcessor.postProcess(UTC_DETECTION_INTERVAL,
        resultMap);
    assertThat(outputMap).hasSize(1);
    final OperatorResult res1Out = outputMap.get(RES_1_KEY);
    final List<MergedAnomalyResultDTO> res1Anomalies = res1Out.getAnomalies();
    assertThat(res1Anomalies).isEqualTo(inputAnomalies);
  }

  @DataProvider(name = "eventCases")
  public static Object[][] eventCases() {
    final Object[] jan1stEventFullMatch = {JANUARY_1_2022, JANUARY_2_2022, null, null,
        false, List.of(true, false, false)};
    final Object[] jan1stEventFullMatchIgnoreTrue = {JANUARY_1_2022, JANUARY_2_2022, null, null,
        true, List.of(true, false, false)};
    final Object[] jan1stEventPartialOverlap = {JANUARY_1_2022 - HALF_DAY_MILLIS,
        JANUARY_2_2022 - HALF_DAY_MILLIS, null, null,
        true, List.of(true, false, false)};
    final Object[] eventIncludesAnomaliesTimeframes = {JANUARY_1_2022 - HALF_DAY_MILLIS,
        JANUARY_3_2022 + HALF_DAY_MILLIS, null, null,
        true, List.of(true, true, false)};
    final Object[] noEventMatch = {JANUARY_3_2022, JANUARY_4_2022, null, null,
        true, List.of(false, false, false)};
    final Object[] eventLeftSpillover = {JANUARY_2_2022, JANUARY_3_2022, Period.hours(3), null,
        true, List.of(true, true, false)};
    final Object[] eventRightSpillover = {JANUARY_1_2022, JANUARY_2_2022, null, Period.days(1),
        true, List.of(true, true, false)};
    final Object[] eventLeftRightSpillover = {JANUARY_2_2022, JANUARY_3_2022, Period.hours(3),
        Period.hours(36),
        true, List.of(true, true, true)};

    return new Object[][]{jan1stEventFullMatch, jan1stEventFullMatchIgnoreTrue,
        jan1stEventPartialOverlap, eventIncludesAnomaliesTimeframes, noEventMatch,
        eventLeftSpillover, eventRightSpillover, eventLeftRightSpillover};
  }

  @Test(dataProvider = "eventCases")
  public void testPostProcessWithEvent(final long eventStart, final long eventEnd,
      final Period lookBefore, final Period lookAfter, final boolean isIgnore,
      final List<Boolean> isLabelled)
      throws Exception {
    final EventPostProcessorSpec spec = new EventPostProcessorSpec();
    if (lookBefore != null) {
      spec.setBeforeEventMargin(lookBefore.toString());
    }
    if (lookAfter != null) {
      spec.setAfterEventMargin(lookAfter.toString());
    }
    spec.setIgnore(isIgnore);
    final EventPostProcessor postProcessor = new EventPostProcessor(spec);
    final List<MergedAnomalyResultDTO> inputAnomalies = List.of(this.january1Anomaly,
        january2Anomaly, january4Anomaly);
    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(inputAnomalies)
        .build();

    final String eventName = "SPECIAL_EVENT";
    final DataFrame eventsDataFrame = new DataFrame()
        .addSeries(COL_EVENT_START, eventStart)
        .addSeries(COL_EVENT_END, eventEnd)
        .addSeries(COL_EVENT_NAME, eventName);
    final Map<String, OperatorResult> resultMap = new HashMap<>(
        Map.of(RES_1_KEY, res1,
            KEY_CURRENT_EVENTS, SimpleDataTable.fromDataFrame(eventsDataFrame)));
    final Map<String, OperatorResult> outputMap = postProcessor.postProcess(UTC_DETECTION_INTERVAL,
        resultMap);
    assertThat(outputMap).hasSize(1);
    final OperatorResult res1Out = outputMap.get(RES_1_KEY);
    final List<MergedAnomalyResultDTO> res1Anomalies = res1Out.getAnomalies();
    assertThat(res1Anomalies).isNotNull();
    assertThat(res1Anomalies).hasSize(3);
    for (int i = 0; i < 3; i++) {
      final boolean expectedIsLabeled = isLabelled.get(i);
      final List<AnomalyLabelDTO> labels = res1Anomalies.get(i).getAnomalyLabels();
      if (expectedIsLabeled) {
        assertThat(labels).hasSize(1);
        final AnomalyLabelDTO label = labels.get(0);
        assertThat(label.isIgnore()).isEqualTo(isIgnore);
        assertThat(label.getName()).isEqualTo("Anomaly happens during " + eventName + " event");
      } else {
        assertThat(labels).isNull();
      }
    }
  }
}
