package org.apache.pinot.thirdeye.task.runner;

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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
  public void testEmptyMerge() {
    anomalyMerger.mergeAndSave(newTaskInfo(startDate, endDate),
        newAlert(),
        emptyList());
  }

  @Test
  public void testSingleAnomalyNoMerge() {
    when(dataProvider.fetchAnomalies(any()))
        .thenAnswer(i -> multimap(getAnomalySliceFromFirstArg(i), emptyList()));

    final MergedAnomalyResultDTO newAnomaly = newAnomaly(startDate, endDate);
    anomalyMerger.mergeAndSave(newTaskInfo(startDate, endDate),
        newAlert(),
        singletonList(newAnomaly));
    verify(mergedAnomalyResultManager).save(newAnomaly);
  }

  @Test
  public void testSingleAnomalyWithMerge() {
    when(dataProvider.fetchAnomalies(any()))
        .thenAnswer(i -> multimap(getAnomalySliceFromFirstArg(i),
            singletonList(existingAnomaly(startDate, endDate))));

    anomalyMerger.mergeAndSave(newTaskInfo(startDate, endDate),
        newAlert(),
        Arrays.asList(
            newAnomaly(startDate, endDate),
            newAnomaly(startDate, new Date(startDate.getTime() + Duration.ofMinutes(10).toMillis()))
        ));
    verify(mergedAnomalyResultManager, times(1)).save(any());
  }
}
