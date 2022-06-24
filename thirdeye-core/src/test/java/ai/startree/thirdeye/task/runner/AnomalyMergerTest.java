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

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnomalyMergerTest {

  private static long ANOMALY_ID = 1000L;

  private AnomalyMerger anomalyMerger;
  private static final long JANUARY_1_2021_01H = 1609462800_000L;
  private static final long JANUARY_1_2021_02H = 1609466400_000L;

  private MergedAnomalyResultManager mergedAnomalyResultManager;
  private AlertTemplateRenderer alertTemplateRenderer;

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
    return new DateTime(startDate, DateTimeZone.UTC).plus(Period.minutes(minutes)).getMillis();
  }

  public static boolean isSameAnomaly(MergedAnomalyResultDTO a1, MergedAnomalyResultDTO a2) {
    return Objects.equals(a1.getId(), a2.getId())
        && a1.getStartTime() == a2.getStartTime()
        && a1.getEndTime() == a2.getEndTime();
  }

  @BeforeMethod
  public void setUp() throws ParseException, IOException, ClassNotFoundException {
    mergedAnomalyResultManager = mock(MergedAnomalyResultManager.class);
    alertTemplateRenderer = mock(AlertTemplateRenderer.class);
    when(alertTemplateRenderer.renderAlert(any(AlertDTO.class), any(Interval.class)))
        .thenReturn(new AlertTemplateDTO());
    anomalyMerger = new AnomalyMerger(mergedAnomalyResultManager, alertTemplateRenderer);
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
  public void testMergeAllNewAndExistingAnomalies() {
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
        DEFAULT_MERGE_MAX_GAP, DEFAULT_ANOMALY_MAX_DURATION, DateTimeZone.UTC);
    assertThat(merged.size()).isEqualTo(1);

    assertThat(isSameAnomaly(merged.get(0), existing1)).isTrue();
  }

  @Test
  public void testMergeNoMergeWhenAnomaliesSpacedByMoreThanMergeGap() {
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    // new anomaly that happens after the merge gap
    final long afterMergeGapStart = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(
            DEFAULT_MERGE_MAX_GAP)
        .plus(1)
        .getMillis();
    final long afterMergeGapEnd = plusMin(afterMergeGapStart, 60);
    final MergedAnomalyResultDTO new2 = newAnomaly(afterMergeGapStart, afterMergeGapEnd);

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.combineAndSort(
        List.of(new1, new2),
        List.of());

    final List<MergedAnomalyResultDTO> merged = anomalyMerger.merge(sorted,
        DEFAULT_MERGE_MAX_GAP, DEFAULT_ANOMALY_MAX_DURATION, DateTimeZone.UTC);

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
            DEFAULT_MERGE_MAX_GAP)
        .plus(1)
        .getMillis();
    final long afterMergeGapEnd = plusMin(afterMergeGapStart, 60);
    final MergedAnomalyResultDTO new1 = newAnomaly(afterMergeGapStart, afterMergeGapEnd);

    final long afterMergeGapStart2 = new DateTime(afterMergeGapEnd, DateTimeZone.UTC).plus(
            DEFAULT_MERGE_MAX_GAP)
        .plus(1)
        .getMillis();
    final long afterMergeGapEnd2 = plusMin(afterMergeGapStart2, 60);
    final MergedAnomalyResultDTO new2 = newAnomaly(afterMergeGapStart2, afterMergeGapEnd2);

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.combineAndSort(
        List.of(new1, new2),
        List.of(existing1));

    final List<MergedAnomalyResultDTO> merged = anomalyMerger.merge(sorted,
        DEFAULT_MERGE_MAX_GAP, DEFAULT_ANOMALY_MAX_DURATION, DateTimeZone.UTC);

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

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.combineAndSort(
        List.of(new1),
        List.of(existing1));

    final List<MergedAnomalyResultDTO> merged = anomalyMerger.merge(sorted,
        DEFAULT_MERGE_MAX_GAP, DEFAULT_ANOMALY_MAX_DURATION, DateTimeZone.UTC);

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

    final long expectedId = existing1.getId();

    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime);

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.combineAndSort(
        List.of(new1),
        List.of(existing1));

    final List<MergedAnomalyResultDTO> merged = anomalyMerger.merge(sorted,
        // zero period here
        Period.ZERO, DEFAULT_ANOMALY_MAX_DURATION, DateTimeZone.UTC);

    assertThat(merged.size()).isEqualTo(2);
  }

  @Test
  public void testMergeExistingInNew() {
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);

    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final MergedAnomalyResultDTO existing1 = existingAnomaly(
        new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(1)).getMillis(),
        newEndTime);

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.combineAndSort(
        List.of(new1),
        List.of(existing1));

    final List<MergedAnomalyResultDTO> merged = anomalyMerger.merge(sorted,
        DEFAULT_MERGE_MAX_GAP, DEFAULT_ANOMALY_MAX_DURATION, DateTimeZone.UTC);

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

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.combineAndSort(
        List.of(new1),
        List.of(existing1));

    final List<MergedAnomalyResultDTO> merged = anomalyMerger.merge(sorted,
        DEFAULT_MERGE_MAX_GAP, DEFAULT_ANOMALY_MAX_DURATION, DateTimeZone.UTC);

    assertThat(merged.size()).isEqualTo(1);
    MergedAnomalyResultDTO parent = merged.get(0);
    assertThat(parent.getStartTime()).isEqualTo(JANUARY_1_2021_01H);
    assertThat(parent.getEndTime()).isEqualTo(JANUARY_1_2021_02H);
    assertThat(parent.getId()).isEqualTo(expectedId);
    assertThat(parent.getChildren().size()).isEqualTo(2);
  }

  @Test
  public void testMergeExistingIncludedInNew() {
    final MergedAnomalyResultDTO new1 = newAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);

    final MergedAnomalyResultDTO existing1 = existingAnomaly(
        new DateTime(JANUARY_1_2021_01H, DateTimeZone.UTC).plus(Period.minutes(10)).getMillis(),
        new DateTime(JANUARY_1_2021_01H, DateTimeZone.UTC).plus(Period.minutes(30)).getMillis());

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.combineAndSort(
        List.of(new1),
        List.of(existing1));

    final List<MergedAnomalyResultDTO> merged = anomalyMerger.merge(sorted,
        DEFAULT_MERGE_MAX_GAP, DEFAULT_ANOMALY_MAX_DURATION, DateTimeZone.UTC);

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
    final MergedAnomalyResultDTO new1 = newAnomaly(endExisting, new DateTime(endExisting,
        DateTimeZone.UTC).plus(Period.days(4)).getMillis());

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.combineAndSort(
        List.of(new1),
        List.of(existing1));

    final List<MergedAnomalyResultDTO> merged = anomalyMerger.merge(sorted,
        DEFAULT_MERGE_MAX_GAP, DEFAULT_ANOMALY_MAX_DURATION, DateTimeZone.UTC);

    assertThat(merged.size()).isEqualTo(2);
    assertThat(merged.get(0)).isEqualTo(existing1);
    assertThat(merged.get(1)).isEqualTo(new1);
  }
}
