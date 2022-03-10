/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task.runner;

import static ai.startree.thirdeye.task.runner.AnomalyMerger.DEFAULT_ANOMALY_MAX_DURATION;
import static ai.startree.thirdeye.task.runner.AnomalyMerger.DEFAULT_MERGE_MAX_GAP;
import static java.util.Arrays.asList;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnomalyMergerTest {

  public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private static long ANOMALY_ID = 1000L;

  private AnomalyMerger anomalyMerger;
  private Date startDate;
  private Date endDate;
  private MergedAnomalyResultManager mergedAnomalyResultManager;

  private static AlertDTO newAlert() {
    final AlertDTO alert = new AlertDTO();
    alert.setId(100L);
    return alert;
  }

  private static MergedAnomalyResultDTO newAnomaly(final Date startDate, final Date endDate) {
    final MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setStartTime(startDate.getTime());
    anomaly.setEndTime(endDate.getTime());
    return anomaly;
  }

  private static MergedAnomalyResultDTO existingAnomaly(final Date startDate, final Date endDate) {
    final MergedAnomalyResultDTO anomaly = newAnomaly(startDate, endDate);
    anomaly.setId(++ANOMALY_ID);
    return anomaly;
  }

  private static Date plusMin(final Date startDate, final int minutes) {
    return plusDuration(startDate, Duration.ofMinutes(minutes));
  }

  private static Date plusDuration(final Date startDate, final Duration duration) {
    return new Date(startDate.getTime() + duration.toMillis());
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

    startDate = DATE_FORMAT.parse("2021-01-01 01:00:00");
    endDate = DATE_FORMAT.parse("2021-01-01 02:00:00");
  }

  @Test
  public void testPrepareSortedAnomalyList() {
    assertThat(anomalyMerger.mergedAndSort(emptyList(), emptyList()))
        .isEqualTo(emptyList());

    final MergedAnomalyResultDTO new1 = newAnomaly(startDate, endDate);
    final MergedAnomalyResultDTO new2 = newAnomaly(startDate, plusMin(startDate, 10));
    final MergedAnomalyResultDTO existing1 = existingAnomaly(startDate, endDate);
    final MergedAnomalyResultDTO existing2 = existingAnomaly(startDate, plusMin(startDate, 10));

    assertThat(anomalyMerger.mergedAndSort(emptyList(), singletonList(existing1)))
        .isEqualTo(singletonList(existing1));

    assertThat(anomalyMerger.mergedAndSort(singletonList(new1), emptyList()))
        .isEqualTo(singletonList(new1));

    assertThat(anomalyMerger.mergedAndSort(
        singletonList(new1),
        singletonList(existing1)))
        .isEqualTo(asList(existing1, new1));

    assertThat(anomalyMerger.mergedAndSort(
        asList(new1, new2),
        asList(existing1, existing2)))
        .isEqualTo(asList(existing1, new1, existing2, new2));
  }

  @Test
  public void testEmptyMergeAndSave() {
    anomalyMerger.mergeAndSave(
        newAlert(),
        emptyList());
  }

  @Test
  public void testSingleAnomalyNoMergeAndSave() {
    when(mergedAnomalyResultManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(), anyLong(), anyLong()))
        .thenAnswer(i -> emptyList());

    final MergedAnomalyResultDTO newAnomaly = newAnomaly(startDate, endDate);
    anomalyMerger.mergeAndSave(
        newAlert(),
        singletonList(newAnomaly));
    verify(mergedAnomalyResultManager).save(newAnomaly);
  }

  @Test
  public void testSingleAnomalyWithMergeAndSave() {
    when(mergedAnomalyResultManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(), anyLong(), anyLong()))
        .thenAnswer(i -> singletonList(existingAnomaly(startDate, endDate)));

    anomalyMerger.mergeAndSave(
        newAlert(),
        asList(
            newAnomaly(startDate, endDate),
            newAnomaly(startDate, plusMin(startDate, 10))
        ));
    verify(mergedAnomalyResultManager, times(1)).save(any());
  }

  @Test
  public void testMerge() {
    final MergedAnomalyResultDTO new1 = newAnomaly(startDate, endDate);
    final MergedAnomalyResultDTO new2 = newAnomaly(startDate, plusMin(startDate, 10));
    final MergedAnomalyResultDTO existing1 = existingAnomaly(startDate, endDate);
    final MergedAnomalyResultDTO existing2 = existingAnomaly(startDate, plusMin(startDate, 10));

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.mergedAndSort(
        asList(new1, new2),
        asList(existing1, existing2));

    final Collection<MergedAnomalyResultDTO> merged = anomalyMerger.merge(sorted,
        DEFAULT_MERGE_MAX_GAP, DEFAULT_ANOMALY_MAX_DURATION);
    assertThat(merged.size()).isEqualTo(1);

    final MergedAnomalyResultDTO mergedAnomaly = merged.iterator().next();
    assertThat(isSameAnomaly(mergedAnomaly, existing1)).isTrue();
  }
}
