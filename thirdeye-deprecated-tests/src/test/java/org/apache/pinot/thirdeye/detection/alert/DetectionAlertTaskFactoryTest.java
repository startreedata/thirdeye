package org.apache.pinot.thirdeye.detection.alert;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.pinot.thirdeye.anomaly.ThirdEyeWorkerConfiguration;
import org.apache.pinot.thirdeye.datalayer.bao.DAOTestBase;
import org.apache.pinot.thirdeye.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.datasource.DAORegistry;
import org.apache.pinot.thirdeye.detection.DataProvider;
import org.apache.pinot.thirdeye.detection.alert.scheme.DetectionAlertScheme;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DetectionAlertTaskFactoryTest {

  private DAOTestBase testDAOProvider;
  private SubscriptionGroupDTO alertConfigDTO;
  private SubscriptionGroupManager alertConfigDAO;
  private Map<String, Object> alerters;
  private DetectionAlertTaskFactory detectionAlertTaskFactory;

  @BeforeMethod
  public void beforeMethod() throws Exception {
    Map<String, Object> randomAlerter = new HashMap<>();
    randomAlerter
        .put("className", "org.apache.pinot.thirdeye.detection.alert.scheme.RandomAlerter");
    Map<String, Object> anotherRandomAlerter = new HashMap<>();
    anotherRandomAlerter
        .put("className", "org.apache.pinot.thirdeye.detection.alert.scheme.AnotherRandomAlerter");

    alerters = new HashMap<>();
    alerters.put("randomScheme", randomAlerter);
    alerters.put("anotherRandomScheme", anotherRandomAlerter);

    this.testDAOProvider = DAOTestBase.getInstance();
    DAORegistry daoRegistry = DAORegistry.getInstance();
    this.alertConfigDAO = daoRegistry.getDetectionAlertConfigManager();
    this.alertConfigDTO = new SubscriptionGroupDTO();

    detectionAlertTaskFactory = new DetectionAlertTaskFactory(mock(DataProvider.class));
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {
    testDAOProvider.cleanup();
  }

  private SubscriptionGroupDTO createAlertConfig(Map<String, Object> schemes, String filter) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("className", filter);
    properties.put("detectionConfigIds", Collections.singletonList(1000));
    Map<Long, Long> vectorClocks = new HashMap<>();

    this.alertConfigDTO = new SubscriptionGroupDTO();
    this.alertConfigDTO.setAlertSchemes(schemes);
    this.alertConfigDTO.setProperties(properties);
    this.alertConfigDTO.setFrom("te@linkedin.com");
    this.alertConfigDTO.setName("factory_alert");
    this.alertConfigDTO.setVectorClocks(vectorClocks);
    this.alertConfigDAO.save(this.alertConfigDTO);

    return this.alertConfigDTO;
  }

  @Test
  public void testLoadAlertFilter() throws Exception {
    SubscriptionGroupDTO alertConfig = createAlertConfig(alerters,
        "org.apache.pinot.thirdeye.detection.alert.filter.ToAllRecipientsDetectionAlertFilter");
    long endTime = 9999l;
    DetectionAlertFilter detectionAlertFilter = detectionAlertTaskFactory
        .loadAlertFilter(alertConfig, endTime);

    Assert.assertEquals(detectionAlertFilter.config.getId().longValue(),
        alertConfig.getId().longValue());
    Assert.assertEquals(detectionAlertFilter.endTime, endTime);
    Assert.assertEquals(detectionAlertFilter.getClass().getSimpleName(),
        "ToAllRecipientsDetectionAlertFilter");
  }

  @Test
  public void testLoadAlertSchemes() throws Exception {
    SubscriptionGroupDTO alertConfig = createAlertConfig(alerters,
        "org.apache.pinot.thirdeye.detection.alert.filter.ToAllRecipientsDetectionAlertFilter");
    Set<DetectionAlertScheme> detectionAlertSchemes = detectionAlertTaskFactory
        .loadAlertSchemes(alertConfig,
            new ThirdEyeWorkerConfiguration(), null);

    Assert.assertEquals(detectionAlertSchemes.size(), 2);
    Iterator<DetectionAlertScheme> alertSchemeIterator = detectionAlertSchemes.iterator();
    Assert.assertTrue(
        getAlerterSet().contains(alertSchemeIterator.next().getClass().getSimpleName()));
    Assert.assertTrue(
        getAlerterSet().contains(alertSchemeIterator.next().getClass().getSimpleName()));
  }

  /**
   * Check if an exception is thrown when the detection config id cannot be found
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testDefaultAlertSchemes() throws Exception {

    ThirdEyeWorkerConfiguration teConfig = new ThirdEyeWorkerConfiguration();
    teConfig.setAlerterConfiguration(new HashMap<>());

    detectionAlertTaskFactory.loadAlertSchemes(null, teConfig, null);
  }

  /**
   * Load the default thirdeye email alerter if no scheme is not configured
   */
  @Test
  public void testLoadDefaultAlertSchemes() throws Exception {
    SubscriptionGroupDTO alertConfig = createAlertConfig(Collections.emptyMap(),
        "org.apache.pinot.thirdeye.detection.alert.filter.ToAllRecipientsDetectionAlertFilter");

    ThirdEyeWorkerConfiguration teConfig = new ThirdEyeWorkerConfiguration();
    teConfig.setAlerterConfiguration(new HashMap<>());

    Set<DetectionAlertScheme> detectionAlertSchemes = detectionAlertTaskFactory
        .loadAlertSchemes(alertConfig,
            teConfig, null);

    Assert.assertEquals(detectionAlertSchemes.size(), 1);
    Assert.assertEquals(detectionAlertSchemes.iterator().next().getClass().getSimpleName(),
        "DetectionEmailAlerter");
  }

  private Set<String> getAlerterSet() {
    return new HashSet<>(Arrays.asList("RandomAlerter", "AnotherRandomAlerter"));
  }
}
