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

import static ai.startree.thirdeye.plugins.postprocessor.TimeOfWeekPostProcessor.labelName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.startree.thirdeye.detectionpipeline.operator.AnomalyDetectorOperatorResult;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.annotations.Test;

public class TimeOfWeekPostProcessorTest {

  // only the timezone is used
  private static final Interval UTC_DETECTION_INTERVAL = new Interval(0L, 0L, DateTimeZone.UTC);
  private static final DateTimeZone PARIS_TIMEZONE = DateTimeZone.forID("Europe/Paris");

  private static final String RES_1_KEY = "res1";
  private static final String RES_2_KEY = "res2";

  @Test
  public void testPostProcessDayOfWeek() throws Exception {
    final TimeOfWeekPostProcessorSpec spec = new TimeOfWeekPostProcessorSpec();
    // lower case should pass
    final List<String> daysOfWeek = List.of("SATURDAY", "Sunday");
    spec.setDaysOfWeek(daysOfWeek);
    spec.setIgnore(true);

    final TimeOfWeekPostProcessor postProcessor = new TimeOfWeekPostProcessor(spec);

    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(List.of(sundayAnomaly(), mondayAnomaly()))
        .build();
    final OperatorResult res2 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(List.of(saturdayAnomaly()))
        .build();
    final Map<String, OperatorResult> resultMap = Map.of(RES_1_KEY, res1, RES_2_KEY, res2);

    final Map<String, OperatorResult> outputMap = postProcessor.postProcess(UTC_DETECTION_INTERVAL,
        resultMap);
    assertThat(outputMap).hasSize(2);
    final OperatorResult res1Out = outputMap.get(RES_1_KEY);
    final List<MergedAnomalyResultDTO> res1Anomalies = res1Out.getAnomalies();
    assertThat(res1Anomalies).hasSize(2);
    final MergedAnomalyResultDTO anomaly0 = res1Anomalies.get(0);
    final List<AnomalyLabelDTO> labels0 = anomaly0.getAnomalyLabels();
    assertThat(labels0).hasSize(1);
    final AnomalyLabelDTO anomaly0Label0 = labels0.get(0);
    assertThat(anomaly0Label0.isIgnore()).isTrue();
    assertThat(anomaly0Label0.getName()).isEqualTo(labelName(daysOfWeek, null, null));

    final MergedAnomalyResultDTO anomaly1 = res1Anomalies.get(1);
    assertThat(anomaly1.getAnomalyLabels()).isNull();

    final OperatorResult res2Out = outputMap.get(RES_2_KEY);
    final List<MergedAnomalyResultDTO> res2Anomalies = res2Out.getAnomalies();
    assertThat(res2Anomalies).hasSize(1);
    final MergedAnomalyResultDTO res2anomaly0 = res2Anomalies.get(0);
    assertThat(res2anomaly0.getAnomalyLabels()).hasSize(1);
    final AnomalyLabelDTO res2Label0 = res2anomaly0.getAnomalyLabels().get(0);
    assertThat(res2Label0.isIgnore()).isTrue();
    assertThat(res2Label0.getName()).isEqualTo(labelName(daysOfWeek, null, null));
  }

  @Test
  public void testPostProcessHourOfDay() throws Exception {
    final TimeOfWeekPostProcessorSpec spec = new TimeOfWeekPostProcessorSpec();
    final List<Integer> hourOfDay = List.of(1, 2, 3, 4);
    spec.setHoursOfDay(hourOfDay);
    spec.setIgnore(true);
    final TimeOfWeekPostProcessor postProcessor = new TimeOfWeekPostProcessor(spec);
    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(List.of(saturdayHour2Anomaly(), saturdayHour5Anomaly()))
        .build();
    final Map<String, OperatorResult> resultMap = Map.of(RES_1_KEY, res1);

    final Map<String, OperatorResult> outputMap = postProcessor.postProcess(UTC_DETECTION_INTERVAL,
        resultMap);
    final List<MergedAnomalyResultDTO> anomalies = outputMap.get(RES_1_KEY).getAnomalies();
    final AnomalyLabelDTO labelOfAnomaly0 = anomalies.get(0).getAnomalyLabels().get(0);
    assertThat(labelOfAnomaly0.isIgnore()).isTrue();
    assertThat(labelOfAnomaly0.getName()).isEqualTo(labelName(null, hourOfDay, null));

    assertThat(anomalies.get(1).getAnomalyLabels()).isNull();
  }

