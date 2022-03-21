package org.apache.pinot.thirdeye.task.runner;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.apache.pinot.thirdeye.spi.detection.model.AnomalySlice;
import org.apache.pinot.thirdeye.task.DetectionPipelineTaskInfo;
import org.mockito.invocation.InvocationOnMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AnomalyMergerTest {

  public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private static long ANOMALY_ID = 1000L;

  private AnomalyMerger anomalyMerger;
  private Date startDate;
  private Date endDate;
  private DataProvider dataProvider;
  private MergedAnomalyResultManager mergedAnomalyResultManager;

  private static DetectionPipelineTaskInfo newTaskInfo(final Date startDate, final Date endDate) {
    return new DetectionPipelineTaskInfo()
        .setStart(startDate.getTime())
        .setEnd(endDate.getTime());
  }

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

  private static AnomalySlice getAnomalySliceFromFirstArg(final InvocationOnMock i) {
    final Set<AnomalySlice> collection = (Set<AnomalySlice>) i.getArguments()[0];
    assertThat(collection.size()).isEqualTo(1);
    return collection.iterator().next();
  }

  private static Multimap<AnomalySlice, MergedAnomalyResultDTO> multimap(
      final AnomalySlice key, final List<MergedAnomalyResultDTO> anomalies) {
    final Multimap<AnomalySlice, MergedAnomalyResultDTO> mmap = ArrayListMultimap.create();
    mmap.putAll(key, anomalies);
    return mmap;
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
    dataProvider = mock(DataProvider.class);
    mergedAnomalyResultManager = mock(MergedAnomalyResultManager.class);
    anomalyMerger = new AnomalyMerger(mergedAnomalyResultManager,
        dataProvider);

    startDate = DATE_FORMAT.parse("2021-01-01 01:00:00");
    endDate = DATE_FORMAT.parse("2021-01-01 02:00:00");
  }

  @Test
  public void testPrepareSortedAnomalyList() {
    assertThat(anomalyMerger.prepareSortedAnomalyList(emptyList(), emptyList()))
        .isEqualTo(emptyList());

    final MergedAnomalyResultDTO new1 = newAnomaly(startDate, endDate);
    final MergedAnomalyResultDTO new2 = newAnomaly(startDate, plusMin(startDate, 10));
    final MergedAnomalyResultDTO existing1 = existingAnomaly(startDate, endDate);
    final MergedAnomalyResultDTO existing2 = existingAnomaly(startDate, plusMin(startDate, 10));

    assertThat(anomalyMerger.prepareSortedAnomalyList(emptyList(), singletonList(existing1)))
        .isEqualTo(singletonList(existing1));

    assertThat(anomalyMerger.prepareSortedAnomalyList(singletonList(new1), emptyList()))
        .isEqualTo(singletonList(new1));

    assertThat(anomalyMerger.prepareSortedAnomalyList(
        singletonList(new1),
        singletonList(existing1)))
        .isEqualTo(asList(existing1, new1));

    assertThat(anomalyMerger.prepareSortedAnomalyList(
        asList(new1, new2),
        asList(existing1, existing2)))
        .isEqualTo(asList(existing1, new1, existing2, new2));
  }

  @Test
  public void testEmptyMergeAndSave() {
    anomalyMerger.mergeAndSave(newTaskInfo(startDate, endDate),
        newAlert(),
        emptyList());
  }

  @Test
  public void testSingleAnomalyNoMergeAndSave() {
    when(dataProvider.fetchAnomalies(any()))
        .thenAnswer(i -> multimap(getAnomalySliceFromFirstArg(i), emptyList()));

    final MergedAnomalyResultDTO newAnomaly = newAnomaly(startDate, endDate);
    anomalyMerger.mergeAndSave(newTaskInfo(startDate, endDate),
        newAlert(),
        singletonList(newAnomaly));
    verify(mergedAnomalyResultManager).save(newAnomaly);
  }

  @Test
  public void testSingleAnomalyWithMergeAndSave() {
    when(dataProvider.fetchAnomalies(any()))
        .thenAnswer(i -> multimap(getAnomalySliceFromFirstArg(i),
            singletonList(existingAnomaly(startDate, endDate))));

    anomalyMerger.mergeAndSave(newTaskInfo(startDate, endDate),
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

    final List<MergedAnomalyResultDTO> sorted = anomalyMerger.prepareSortedAnomalyList(
        asList(new1, new2),
        asList(existing1, existing2));

    final Collection<MergedAnomalyResultDTO> merged = anomalyMerger.merge(newAlert(), sorted);
    assertThat(merged.size()).isEqualTo(1);

    final MergedAnomalyResultDTO mergedAnomaly = merged.iterator().next();
    assertThat(isSameAnomaly(mergedAnomaly, existing1)).isTrue();
  }
}
