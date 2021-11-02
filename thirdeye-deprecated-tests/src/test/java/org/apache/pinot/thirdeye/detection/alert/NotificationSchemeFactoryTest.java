package org.apache.pinot.thirdeye.detection.alert;

import static org.mockito.Mockito.mock;

import com.codahale.metrics.MetricRegistry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.datalayer.bao.TestDbEnv;
import org.apache.pinot.thirdeye.datasource.DAORegistry;
import org.apache.pinot.thirdeye.notification.content.templates.EntityGroupKeyContent;
import org.apache.pinot.thirdeye.notification.content.templates.MetricAnomaliesContent;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NotificationSchemeFactoryTest {

  private TestDbEnv testDAOProvider;
  private SubscriptionGroupDTO alertConfigDTO;
  private SubscriptionGroupManager alertConfigDAO;
  private NotificationSchemesDto alerters;
  private NotificationSchemeFactory notificationSchemeFactory;

  @BeforeMethod
  public void beforeMethod() throws Exception {
    Map<String, Object> randomAlerter = new HashMap<>();
    randomAlerter
        .put("className", "org.apache.pinot.thirdeye.detection.alert.scheme.RandomAlerter");
    Map<String, Object> anotherRandomAlerter = new HashMap<>();
    anotherRandomAlerter
        .put("className", "org.apache.pinot.thirdeye.detection.alert.scheme.AnotherRandomAlerter");

    alerters = new NotificationSchemesDto();

    this.testDAOProvider = new TestDbEnv();
    DAORegistry daoRegistry = TestDbEnv.getInstance();
    this.alertConfigDAO = daoRegistry.getDetectionAlertConfigManager();
    this.alertConfigDTO = new SubscriptionGroupDTO();

    notificationSchemeFactory = new NotificationSchemeFactory(mock(DataProvider.class),
        mock(MergedAnomalyResultManager.class),
        mock(AlertManager.class),
        new ThirdEyeServerConfiguration(),
        mock(EntityGroupKeyContent.class),
        mock(MetricAnomaliesContent.class),
        new MetricRegistry());
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {
    testDAOProvider.cleanup();
  }

  private SubscriptionGroupDTO createAlertConfig(NotificationSchemesDto schemes, String filter) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("className", filter);
    properties.put("detectionConfigIds", Collections.singletonList(1000));
    Map<Long, Long> vectorClocks = new HashMap<>();

    this.alertConfigDTO = new SubscriptionGroupDTO();
    this.alertConfigDTO.setNotificationSchemes(schemes);
    this.alertConfigDTO.setProperties(properties);
    this.alertConfigDTO.setFrom("te@linkedin.com");
    this.alertConfigDTO.setName("factory_alert");
    this.alertConfigDTO.setVectorClocks(vectorClocks);
    this.alertConfigDAO.save(this.alertConfigDTO);

    return this.alertConfigDTO;
  }

  @Test(enabled = false)
  public void testLoadAlertFilter() throws Exception {
    SubscriptionGroupDTO alertConfig = createAlertConfig(alerters,
        "org.apache.pinot.thirdeye.detection.alert.filter.ToAllRecipientsDetectionAlertFilter");
    long endTime = 9999l;
    DetectionAlertFilter detectionAlertFilter = notificationSchemeFactory
        .loadAlertFilter(alertConfig, endTime);

    Assert.assertEquals(detectionAlertFilter.config.getId().longValue(),
        alertConfig.getId().longValue());
    Assert.assertEquals(detectionAlertFilter.endTime, endTime);
    Assert.assertEquals(detectionAlertFilter.getClass().getSimpleName(),
        "ToAllRecipientsDetectionAlertFilter");
  }
}