  @Test
  public void testPostProcessDayHoursOfWeek() throws Exception {
    final TimeOfWeekPostProcessorSpec spec = new TimeOfWeekPostProcessorSpec();
    final Map<String, List<Integer>> dayHoursOfWeek = Map.of("SATURDAY", List.of(1, 2, 3, 4));
    spec.setDayHoursOfWeek(dayHoursOfWeek);
    spec.setIgnore(true);
    final TimeOfWeekPostProcessor postProcessor = new TimeOfWeekPostProcessor(spec);
    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(List.of(saturdayHour2Anomaly(), saturdayHour5Anomaly()))
        .build();
    final Map<String, OperatorResult> resultMap = Map.of(RES_1_KEY, res1);

    final Map<String, OperatorResult> outputMap = postProcessor.postProcess(UTC_DETECTION_INTERVAL,
        resultMap);
    final List<MergedAnomalyResultDTO> anomalies = outputMap.get(RES_1_KEY).getAnomalies();
    final AnomalyLabelDTO labelOfAnomaly0 = anomalies.get(0).getAnomalyLabels().get(0);
    assertThat(labelOfAnomaly0.isIgnore()).isTrue();
    assertThat(labelOfAnomaly0.getName()).isEqualTo(labelName(null, null, dayHoursOfWeek));

    assertThat(anomalies.get(1).getAnomalyLabels()).isNull();
  }

  @Test
  public void testPostProcessHourOfDayWithIgnoreFalse() throws Exception {
    final TimeOfWeekPostProcessorSpec spec = new TimeOfWeekPostProcessorSpec();
    final List<Integer> hourOfDay = List.of(1, 2, 3, 4);
    spec.setHoursOfDay(hourOfDay);
    spec.setIgnore(false);
    final TimeOfWeekPostProcessor postProcessor = new TimeOfWeekPostProcessor(spec);
    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(List.of(saturdayHour2Anomaly(), saturdayHour5Anomaly()))
        .build();
    final Map<String, OperatorResult> resultMap = Map.of(RES_1_KEY, res1);

    final Map<String, OperatorResult> outputMap = postProcessor.postProcess(UTC_DETECTION_INTERVAL,
        resultMap);
    final List<MergedAnomalyResultDTO> anomalies = outputMap.get(RES_1_KEY).getAnomalies();
    final AnomalyLabelDTO labelOfAnomaly0 = anomalies.get(0).getAnomalyLabels().get(0);
    assertThat(labelOfAnomaly0.isIgnore()).isFalse();
    assertThat(labelOfAnomaly0.getName()).isEqualTo(labelName(null, hourOfDay, null));

    assertThat(anomalies.get(1).getAnomalyLabels()).isNull();
  }

  @Test
  public void testPostProcessEverythingEmpty() throws Exception {
    final TimeOfWeekPostProcessorSpec spec = new TimeOfWeekPostProcessorSpec();
    spec.setIgnore(true);
    final TimeOfWeekPostProcessor postProcessor = new TimeOfWeekPostProcessor(spec);
    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(List.of(saturdayHour2Anomaly(), saturdayHour5Anomaly()))
        .build();
    final Map<String, OperatorResult> resultMap = Map.of(RES_1_KEY, res1);

    final Map<String, OperatorResult> outputMap = postProcessor.postProcess(UTC_DETECTION_INTERVAL,
        resultMap);
    final List<MergedAnomalyResultDTO> anomalies = outputMap.get(RES_1_KEY).getAnomalies();
    assertThat(anomalies.get(0).getAnomalyLabels()).isNull();
    assertThat(anomalies.get(1).getAnomalyLabels()).isNull();
  }

