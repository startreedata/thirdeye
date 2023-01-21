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
package ai.startree.thirdeye.datalayer.bao;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestAnomalyManager {

  private AnomalyDTO mergedResult = null;

  private AlertManager detectionConfigDAO;
  private AnomalyManager mergedAnomalyResultDAO;

  @BeforeClass
  void beforeClass() {
    final Injector injector = MySqlTestDatabase.sharedInjector();
    detectionConfigDAO = injector.getInstance(AlertManager.class);
    mergedAnomalyResultDAO = injector.getInstance(AnomalyManager.class);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {
    detectionConfigDAO.findAll().forEach(detectionConfigDAO::delete);
    mergedAnomalyResultDAO.findAll().forEach(mergedAnomalyResultDAO::delete);
  }

  @Test(dependsOnMethods = {"testSaveChildren"})
  public void testFeedback() {
    AnomalyDTO anomalyMergedResult = mergedAnomalyResultDAO
        .findById(mergedResult.getId());
    AnomalyFeedbackDTO feedback = new AnomalyFeedbackDTO()
        .setComment("this is a good find")
        .setFeedbackType(AnomalyFeedbackType.ANOMALY);
    anomalyMergedResult.setFeedback(feedback);
    // now we need to make explicit call to anomaly update in order to update the feedback
    mergedAnomalyResultDAO.updateAnomalyFeedback(anomalyMergedResult);

    //verify feedback
    AnomalyDTO mergedResult1 = mergedAnomalyResultDAO.findById(mergedResult.getId());
    Assert.assertEquals(mergedResult1.getFeedback().getFeedbackType(), AnomalyFeedbackType.ANOMALY);
  }

  @Test
  public void testSaveChildren() {
    mergedResult = new AnomalyDTO();
    mergedResult.setStartTime(1000);
    mergedResult.setEndTime(2000);

    AnomalyDTO child1 = new AnomalyDTO();
    child1.setStartTime(1000);
    child1.setEndTime(1500);

    AnomalyDTO child2 = new AnomalyDTO();
    child2.setStartTime(1500);
    child2.setEndTime(2000);

    mergedResult.setChildren(new HashSet<>(Arrays.asList(child1, child2)));

    this.mergedAnomalyResultDAO.save(mergedResult);

    Assert.assertNotNull(mergedResult.getId());
    Assert.assertNotNull(child1.getId());
    Assert.assertNotNull(child2.getId());
  }

  @Test
  public void testFindByStartEndTimeInRangeAndDetectionConfigId() {
    long detectionConfigId = detectionConfigDAO.save(mockDetectionConfig());
    List<AnomalyDTO> anomalies = mockAnomalies(detectionConfigId);
    for (AnomalyDTO anomaly : anomalies) {
      this.mergedAnomalyResultDAO.save(anomaly);
    }
    List<AnomalyDTO> fetchedAnomalies = mergedAnomalyResultDAO
        .findByStartEndTimeInRangeAndDetectionConfigId(
            new DateTime(2019, 1, 1, 0, 0).getMillis(),
            new DateTime(2019, 1, 3, 0, 0).getMillis(),
            detectionConfigId, null);
    Assert.assertEquals(fetchedAnomalies.size(), anomalies.size());
    for (int i = 0; i < anomalies.size(); i++) {
      AnomalyDTO actual = fetchedAnomalies.get(i);
      AnomalyDTO expected = anomalies.get(i);
      Assert.assertNotNull(actual.getId());
      Assert.assertEquals(actual.getDetectionConfigId(), expected.getDetectionConfigId());
    }
    // Clean up
    for (int i = 0; i < anomalies.size(); i++) {
      this.mergedAnomalyResultDAO.delete(fetchedAnomalies.get(i));
    }
    this.detectionConfigDAO.deleteById(detectionConfigId);
  }

  @Test
  public void testFindByStartTimeInRangeAndDetectionConfigId() {
    long detectionConfigId = detectionConfigDAO.save(mockDetectionConfig());
    List<AnomalyDTO> anomalies = mockAnomalies(detectionConfigId);
    for (AnomalyDTO anomaly : anomalies) {
      this.mergedAnomalyResultDAO.save(anomaly);
    }
    List<AnomalyDTO> fetchedAnomalies = mergedAnomalyResultDAO
        .findByStartEndTimeInRangeAndDetectionConfigId(
            new DateTime(2019, 1, 1, 0, 0).getMillis(),
            new DateTime(2019, 1, 3, 0, 0).getMillis(),
            detectionConfigId, null);
    Assert.assertEquals(fetchedAnomalies.size(), anomalies.size());
    for (int i = 0; i < anomalies.size(); i++) {
      AnomalyDTO actual = fetchedAnomalies.get(i);
      AnomalyDTO expected = anomalies.get(i);
      Assert.assertNotNull(actual.getId());
      Assert.assertEquals(actual.getDetectionConfigId(), expected.getDetectionConfigId());
    }
    // Clean up
    for (int i = 0; i < anomalies.size(); i++) {
      this.mergedAnomalyResultDAO.delete(fetchedAnomalies.get(i));
    }
    this.detectionConfigDAO.deleteById(detectionConfigId);
  }

  @Test
  public void testSaveChildrenIndependently() {
    AnomalyDTO parent = new AnomalyDTO();
    parent.setStartTime(1000);
    parent.setEndTime(2000);

    AnomalyDTO child1 = new AnomalyDTO();
    child1.setStartTime(1000);
    child1.setEndTime(1500);

    AnomalyDTO child2 = new AnomalyDTO();
    child2.setStartTime(1500);
    child2.setEndTime(2000);

    parent.setChildren(new HashSet<>(Arrays.asList(child1, child2)));

    long id = this.mergedAnomalyResultDAO.save(child1);
    this.mergedAnomalyResultDAO.save(parent);

    Assert.assertNotNull(parent.getId());
    Assert.assertEquals(child1.getId().longValue(), id);
    Assert.assertNotNull(child2.getId());
  }

  @Test
  public void testSaveAndLoadHierarchicalAnomalies() {
    AnomalyDTO parent = new AnomalyDTO();
    parent.setStartTime(1000);
    parent.setEndTime(2000);

    AnomalyDTO child1 = new AnomalyDTO();
    child1.setStartTime(1000);
    child1.setEndTime(1500);

    AnomalyDTO child2 = new AnomalyDTO();
    child2.setStartTime(1500);
    child2.setEndTime(2000);

    AnomalyDTO child3 = new AnomalyDTO();
    child3.setStartTime(1600);
    child3.setEndTime(1800);

    child2.setChildren(new HashSet<>(Arrays.asList(child3)));
    parent.setChildren(new HashSet<>(Arrays.asList(child1, child2)));

    long parentId = this.mergedAnomalyResultDAO.save(parent);

    AnomalyDTO read = this.mergedAnomalyResultDAO.findById(parentId);

    assertThat(read).isNotSameAs(parent);
    Assert.assertEquals(read.getStartTime(), 1000);
    Assert.assertEquals(read.getEndTime(), 2000);
    Assert.assertFalse(read.isChild());
    Assert.assertFalse(read.getChildren().isEmpty());

    List<AnomalyDTO> readChildren = new ArrayList<>(read.getChildren());
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
    AnomalyDTO parent = new AnomalyDTO();
    parent.setStartTime(1000);
    parent.setEndTime(2000);

    AnomalyDTO child1 = new AnomalyDTO();
    child1.setStartTime(1000);
    child1.setEndTime(1500);

    AnomalyDTO child2 = new AnomalyDTO();
    child2.setStartTime(1500);
    child2.setEndTime(2000);

    AnomalyDTO child3 = new AnomalyDTO();
    child3.setStartTime(1600);
    child3.setEndTime(1800);

    child1.setChildren(new HashSet<>(Arrays.asList(child2)));
    parent.setChildren(new HashSet<>(Arrays.asList(child1)));

    this.mergedAnomalyResultDAO.save(parent);

    child2.setChildren(new HashSet<>(Arrays.asList(child3)));

    this.mergedAnomalyResultDAO.save(parent);

    AnomalyDTO read = this.mergedAnomalyResultDAO.findById(parent.getId());
    Assert.assertFalse(read.getChildren().isEmpty());
    Assert.assertEquals(read.getChildren().iterator().next().getStartTime(), 1000);
    Assert.assertFalse(read.getChildren().iterator().next().getChildren().isEmpty());
    Assert.assertEquals(
        read.getChildren().iterator().next().getChildren().iterator().next().getStartTime(), 1500);
    Assert.assertFalse(
        read.getChildren().iterator().next().getChildren().iterator().next().getChildren()
            .isEmpty());
    Assert.assertEquals(
        read.getChildren().iterator().next().getChildren().iterator().next().getChildren()
            .iterator().next().getStartTime(), 1600);
  }

  @Test
  public void testFindParent() {
    AnomalyDTO top = new AnomalyDTO();
    top.setDetectionConfigId(1L);
    top.setStartTime(1000);
    top.setEndTime(3000);

    AnomalyDTO child1 = new AnomalyDTO();
    child1.setDetectionConfigId(1L);
    child1.setStartTime(1000);
    child1.setEndTime(2000);

    AnomalyDTO child2 = new AnomalyDTO();
    child2.setDetectionConfigId(1L);
    child2.setStartTime(1200);
    child2.setEndTime(1800);

    AnomalyDTO child3 = new AnomalyDTO();
    child3.setDetectionConfigId(1L);
    child3.setStartTime(1500);
    child3.setEndTime(3000);

    child1.setChildren(new HashSet<>(Collections.singletonList(child2)));
    top.setChildren(new HashSet<>(Arrays.asList(child1, child3)));

    long topId = this.mergedAnomalyResultDAO.save(top);
    AnomalyDTO topNode = this.mergedAnomalyResultDAO.findById(topId);
    AnomalyDTO parent = null;
    AnomalyDTO leafNode = null;
    for (AnomalyDTO intermediate : topNode.getChildren()) {
      if (!intermediate.getChildren().isEmpty()) {
        parent = intermediate;
        leafNode = intermediate.getChildren().iterator().next();
      }
    }
    Assert.assertNotNull(parent);
    Assert.assertEquals(parent, this.mergedAnomalyResultDAO.findParent(leafNode));
  }

  public static AlertDTO mockDetectionConfig() {
    AlertDTO detectionConfig = new AlertDTO();
    detectionConfig.setName("Only For Test");
    return detectionConfig;
  }

  public static List<AnomalyDTO> mockAnomalies(long detectionConfigId) {
    AnomalyDTO anomaly1 = new AnomalyDTO();
    anomaly1.setMetric("metric");
    anomaly1.setDetectionConfigId(detectionConfigId);
    anomaly1.setStartTime(new DateTime(2019, 1, 1, 0, 0).getMillis());
    anomaly1.setEndTime(new DateTime(2019, 1, 1, 12, 0).getMillis());
    AnomalyDTO anomaly2 = new AnomalyDTO();
    anomaly2.setMetric("metric");
    anomaly2.setDetectionConfigId(detectionConfigId);
    anomaly2.setStartTime(new DateTime(2019, 1, 2, 10, 0).getMillis());
    anomaly2.setEndTime(new DateTime(2019, 1, 2, 20, 0).getMillis());

    return Arrays.asList(anomaly1, anomaly2);
  }
}
