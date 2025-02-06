/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.datalayer.bao;

import static ai.startree.thirdeye.datalayer.DatalayerTestUtils.collectIds;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.AnomalyCause;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import com.google.inject.Injector;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestAnomalyManager {

  private static final long START_TIME = System.currentTimeMillis();
  private static final long END_TIME = START_TIME + 60_000L;
  private static final long CREATED_TIME = END_TIME + 60_000L;

  private AnomalyDTO mergedResult = null;
  private AlertManager detectionConfigDAO;
  private AnomalyManager anomalyManager;

  public static AlertDTO mockDetectionConfig() {
    final AlertDTO detectionConfig = new AlertDTO();
    detectionConfig.setName("Only For Test");
    return detectionConfig;
  }

  public static List<AnomalyDTO> mockAnomalies(final long detectionConfigId) {
    final AnomalyDTO anomaly1 = new AnomalyDTO();
    anomaly1.setMetric("metric");
    anomaly1.setDetectionConfigId(detectionConfigId);
    anomaly1.setStartTime(new DateTime(2019, 1, 1, 0, 0).getMillis());
    anomaly1.setEndTime(new DateTime(2019, 1, 1, 12, 0).getMillis());
    final AnomalyDTO anomaly2 = new AnomalyDTO();
    anomaly2.setMetric("metric");
    anomaly2.setDetectionConfigId(detectionConfigId);
    anomaly2.setStartTime(new DateTime(2019, 1, 2, 10, 0).getMillis());
    anomaly2.setEndTime(new DateTime(2019, 1, 2, 20, 0).getMillis());

    return Arrays.asList(anomaly1, anomaly2);
  }

  /**
   * Example:
   * String str = "Jun 13 2003 23:11:52.454 UTC";
   */
  public static long epoch(final String dateStr) throws ParseException {
    final SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
    final Date date = df.parse(dateStr);
    return date.getTime();
  }

  private static AnomalyDTO anomalyWithCreateTime(final long createTimeOffset) {
    final AnomalyDTO anomaly = new AnomalyDTO().setStartTime(START_TIME).setEndTime(END_TIME);
    anomaly.setCreateTime(new Timestamp(CREATED_TIME + createTimeOffset));
    return anomaly;
  }

  private static EnumerationItemDTO enumerationItem(final long id) {
    return (EnumerationItemDTO) new EnumerationItemDTO().setId(id);
  }

  private static AnomalyDTO anomaly(long startTime, long endTime) {
    return new AnomalyDTO().setStartTime(startTime).setEndTime(endTime);
  }

  private static AnomalyDTO anomaly() {
    return new AnomalyDTO();
  }

  private static AnomalyLabelDTO anomalyLabel(final boolean ignore) {
    return new AnomalyLabelDTO().setIgnore(ignore);
  }

  private static AnomalyDTO findAnomalyById(final List<AnomalyDTO> anomalies, final Long id) {
    for (final AnomalyDTO anomaly : anomalies) {
      if (anomaly.getId().equals(id)) {
        return anomaly;
      }
    }
    return null;
  }

  private AnomalyDTO provideFeedbackToAnomaly(final Long anomalyId, final AnomalyFeedbackType type,
      final String comment) {
    final AnomalyDTO anomalyForFeedback = anomalyManager.findById(anomalyId);
    final AnomalyFeedbackDTO feedback = new AnomalyFeedbackDTO().setComment(comment)
        .setFeedbackType(type);
    anomalyForFeedback.setFeedback(feedback);
    anomalyManager.updateAnomalyFeedback(anomalyForFeedback);
    return anomalyManager.findById(anomalyId);
  }

  @BeforeClass
  void beforeClass() {
    final Injector injector = MySqlTestDatabase.sharedInjector();
    detectionConfigDAO = injector.getInstance(AlertManager.class);
    anomalyManager = injector.getInstance(AnomalyManager.class);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {
    if (detectionConfigDAO != null) {
      detectionConfigDAO.findAll().forEach(detectionConfigDAO::delete); 
    }
    if (anomalyManager != null) {
      anomalyManager.findAll().forEach(anomalyManager::delete); 
    }
  }

  @Test
  public void testAnomalyCrud() throws ParseException {
    final AnomalyDTO a = new AnomalyDTO().setStartTime(1000).setEndTime(2000);

    final Long id = anomalyManager.save(a);
    assertThat(id).isNotNull();
    assertThat(a.getCreateTime()).isNotNull();

    final AnomalyDTO aRetrieved = anomalyManager.findById(id);
    assertThat(aRetrieved).isNotNull();
    assertThat(aRetrieved.getStartTime()).isEqualTo(a.getStartTime());
    assertThat(aRetrieved.getEndTime()).isEqualTo(a.getEndTime());

    /*
     * There could be some round off errors when saving timestamp to DB but it should be less
     * than 1 sec in rounding error.
     */
    assertThat(
        Math.abs(aRetrieved.getCreateTime().getTime() - a.getCreateTime().getTime())).isLessThan(
        1000L);
  }

  @Test
  public void testAnomalyCrudWithCreateTime() throws ParseException {
    final AnomalyDTO a = new AnomalyDTO().setStartTime(1000).setEndTime(2000);

    /* Checking if create time is retained */
    a.setCreateTime(new Timestamp(epoch("Jan 01 2020 01:01:01.454 UTC")));

    final Long id = anomalyManager.save(a);
    assertThat(id).isNotNull();
    assertThat(a.getCreateTime()).isNotNull();

    final AnomalyDTO aRetrieved = anomalyManager.findById(id);
    assertThat(aRetrieved).isNotNull();
    assertThat(aRetrieved.getStartTime()).isEqualTo(a.getStartTime());
    assertThat(aRetrieved.getEndTime()).isEqualTo(a.getEndTime());

    /*
     * There could be some round off errors when saving timestamp to DB but it should be less
     * than 1 sec in rounding error.
     */
    assertThat(
        Math.abs(aRetrieved.getCreateTime().getTime() - a.getCreateTime().getTime())).isLessThan(
        1000L);
  }

  @Test(dependsOnMethods = {"testSaveChildren"})
  public void testFeedback() {
    final String expectedComment = "this is a good find";
    final String expectedUser = "test user";
    final AnomalyDTO anomalyMergedResult = anomalyManager.findById(mergedResult.getId());
    AnomalyFeedbackDTO feedback = new AnomalyFeedbackDTO().setComment(expectedComment)
        .setFeedbackType(AnomalyFeedbackType.ANOMALY)
        .setCause(AnomalyCause.FRAUD);
    feedback = (AnomalyFeedbackDTO) feedback.setUpdatedBy(expectedUser);

    anomalyMergedResult.setFeedback(feedback);
    // now we need to make explicit call to anomaly update in order to update the feedback
    anomalyManager.updateAnomalyFeedback(anomalyMergedResult);

    //verify feedback
    final AnomalyDTO mergedResult1 = anomalyManager.findById(mergedResult.getId());
    AnomalyFeedbackDTO actualFeedback = (AnomalyFeedbackDTO) mergedResult1.getFeedback();
    Assert.assertEquals(actualFeedback.getFeedbackType(), AnomalyFeedbackType.ANOMALY);
    Assert.assertEquals(actualFeedback.getComment(), expectedComment);
    Assert.assertEquals(actualFeedback.getUpdatedBy(), expectedUser);
    Assert.assertEquals(actualFeedback.getCause(), AnomalyCause.FRAUD);
  }

  @Test(dependsOnMethods = {"testFeedback"})
  public void testFeedback_updateWithoutOptionalFields() {
    final String expectedUser = "test user 2";
    final AnomalyDTO anomalyMergedResult = anomalyManager.findById(mergedResult.getId());
    AnomalyFeedbackDTO feedback = new AnomalyFeedbackDTO().setFeedbackType(
        AnomalyFeedbackType.NOT_ANOMALY);
    feedback = (AnomalyFeedbackDTO) feedback.setUpdatedBy(expectedUser);

    anomalyMergedResult.setFeedback(feedback);
    // now we need to make explicit call to anomaly update in order to update the feedback
    anomalyManager.updateAnomalyFeedback(anomalyMergedResult);

    //verify feedback
    final AnomalyDTO mergedResult1 = anomalyManager.findById(mergedResult.getId());
    AnomalyFeedbackDTO actualFeedback = (AnomalyFeedbackDTO) mergedResult1.getFeedback();
    assertThat(actualFeedback.getFeedbackType()).isEqualTo(AnomalyFeedbackType.NOT_ANOMALY);
    assertThat(actualFeedback.getUpdatedBy()).isEqualTo(expectedUser);
    assertThat(actualFeedback.getComment()).isEmpty();
    assertThat(actualFeedback.getCause()).isNull();
  }

  @Test
  public void testFindAllAnomaliesForFeedback() {
    final Long id1 = anomalyManager.save(anomaly(10000, 11000));
    final Long id2 = anomalyManager.save(anomaly(12000, 13000));

    final String feedbackComment = "test feedback";
    provideFeedbackToAnomaly(id1, AnomalyFeedbackType.ANOMALY, feedbackComment);

    final List<AnomalyDTO> findAllAnomalies = anomalyManager.findAll();
    assertThat(findAnomalyById(findAllAnomalies, id1).getFeedback().getComment()).isEqualTo(
        feedbackComment);
    assertThat(findAnomalyById(findAllAnomalies, id2).getFeedback()).isNull();
  }

  @Test(dependsOnMethods = {"testFindAllAnomaliesForFeedback"})
  public void testFilterAnomaliesForFeedback() {
    final Long id1 = anomalyManager.save(anomaly(10000, 11000));
    final Long id2 = anomalyManager.save(anomaly(12000, 13000));

    final String feedbackComment = "test feedback";
    provideFeedbackToAnomaly(id1, AnomalyFeedbackType.ANOMALY, feedbackComment);

    final DaoFilter filter = new DaoFilter().setPredicate(Predicate.GE("startTime", 10000));
    final List<AnomalyDTO> filterAnomalies = anomalyManager.filter(filter);
    assertThat(findAnomalyById(filterAnomalies, id1).getFeedback().getComment()).isEqualTo(
        feedbackComment);
    assertThat(findAnomalyById(filterAnomalies, id2).getFeedback()).isNull();
  }

  @Test
  public void testSaveChildren() {
    mergedResult = anomaly(1000, 2000);

    final AnomalyDTO child1 = anomaly(1000, 1500);

    final AnomalyDTO child2 = anomaly(1500, 2000);

    mergedResult.setChildren(new HashSet<>(Arrays.asList(child1, child2)));

    anomalyManager.save(mergedResult);

    Assert.assertNotNull(mergedResult.getId());
    Assert.assertNotNull(child1.getId());
    Assert.assertNotNull(child2.getId());
  }

  @Test
  public void testFindByStartEndTimeInRangeAndDetectionConfigId() {
    final long detectionConfigId = detectionConfigDAO.save(mockDetectionConfig());
    final List<AnomalyDTO> anomalies = mockAnomalies(detectionConfigId);
    for (final AnomalyDTO anomaly : anomalies) {
      anomalyManager.save(anomaly);
    }
    final DateTime start = new DateTime(2019, 1, 1, 0, 0, DateTimeZone.UTC);
    final DateTime end = new DateTime(2019, 1, 3, 0, 0, DateTimeZone.UTC);
    final List<AnomalyDTO> fetchedAnomalies = anomalyManager.filter(
        new AnomalyFilter().setAlertId(detectionConfigId)
            .setStartEndWindow(new Interval(start, end)));
    Assert.assertEquals(fetchedAnomalies.size(), anomalies.size());
    for (int i = 0; i < anomalies.size(); i++) {
      final AnomalyDTO actual = fetchedAnomalies.get(i);
      final AnomalyDTO expected = anomalies.get(i);
      Assert.assertNotNull(actual.getId());
      Assert.assertEquals(actual.getDetectionConfigId(), expected.getDetectionConfigId());
    }
    // Clean up
    for (int i = 0; i < anomalies.size(); i++) {
      anomalyManager.delete(fetchedAnomalies.get(i));
    }
    detectionConfigDAO.deleteById(detectionConfigId);
  }

  @Test
  public void testFindByStartTimeInRangeAndDetectionConfigId() {
    final long detectionConfigId = detectionConfigDAO.save(mockDetectionConfig());
    final List<AnomalyDTO> anomalies = mockAnomalies(detectionConfigId);
    for (final AnomalyDTO anomaly : anomalies) {
      anomalyManager.save(anomaly);
    }
    final DateTime start = new DateTime(2019, 1, 1, 0, 0, DateTimeZone.UTC);
    final DateTime end = new DateTime(2019, 1, 3, 0, 0, DateTimeZone.UTC);
    final List<AnomalyDTO> fetchedAnomalies = anomalyManager.filter(
        new AnomalyFilter().setAlertId(detectionConfigId)
            .setStartEndWindow(new Interval(start, end)));
    Assert.assertEquals(fetchedAnomalies.size(), anomalies.size());
    for (int i = 0; i < anomalies.size(); i++) {
      final AnomalyDTO actual = fetchedAnomalies.get(i);
      final AnomalyDTO expected = anomalies.get(i);
      Assert.assertNotNull(actual.getId());
      Assert.assertEquals(actual.getDetectionConfigId(), expected.getDetectionConfigId());
    }
    // Clean up
    for (int i = 0; i < anomalies.size(); i++) {
      anomalyManager.delete(fetchedAnomalies.get(i));
    }
    detectionConfigDAO.deleteById(detectionConfigId);
  }

  @Test
  public void testSaveChildrenIndependently() {
    final AnomalyDTO parent = new AnomalyDTO();
    parent.setStartTime(1000);
    parent.setEndTime(2000);

    final AnomalyDTO child1 = new AnomalyDTO();
    child1.setStartTime(1000);
    child1.setEndTime(1500);

    final AnomalyDTO child2 = new AnomalyDTO();
    child2.setStartTime(1500);
    child2.setEndTime(2000);

    parent.setChildren(new HashSet<>(Arrays.asList(child1, child2)));

    final long id = anomalyManager.save(child1);
    anomalyManager.save(parent);

    Assert.assertNotNull(parent.getId());
    Assert.assertEquals(child1.getId().longValue(), id);
    Assert.assertNotNull(child2.getId());
  }

  @Test
  public void testSaveAndLoadHierarchicalAnomalies() {
    final AnomalyDTO parent = new AnomalyDTO();
    parent.setStartTime(1000);
    parent.setEndTime(2000);

    final AnomalyDTO child1 = new AnomalyDTO();
    child1.setStartTime(1000);
    child1.setEndTime(1500);

    final AnomalyDTO child2 = new AnomalyDTO();
    child2.setStartTime(1500);
    child2.setEndTime(2000);

    final AnomalyDTO child3 = new AnomalyDTO();
    child3.setStartTime(1600);
    child3.setEndTime(1800);

    child2.setChildren(new HashSet<>(Arrays.asList(child3)));
    parent.setChildren(new HashSet<>(Arrays.asList(child1, child2)));

    final long parentId = anomalyManager.save(parent);

    final AnomalyDTO read = anomalyManager.findById(parentId);

    assertThat(read).isNotSameAs(parent);
    Assert.assertEquals(read.getStartTime(), 1000);
    Assert.assertEquals(read.getEndTime(), 2000);
    Assert.assertFalse(read.isChild());
    Assert.assertFalse(read.getChildren().isEmpty());

    final List<AnomalyDTO> readChildren = new ArrayList<>(read.getChildren());
    readChildren.sort(Comparator.comparingLong(AnomalyDTO::getStartTime));

    assertThat(readChildren.get(0)).isNotSameAs(child1);
    Assert.assertTrue(readChildren.get(0).isChild());
    Assert.assertEquals(readChildren.get(0).getStartTime(), 1000);
    Assert.assertEquals(readChildren.get(0).getEndTime(), 1500);

    assertThat(readChildren.get(1)).isNotSameAs(child2);
    Assert.assertTrue(readChildren.get(1).isChild());
    Assert.assertEquals(readChildren.get(1).getStartTime(), 1500);
    Assert.assertEquals(readChildren.get(1).getEndTime(), 2000);
    Assert.assertEquals(readChildren.get(1).getChildren().size(), 1);
    Assert.assertEquals(readChildren.get(1).getChildren().iterator().next().getStartTime(), 1600);
    Assert.assertEquals(readChildren.get(1).getChildren().iterator().next().getEndTime(), 1800);
  }

  @Test
  public void testUpdateToAnomalyHierarchy() {
    final AnomalyDTO parent = new AnomalyDTO();
    parent.setStartTime(1000);
    parent.setEndTime(2000);

    final AnomalyDTO child1 = new AnomalyDTO();
    child1.setStartTime(1000);
    child1.setEndTime(1500);

    final AnomalyDTO child2 = new AnomalyDTO();
    child2.setStartTime(1500);
    child2.setEndTime(2000);

    final AnomalyDTO child3 = new AnomalyDTO();
    child3.setStartTime(1600);
    child3.setEndTime(1800);

    child1.setChildren(new HashSet<>(Arrays.asList(child2)));
    parent.setChildren(new HashSet<>(Arrays.asList(child1)));

    anomalyManager.save(parent);

    child2.setChildren(new HashSet<>(Arrays.asList(child3)));

    anomalyManager.save(parent);

    final AnomalyDTO read = anomalyManager.findById(parent.getId());
    Assert.assertFalse(read.getChildren().isEmpty());
    Assert.assertEquals(read.getChildren().iterator().next().getStartTime(), 1000);
    Assert.assertFalse(read.getChildren().iterator().next().getChildren().isEmpty());
    Assert.assertEquals(
        read.getChildren().iterator().next().getChildren().iterator().next().getStartTime(), 1500);
    Assert.assertFalse(read.getChildren()
        .iterator()
        .next()
        .getChildren()
        .iterator()
        .next()
        .getChildren()
        .isEmpty());
    Assert.assertEquals(read.getChildren()
        .iterator()
        .next()
        .getChildren()
        .iterator()
        .next()
        .getChildren()
        .iterator()
        .next()
        .getStartTime(), 1600);
  }

  @Test
  public void testFindParent() {
    final AnomalyDTO top = new AnomalyDTO();
    top.setDetectionConfigId(1L);
    top.setStartTime(1000);
    top.setEndTime(3000);

    final AnomalyDTO child1 = new AnomalyDTO();
    child1.setDetectionConfigId(1L);
    child1.setStartTime(1000);
    child1.setEndTime(2000);

    final AnomalyDTO child2 = new AnomalyDTO();
    child2.setDetectionConfigId(1L);
    child2.setStartTime(1200);
    child2.setEndTime(1800);

    final AnomalyDTO child3 = new AnomalyDTO();
    child3.setDetectionConfigId(1L);
    child3.setStartTime(1500);
    child3.setEndTime(3000);

    child1.setChildren(new HashSet<>(Collections.singletonList(child2)));
    top.setChildren(new HashSet<>(Arrays.asList(child1, child3)));

    final long topId = anomalyManager.save(top);
    final AnomalyDTO topNode = anomalyManager.findById(topId);
    AnomalyDTO parent = null;
    AnomalyDTO leafNode = null;
    for (final AnomalyDTO intermediate : topNode.getChildren()) {
      if (!intermediate.getChildren().isEmpty()) {
        parent = intermediate;
        leafNode = intermediate.getChildren().iterator().next();
      }
    }
    Assert.assertNotNull(parent);
    Assert.assertEquals(parent, anomalyManager.findParent(leafNode));
  }

  @Test
  public void testFilterWithAnomalyFilter() throws InterruptedException {
    final long alertId = 1234L;
    final long alertId2 = 5678L;
    final EnumerationItemDTO ei = enumerationItem(123_123);

    final AnomalyDTO a1 = persist(
        anomalyWithCreateTime(1000).setDetectionConfigId(alertId).setEnumerationItem(ei));
    Thread.sleep(100);

    final AnomalyDTO a2 = persist(anomalyWithCreateTime(2000).setDetectionConfigId(alertId));
    Thread.sleep(100);

    final AnomalyDTO a3 = persist(anomalyWithCreateTime(3000).setDetectionConfigId(alertId2));
    Thread.sleep(100);

    final AnomalyDTO a4 = persist(anomalyWithCreateTime(4000).setDetectionConfigId(alertId));

    assertThat(collectIds(anomalyManager.filter(new AnomalyFilter().setCreateTimeWindow(
        new Interval(a2.getCreateTime().getTime(), a4.getCreateTime().getTime()))))).isEqualTo(
        collectIds(Set.of(a2, a3)));

    assertThat(collectIds(anomalyManager.filter(new AnomalyFilter().setCreateTimeWindow(
            new Interval(a2.getCreateTime().getTime(), a4.getCreateTime().getTime()))
        .setAlertId(alertId)))).isEqualTo(collectIds(Set.of(a2)));

    assertThat(collectIds(
        anomalyManager.filter(new AnomalyFilter().setAlertId(alertId2)))).isEqualTo(
        collectIds(Set.of(a3)));

    assertThat(collectIds(anomalyManager.filter(
        new AnomalyFilter().setEnumerationItemId(ei.getId())))).isEqualTo(collectIds(Set.of(a1)));
  }

  @Test
  public void testFilterWithChild() throws InterruptedException {
    final long alertId = 13579L;
    final AnomalyDTO child = persist(anomalyWithCreateTime(1000)
        .setDetectionConfigId(alertId)
        .setChild(true));
    Thread.sleep(100);

    final AnomalyDTO parent = persist(anomalyWithCreateTime(1000)
        .setDetectionConfigId(alertId)
        .setChildIds(Set.of(child.getId())));
    Thread.sleep(100);

    assertThat(collectIds(anomalyManager.filter(new AnomalyFilter()
        .setAlertId(alertId))))
        .isEqualTo(collectIds(Set.of(child, parent)));

    assertThat(collectIds(anomalyManager.filter(new AnomalyFilter()
        .setAlertId(alertId)
        .setIsChild(true))))
        .isEqualTo(collectIds(Set.of(child)));

    assertThat(collectIds(anomalyManager.filter(new AnomalyFilter()
        .setAlertId(alertId)
        .setIsChild(false))))
        .isEqualTo(collectIds(Set.of(parent)));
  }

  @Test
  public void testFilterWithEndTimeIsGte() throws InterruptedException {
    final long alertId = 10101L;
    final AnomalyDTO a1 = persist(anomaly(1000L, 2000L).setDetectionConfigId(alertId));
    Thread.sleep(100);

    final AnomalyDTO a2 = persist(anomaly(1500L, 2500L).setDetectionConfigId(alertId));
    Thread.sleep(100);

    assertThat(collectIds(anomalyManager.filter(new AnomalyFilter()
        .setAlertId(alertId))))
        .isEqualTo(collectIds(Set.of(a1, a2)));

    assertThat(collectIds(anomalyManager.filter(new AnomalyFilter()
        .setAlertId(alertId)
        .setEndTimeIsGte(0L))))
        .isEqualTo(collectIds(Set.of(a1, a2)));

    assertThat(collectIds(anomalyManager.filter(new AnomalyFilter()
        .setAlertId(alertId)
        .setEndTimeIsGte(3000L))))
        .isEqualTo(collectIds(Set.of()));

    assertThat(collectIds(anomalyManager.filter(new AnomalyFilter()
        .setAlertId(alertId)
        .setEndTimeIsGte(2100L))))
        .isEqualTo(collectIds(Set.of(a2)));

    assertThat(collectIds(anomalyManager.filter(new AnomalyFilter()
        .setAlertId(alertId)
        .setEndTimeIsGte(2000L))))
        .isEqualTo(collectIds(Set.of(a1, a2)));

    assertThat(collectIds(anomalyManager.filter(new AnomalyFilter()
        .setAlertId(alertId)
        .setEndTimeIsGte(1800L))))
        .isEqualTo(collectIds(Set.of(a1, a2)));
  }

  @Test
  public void testUpdateTime() {
    final long alertId = 1234L;

    final AnomalyDTO a1 = persist(anomalyWithCreateTime(1000).setDetectionConfigId(alertId));

    assertThat(a1.getUpdateTime()).isNull();

    persist(a1.setStartTime(System.currentTimeMillis()));
    assertThat(a1.getUpdateTime()).isNotNull();

    final Timestamp lastUpdateTime = a1.getUpdateTime();
    persist(a1.setStartTime(System.currentTimeMillis()));
    assertThat(a1.getUpdateTime()).isNotNull();
    assertThat(a1.getUpdateTime().getTime()).isGreaterThan(lastUpdateTime.getTime());
  }

  private AnomalyDTO persist(final AnomalyDTO anomaly) {
    final long id = anomalyManager.save(anomaly);
    assertThat(id).isNotNull();
    return anomaly;
  }

  @Test
  public void ignoreLabelIndexNoLabelsTest() {
    final Long id = anomalyManager.save(anomaly());
    assertByIdAndIgnored(id, false);
  }

  @Test
  public void ignoreLabelIndexLabelsWithNoIgnoreTest() {
    final List<AnomalyLabelDTO> labels = List.of(anomalyLabel(false), anomalyLabel(false));
    final Long id = anomalyManager.save(anomaly().setAnomalyLabels(labels));
    assertByIdAndIgnored(id, false);
  }

  @Test
  public void ignoreLabelIndexLabelsWithIgnoreTest() {
    final List<AnomalyLabelDTO> labels = List.of(anomalyLabel(false), anomalyLabel(true));
    final Long id = anomalyManager.save(anomaly().setAnomalyLabels(labels));
    assertByIdAndIgnored(id, true);
  }

  @Test
  public void updateLabelsToIgnoreTest() {
    // create anomaly with labels with no ignore
    final List<AnomalyLabelDTO> labels = List.of(anomalyLabel(false), anomalyLabel(false));
    final Long id = anomalyManager.save(anomaly().setAnomalyLabels(labels));

    // update the anomaly label to ignore
    final AnomalyDTO result = anomalyManager.findById(id);
    result.getAnomalyLabels().get(0).setIgnore(true);
    anomalyManager.update(result);
    assertByIdAndIgnored(id, true);
  }

  @Test
  public void updateResetIgnoreLabelsTest() {
    // create anomaly with ignore label
    final List<AnomalyLabelDTO> labels = List.of(anomalyLabel(true), anomalyLabel(false));
    final Long id = anomalyManager.save(anomaly().setAnomalyLabels(labels));

    // update the anomaly label not to ignore
    final AnomalyDTO result = anomalyManager.findById(id);
    result.getAnomalyLabels().forEach(label -> label.setIgnore(false));
    anomalyManager.update(result);
    assertByIdAndIgnored(id, false);
  }

  private List<AnomalyDTO> filterByIdAndIgnored(final Long id, final boolean ignored) {
    final DaoFilter filter = new DaoFilter().setPredicate(
        Predicate.AND(Predicate.EQ("baseId", id), Predicate.EQ("ignored", ignored)));
    return anomalyManager.filter(filter);
  }

  private void assertByIdAndIgnored(final long id, final boolean ignored) {
    final List<AnomalyDTO> anomalies = filterByIdAndIgnored(id, ignored);
    assertThat(anomalies.size()).isEqualTo(1);
    assertThat(anomalies.get(0).getId()).isEqualTo(id);
    assertThat(filterByIdAndIgnored(id, !ignored).size()).isEqualTo(0);
  }
}
