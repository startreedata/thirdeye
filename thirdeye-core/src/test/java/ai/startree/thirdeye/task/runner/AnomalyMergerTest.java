/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task.runner;

import static ai.startree.thirdeye.task.runner.AnomalyMerger.DEFAULT_ANOMALY_MAX_DURATION;
import static ai.startree.thirdeye.task.runner.AnomalyMerger.DEFAULT_MERGE_MAX_GAP;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnomalyMergerTest {

  private static long ANOMALY_ID = 1000L;

  private AnomalyMerger anomalyMerger;
  private static final long JANUARY_1_2021_01H = 1609462800_000L;
  private static final long JANUARY_1_2021_02H = 1609466400_000L;

  private MergedAnomalyResultManager mergedAnomalyResultManager;

  private static AlertDTO newAlert() {
    final AlertDTO alert = new AlertDTO();
    alert.setId(100L);
    return alert;
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
    return new DateTime(startDate).plus(Period.minutes(minutes)).getMillis();
  }

  public static boolean isSameAnomaly(MergedAnomalyResultDTO a1, MergedAnomalyResultDTO a2) {
    return Objects.equals(a1.getId(), a2.getId())
        && a1.getStartTime() == a2.getStartTime()
        && a1.getEndTime() == a2.getEndTime();
  }

  @BeforeMethod
  public void setUp() throws ParseException {
    mergedAnomalyResultManager = mock(MergedAnomalyResultManager.class);
    anomalyMerger = new AnomalyMerger(mergedAnomalyResultManager);
  }

  @Test
  public void testPrepareSortedAnomalyList() {
    assertThat(anomalyMerger.combineAndSort(emptyList(), emptyList()))
        .isEqualTo(emptyList());

    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final MergedAnomalyResultDTO new2 = newAnomaly(JANUARY_1_2021_01H,
        plusMin(JANUARY_1_2021_01H, 10));
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    final MergedAnomalyResultDTO existing2 = existingAnomaly(JANUARY_1_2021_01H, plusMin(
        JANUARY_1_2021_01H, 10));

    assertThat(anomalyMerger.combineAndSort(emptyList(), singletonList(existing1)))
        .isEqualTo(singletonList(existing1));

    assertThat(anomalyMerger.combineAndSort(singletonList(new1), emptyList()))
        .isEqualTo(singletonList(new1));

    assertThat(anomalyMerger.combineAndSort(
        singletonList(new1),
        singletonList(existing1)))
        .isEqualTo(List.of(existing1, new1));

    assertThat(anomalyMerger.combineAndSort(
        List.of(new1, new2),
        List.of(existing1, existing2)))
        .isEqualTo(List.of(existing1, new1, existing2, new2));
  }

  @Test
  public void testEmptyMergeAndSave() {
    anomalyMerger.mergeAndSave(
        newAlert(),
        emptyList());
  }

  @Test
  public void testSingleAnomalyNoMergeAndSave() {
    when(mergedAnomalyResultManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(),
        anyLong()))
        .thenAnswer(i -> emptyList());

    final MergedAnomalyResultDTO newAnomaly = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    anomalyMerger.mergeAndSave(
        newAlert(),
        singletonList(newAnomaly));
    verify(mergedAnomalyResultManager).save(newAnomaly);
  }

  @Test
  public void testSingleAnomalyWithMergeAndSave() {
    when(mergedAnomalyResultManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(),
        anyLong()))
        .thenAnswer(i -> singletonList(existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H)));

    anomalyMerger.mergeAndSave(
        newAlert(),
        List.of(
            newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H),
            newAnomaly(JANUARY_1_2021_01H, plusMin(JANUARY_1_2021_01H, 10))
        ));
    verify(mergedAnomalyResultManager, times(1)).save(any());
  }

  @Test
  public void testMerge() {
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final MergedAnomalyResultDTO new2 = newAnomaly(JANUARY_1_2021_01H,
        plusMin(JANUARY_1_2021_01H, 10));
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    final MergedAnomalyResultDTO existing2 = existingAnomaly(JANUARY_1_2021_01H, plusMin(
        JANUARY_1_2021_01H, 10));

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.combineAndSort(
        List.of(new1, new2),
        List.of(existing1, existing2));

    final List<MergedAnomalyResultDTO> merged = anomalyMerger.merge(sorted,
        DEFAULT_MERGE_MAX_GAP, DEFAULT_ANOMALY_MAX_DURATION);
    assertThat(merged.size()).isEqualTo(1);

    assertThat(isSameAnomaly(merged.get(0), existing1)).isTrue();
  }


  @Test
  public void testMergeWithSameKeySpacedByMoreThanMergeGap() {
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    // new anomaly that happens after the merge gap
    final long afterMergeGapStart = JANUARY_1_2021_02H + DEFAULT_MERGE_MAX_GAP + 1;
    final long afterMergeGapEnd = plusMin(afterMergeGapStart, 60);
    final MergedAnomalyResultDTO new2 = newAnomaly(afterMergeGapStart, afterMergeGapEnd);

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.combineAndSort(
        List.of(new1, new2),
        List.of());

    final List<MergedAnomalyResultDTO> merged = anomalyMerger.merge(sorted,
        DEFAULT_MERGE_MAX_GAP, DEFAULT_ANOMALY_MAX_DURATION);

    assertThat(merged.size()).isEqualTo(2);
    assertThat(merged.get(0)).isEqualTo(new1);
    assertThat(merged.get(1)).isEqualTo(new2);
  }


  @Test
  public void testMergeWithSameKeySpacedByMoreThanMergeGapWithExisting() {
    final MergedAnomalyResultDTO existing1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    // new anomaly that happens before the merge gap
    final long afterMergeGapStart = JANUARY_1_2021_02H + DEFAULT_MERGE_MAX_GAP + 1;
    final long afterMergeGapEnd = plusMin(afterMergeGapStart, 60);
    final MergedAnomalyResultDTO new1 = newAnomaly(afterMergeGapStart, afterMergeGapEnd);

    final long afterMergeGapStart2 = afterMergeGapEnd + DEFAULT_MERGE_MAX_GAP + 1;
    final long afterMergeGapEnd2 = plusMin(afterMergeGapStart2, 60);
    final MergedAnomalyResultDTO new2 = newAnomaly(afterMergeGapStart2, afterMergeGapEnd2);

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.combineAndSort(
        List.of(new1, new2),
        List.of(existing1));

    final List<MergedAnomalyResultDTO> merged = anomalyMerger.merge(sorted,
        DEFAULT_MERGE_MAX_GAP, DEFAULT_ANOMALY_MAX_DURATION);

    assertThat(merged.size()).isEqualTo(3);
    assertThat(merged.get(0)).isEqualTo(existing1);
    assertThat(merged.get(1)).isEqualTo(new1);
    assertThat(merged.get(2)).isEqualTo(new2);
  }
}
