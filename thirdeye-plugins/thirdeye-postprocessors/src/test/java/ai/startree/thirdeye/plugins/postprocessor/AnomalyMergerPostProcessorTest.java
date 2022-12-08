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
package ai.startree.thirdeye.plugins.postprocessor;

import static ai.startree.thirdeye.plugins.postprocessor.AnomalyMergerPostProcessor.DEFAULT_MERGE_MAX_GAP;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.chrono.ISOChronology;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnomalyMergerPostProcessorTest {

  public static final long ALERT_ID = 1L;
  private static final long JANUARY_1_2021_01H = 1609462800_000L;
  private static final long JANUARY_1_2021_02H = 1609466400_000L;
  private static final long JANUARY_1_2021_03H = 1609470000000L;
  private static long ANOMALY_ID = 1000L;
  private MergedAnomalyResultManager mergedAnomalyResultManager;
  private AnomalyMergerPostProcessorSpec detectionSpec;
  private AnomalyMergerPostProcessor detectionMerger;

  private static EnumerationItemDTO newEnumerationItemRef(final long enumerationItemId) {
    final EnumerationItemDTO enumerationItemDTO = new EnumerationItemDTO();
    enumerationItemDTO.setId(enumerationItemId);
    return enumerationItemDTO;
  }

  private static MergedAnomalyResultDTO newAnomaly(final long startDate, final long endDate) {
    final MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setStartTime(startDate);
    anomaly.setEndTime(endDate);
    return anomaly;
  }

  private static MergedAnomalyResultDTO existingAnomaly(final long startDate, final long endDate) {
    final MergedAnomalyResultDTO anomaly = newAnomaly(startDate, endDate);
    anomaly.setId(++ANOMALY_ID);
    return anomaly;
  }

  private static long plusMin(final long startDate, final int minutes) {
    return new DateTime(startDate, DateTimeZone.UTC).plus(Period.minutes(minutes)).getMillis();
  }

  public static boolean isSameAnomaly(MergedAnomalyResultDTO a1, MergedAnomalyResultDTO a2) {
    return Objects.equals(a1.getId(), a2.getId()) && a1.getStartTime() == a2.getStartTime()
        && a1.getEndTime() == a2.getEndTime();
  }

  @BeforeMethod
  public void setUp() {
    mergedAnomalyResultManager = mock(MergedAnomalyResultManager.class);
    detectionSpec = new AnomalyMergerPostProcessorSpec().setMergedAnomalyResultManager(
        mergedAnomalyResultManager).setAlertId(ALERT_ID).setUsage(DetectionPipelineUsage.DETECTION);
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    detectionMerger.setChronology(ISOChronology.getInstanceUTC());
  }

  @Test
  public void testPrepareSortedAnomalyList() {
    assertThat(AnomalyMergerPostProcessor.combineAndSort(emptyList(), emptyList())).isEqualTo(
        emptyList());

    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final MergedAnomalyResultDTO new2 = newAnomaly(JANUARY_1_2021_01H,
        plusMin(JANUARY_1_2021_01H, 10));
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    final MergedAnomalyResultDTO existing2 = existingAnomaly(JANUARY_1_2021_01H,
        plusMin(JANUARY_1_2021_01H, 10));

    assertThat(
        AnomalyMergerPostProcessor.combineAndSort(emptyList(), singletonList(existing1))).isEqualTo(
        singletonList(existing1));

    assertThat(
        AnomalyMergerPostProcessor.combineAndSort(singletonList(new1), emptyList())).isEqualTo(
        singletonList(new1));

    assertThat(AnomalyMergerPostProcessor.combineAndSort(singletonList(new1),
        singletonList(existing1))).isEqualTo(List.of(existing1, new1));

    assertThat(AnomalyMergerPostProcessor.combineAndSort(List.of(new1, new2),
        List.of(existing1, existing2))).isEqualTo(List.of(existing1, new1, existing2, new2));
  }

  @Test
  public void testEmptyMergeAndSave() {
    assertThat(detectionMerger.merge(emptyList())).isEqualTo(List.of());
  }

  @Test
  public void testSingleAnomalyNoMerge() {
    when(mergedAnomalyResultManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> emptyList());
    final MergedAnomalyResultDTO newAnomaly = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final List<MergedAnomalyResultDTO> output = detectionMerger.merge(singletonList(newAnomaly));
    assertThat(output).isEqualTo(List.of(newAnomaly));
  }

  @Test
  public void testSingleAnomalyMergeWithExisting() {
    final MergedAnomalyResultDTO existingAnomaly = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    when(mergedAnomalyResultManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> singletonList(existingAnomaly));

    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H);
    final List<MergedAnomalyResultDTO> output = detectionMerger.merge(List.of(new1));
    assertThat(output).isEqualTo(List.of(existingAnomaly));
    assertThat(existingAnomaly.getChildren().size()).isEqualTo(2);
  }

  @Test
  public void testSingleAnomalyNoMergeWithExistingIfUsageIsEvaluate() {
    final MergedAnomalyResultDTO existingAnomaly = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    when(mergedAnomalyResultManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> singletonList(existingAnomaly));

    detectionSpec.setUsage(DetectionPipelineUsage.EVALUATION);
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H);
    final List<MergedAnomalyResultDTO> output = detectionMerger.merge(List.of(new1));
    assertThat(output).isEqualTo(List.of(new1));
    assertThat(new1.getChildren().size()).isEqualTo(0);
  }

  @Test
  public void testMergeAllNewAndExistingAnomalies() {
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final MergedAnomalyResultDTO new2 = newAnomaly(JANUARY_1_2021_01H,
        plusMin(JANUARY_1_2021_01H, 10));
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    final MergedAnomalyResultDTO existing2 = existingAnomaly(JANUARY_1_2021_01H,
        plusMin(JANUARY_1_2021_01H, 10));
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1, new2), List.of(existing1, existing2));
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);
    assertThat(merged.size()).isEqualTo(1);

    assertThat(isSameAnomaly(merged.get(0), existing1)).isTrue();
  }

  @Test
  public void testMergeNoMergeWhenAnomaliesSpacedByMoreThanMergeGap() {
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    // new anomaly that happens after the merge gap
    final long afterMergeGapStart = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(
        DEFAULT_MERGE_MAX_GAP).plus(1).getMillis();
    final long afterMergeGapEnd = plusMin(afterMergeGapStart, 60);
    final MergedAnomalyResultDTO new2 = newAnomaly(afterMergeGapStart, afterMergeGapEnd);
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1, new2), List.of());
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(2);
    assertThat(merged.get(0)).isEqualTo(new1);
    assertThat(merged.get(1)).isEqualTo(new2);
  }

  @Test
  public void testMergeNoMergeWhenAnomaliesByMoreThanMergeGapWithExisting() {
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    // new anomaly that happens before the merge gap
    final long afterMergeGapStart = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(
        DEFAULT_MERGE_MAX_GAP).plus(1).getMillis();
    final long afterMergeGapEnd = plusMin(afterMergeGapStart, 60);
    final MergedAnomalyResultDTO new1 = newAnomaly(afterMergeGapStart, afterMergeGapEnd);
    final long afterMergeGapStart2 = new DateTime(afterMergeGapEnd, DateTimeZone.UTC).plus(
        DEFAULT_MERGE_MAX_GAP).plus(1).getMillis();
    final long afterMergeGapEnd2 = plusMin(afterMergeGapStart2, 60);
    final MergedAnomalyResultDTO new2 = newAnomaly(afterMergeGapStart2, afterMergeGapEnd2);
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1, new2), List.of(existing1));
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(3);
    assertThat(merged.get(0)).isEqualTo(existing1);
    assertThat(merged.get(1)).isEqualTo(new1);
    assertThat(merged.get(2)).isEqualTo(new2);
  }

  @Test
  public void testMergeNewInExisting() {
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    final long expectedId = existing1.getId();
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final MergedAnomalyResultDTO new1 = newAnomaly(
        new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(1)).getMillis(),
        newEndTime);
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
    MergedAnomalyResultDTO parent = merged.get(0);
    assertThat(parent.getStartTime()).isEqualTo(JANUARY_1_2021_01H);
    assertThat(parent.getEndTime()).isEqualTo(newEndTime);
    assertThat(parent.getId()).isEqualTo(expectedId);
    assertThat(parent.getChildren().size()).isEqualTo(2);
  }

  @Test
  public void testMergeNoMergeWithZeroMergeGapPeriod() {
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime);
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    detectionSpec.setMergeMaxGap(Period.ZERO.toString());
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    detectionMerger.setChronology(ISOChronology.getInstanceUTC());
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(2);
  }

  @Test
  public void testMergeNoMergeWithLabelsWithIgnoreDefaultDifferent() {
    // never merge anomalies with different ignore default
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H).setAnomalyLabels(List.of(new AnomalyLabelDTO().setIgnore(true)));
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime).setAnomalyLabels(
        List.of(new AnomalyLabelDTO().setIgnore(false)));
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(2);
  }

  @Test
  public void testMergeDoMergeWithLabelsWithIgnoreDefaultSameFalse() {
    // merge anomalies with ignore default both false
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H)
        // ignore=false if there is no label
        .setAnomalyLabels(List.of());
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime).setAnomalyLabels(
        List.of(new AnomalyLabelDTO().setIgnore(false)));
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
  }

  @Test
  public void testMergeDoMergeWithLabelsWithIgnoreDefaultSameTrue() {
    // merge anomalies with ignore default both true
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H).setAnomalyLabels(List.of(new AnomalyLabelDTO().setIgnore(true)));
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime).setAnomalyLabels(
        List.of(new AnomalyLabelDTO().setIgnore(true)));
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
  }

  @Test
  public void testMergeLabelsAreMergedCorrectly() {
    // check that labels that are equal are not duplicated in the merger
    final AnomalyLabelDTO christmasEveLabel = new AnomalyLabelDTO().setName("Holiday")
        .setMetadata(Map.of("eventName", "Christmas Eve"))
        .setIgnore(false);
    final AnomalyLabelDTO coldStartLabel = new AnomalyLabelDTO().setName("ColdStart")
        .setIgnore(true);
    final List<AnomalyLabelDTO> existingLabels = List.of(christmasEveLabel, coldStartLabel);
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H).setAnomalyLabels(existingLabels);
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final AnomalyLabelDTO christmasDayLabel = new AnomalyLabelDTO().setName("Holiday")
        .setMetadata(Map.of("eventName", "Christmas Day"))
        .setIgnore(false);
    final List<AnomalyLabelDTO> newLabels = List.of(christmasDayLabel, coldStartLabel);
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime).setAnomalyLabels(
        newLabels);
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
    assertThat(merged.get(0).getAnomalyLabels().size()).isEqualTo(3);
  }

  @Test
  public void testMergeDoMergeWithLabelsWithExistingIgnoreDefaultSameTrue() {
    // never merge anomalies with different ignore default
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H).setAnomalyLabels(List.of(new AnomalyLabelDTO().setIgnore(true)));
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime).setAnomalyLabels(
        List.of(new AnomalyLabelDTO().setIgnore(true)));
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
  }

  @Test
  public void testMergeExistingInNew() {
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final MergedAnomalyResultDTO existing1 = existingAnomaly(
        new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(1)).getMillis(),
        newEndTime);
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
    MergedAnomalyResultDTO parent = merged.get(0);
    assertThat(parent.getStartTime()).isEqualTo(JANUARY_1_2021_01H);
    assertThat(parent.getEndTime()).isEqualTo(newEndTime);
    assertThat(parent.getId()).isEqualTo(null);
    assertThat(parent.getChildren().size()).isEqualTo(2);
  }

  @Test
  public void testMergeNewIncludedInExisting() {
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    final long expectedId = existing1.getId();
    final MergedAnomalyResultDTO new1 = newAnomaly(
        new DateTime(JANUARY_1_2021_01H, DateTimeZone.UTC).plus(Period.minutes(10)).getMillis(),
        new DateTime(JANUARY_1_2021_01H, DateTimeZone.UTC).plus(Period.minutes(30)).getMillis());
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));

    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
    MergedAnomalyResultDTO parent = merged.get(0);
    assertThat(parent.getStartTime()).isEqualTo(JANUARY_1_2021_01H);
    assertThat(parent.getEndTime()).isEqualTo(JANUARY_1_2021_02H);
    assertThat(parent.getId()).isEqualTo(expectedId);
    assertThat(parent.getChildren().size()).isEqualTo(2);
  }

  @Test
  public void testMergeExistingIncludedInNew() {
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final MergedAnomalyResultDTO existing1 = existingAnomaly(
        new DateTime(JANUARY_1_2021_01H, DateTimeZone.UTC).plus(Period.minutes(10)).getMillis(),
        new DateTime(JANUARY_1_2021_01H, DateTimeZone.UTC).plus(Period.minutes(30)).getMillis());
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
    MergedAnomalyResultDTO parent = merged.get(0);
    assertThat(parent.getStartTime()).isEqualTo(JANUARY_1_2021_01H);
    assertThat(parent.getEndTime()).isEqualTo(JANUARY_1_2021_02H);
    assertThat(parent.getId()).isEqualTo(null);
    assertThat(parent.getChildren().size()).isEqualTo(2);
  }

  @Test
  public void testMergeNoMergeIfAnomalyWouldBecomeBiggerThanMaxDuration() {
    final long endExisting = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.days(4))
        .getMillis();
    // 4 days anomaly
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H, endExisting);
    // 4 days anomaly
    final MergedAnomalyResultDTO new1 = newAnomaly(endExisting,
        new DateTime(endExisting, DateTimeZone.UTC).plus(Period.days(4)).getMillis());
    final List<MergedAnomalyResultDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<MergedAnomalyResultDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(2);
    assertThat(merged.get(0)).isEqualTo(existing1);
    assertThat(merged.get(1)).isEqualTo(new1);
  }

  // note: there is no need to test for multiple enumerationItems - merger is run for each enumeration independently
  @Test
  public void testSingleAnomalyWithMergeAndSaveWithEnumerationItem() {
    final long enumerationItemId = 1L;
    final EnumerationItemDTO ei1 = newEnumerationItemRef(enumerationItemId);
    final MergedAnomalyResultDTO existingAnomaly = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H).setEnumerationItem(ei1);
    when(mergedAnomalyResultManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), eq(enumerationItemId))).thenAnswer(
        i -> singletonList(existingAnomaly));
    final EnumerationItemDTO enumerationDTO = (EnumerationItemDTO) new EnumerationItemDTO().setId(
        enumerationItemId);
    detectionSpec.setEnumerationItemDTO(enumerationDTO);
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final List<MergedAnomalyResultDTO> output = detectionMerger.merge(
        List.of(newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H).setEnumerationItem(ei1),
            newAnomaly(JANUARY_1_2021_01H, plusMin(JANUARY_1_2021_01H, 10)).setEnumerationItem(
                ei1)));

    assertThat(output).isEqualTo(List.of(existingAnomaly));
  }
}