  @Test
  public void testPostProcessWithTimezone() throws Exception {
    final TimeOfWeekPostProcessorSpec spec = new TimeOfWeekPostProcessorSpec();
    final List<Integer> hourOfDay = List.of(2);
    spec.setHoursOfDay(hourOfDay);
    spec.setIgnore(true);
    final TimeOfWeekPostProcessor postProcessor = new TimeOfWeekPostProcessor(spec);
    final OperatorResult res1 = AnomalyDetectorOperatorResult.builder()
        .setAnomalies(List.of(saturdayHour2InParisTimezone(), saturdayHour2Anomaly()))
        .build();
    final Map<String, OperatorResult> resultMap = Map.of(RES_1_KEY, res1);

    final Map<String, OperatorResult> outputMap = postProcessor.postProcess(
        new Interval(0L, 0L, PARIS_TIMEZONE),
        resultMap);
    final List<MergedAnomalyResultDTO> anomalies = outputMap.get(RES_1_KEY).getAnomalies();
    final AnomalyLabelDTO anomalyLabel0 = anomalies.get(0).getAnomalyLabels().get(0);
    assertThat(anomalyLabel0.isIgnore()).isTrue();
    assertThat(anomalyLabel0.getName()).isEqualTo(labelName(null, hourOfDay, null));

    assertThat(anomalies.get(1).getAnomalyLabels()).isNull();
  }

  @Test
  public void testInitInvalidHour() {
    final TimeOfWeekPostProcessorSpec spec = new TimeOfWeekPostProcessorSpec();
    // 24 is invalid
    final List<Integer> hourOfDay = List.of(1, 24);
    spec.setHoursOfDay(hourOfDay);
    assertThatThrownBy(() -> new TimeOfWeekPostProcessor(spec)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void testInitInvalidDay() {
    final TimeOfWeekPostProcessorSpec spec = new TimeOfWeekPostProcessorSpec();
    // invalid day
    final List<String> dayOfWeek = List.of("INVALIDAY");
    spec.setDaysOfWeek(dayOfWeek);
    assertThatThrownBy(() -> new TimeOfWeekPostProcessor(spec)).isInstanceOf(IllegalArgumentException.class);
  }

  private MergedAnomalyResultDTO saturdayHour2InParisTimezone() {
    final DateTime hour2Anomaly = new DateTime(2022, 9, 3, 2, 0, PARIS_TIMEZONE);
    final DateTime hour3Anomaly = new DateTime(2022, 9, 3, 3, 0, PARIS_TIMEZONE);
    return new MergedAnomalyResultDTO().setStartTime(hour2Anomaly.getMillis())
        .setEndTime(hour3Anomaly.getMillis());
  }

  private MergedAnomalyResultDTO saturdayHour2Anomaly() {
    final DateTime hour2Anomaly = new DateTime(2022, 9, 3, 2, 0, DateTimeZone.UTC);
    final DateTime hour3Anomaly = new DateTime(2022, 9, 3, 3, 0, DateTimeZone.UTC);
    return new MergedAnomalyResultDTO().setStartTime(hour2Anomaly.getMillis())
        .setEndTime(hour3Anomaly.getMillis());
  }

  private MergedAnomalyResultDTO saturdayHour5Anomaly() {
    final DateTime hour5Anomaly = new DateTime(2022, 9, 3, 5, 0, DateTimeZone.UTC);
    final DateTime hour6Anomaly = new DateTime(2022, 9, 3, 6, 0, DateTimeZone.UTC);
    return new MergedAnomalyResultDTO().setStartTime(hour5Anomaly.getMillis())
        .setEndTime(hour6Anomaly.getMillis());
  }

  private MergedAnomalyResultDTO saturdayAnomaly() {
    final DateTime sundayStart = new DateTime(2022, 9, 3, 0, 0, DateTimeZone.UTC);
    final DateTime mondayEnd = new DateTime(2022, 9, 4, 0, 0, DateTimeZone.UTC);
    return new MergedAnomalyResultDTO().setStartTime(sundayStart.getMillis())
        .setEndTime(mondayEnd.getMillis());
  }

  private MergedAnomalyResultDTO sundayAnomaly() {
    final DateTime sundayStart = new DateTime(2022, 9, 4, 0, 0, DateTimeZone.UTC);
    final DateTime mondayEnd = new DateTime(2022, 9, 5, 0, 0, DateTimeZone.UTC);
    return new MergedAnomalyResultDTO().setStartTime(sundayStart.getMillis())
        .setEndTime(mondayEnd.getMillis());
  }

  private MergedAnomalyResultDTO mondayAnomaly() {
    final DateTime sundayStart = new DateTime(2022, 9, 5, 0, 0, DateTimeZone.UTC);
    final DateTime mondayEnd = new DateTime(2022, 9, 6, 0, 0, DateTimeZone.UTC);
    return new MergedAnomalyResultDTO().setStartTime(sundayStart.getMillis())
        .setEndTime(mondayEnd.getMillis());
  }
}
