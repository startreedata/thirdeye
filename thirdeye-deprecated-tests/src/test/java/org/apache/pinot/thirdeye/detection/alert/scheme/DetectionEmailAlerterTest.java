/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pinot.thirdeye.detection.alert.scheme;

import static org.apache.pinot.thirdeye.detection.alert.filter.AlertFilterUtils.PROP_BCC;
import static org.apache.pinot.thirdeye.detection.alert.filter.AlertFilterUtils.PROP_BCC_VALUE;
import static org.apache.pinot.thirdeye.detection.alert.filter.AlertFilterUtils.PROP_CC;
import static org.apache.pinot.thirdeye.detection.alert.filter.AlertFilterUtils.PROP_CC_VALUE;
import static org.apache.pinot.thirdeye.detection.alert.filter.AlertFilterUtils.PROP_RECIPIENTS;
import static org.apache.pinot.thirdeye.detection.alert.filter.AlertFilterUtils.PROP_TO;
import static org.apache.pinot.thirdeye.detection.alert.filter.AlertFilterUtils.PROP_TO_VALUE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.mail.Session;
import org.apache.commons.mail.HtmlEmail;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.detection.alert.filter.SubscriptionUtils;
import org.apache.pinot.thirdeye.notification.commons.AlerterConfigurations;
import org.apache.pinot.thirdeye.notification.commons.EmailEntity;
import org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration;
import org.apache.pinot.thirdeye.notification.content.BaseNotificationContent;
import org.apache.pinot.thirdeye.notification.content.templates.MetricAnomaliesContent;
import org.apache.pinot.thirdeye.restclient.MockThirdEyeRcaRestClient;
import org.apache.pinot.thirdeye.restclient.ThirdEyeRcaRestClient;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DetectionEmailAlerterTest {

  private static final String PROP_CLASS_NAME = "className";
  private static final String PROP_DETECTION_CONFIG_IDS = "detectionConfigIds";
  private static final String FROM_ADDRESS_VALUE = "test3@test.test";
  private static final String ALERT_NAME_VALUE = "alert_name";
  private static final String DASHBOARD_HOST_VALUE = "dashboard";
  private static final String COLLECTION_VALUE = "test_dataset";
  private static final String DETECTION_NAME_VALUE = "test detection";
  private static final String METRIC_VALUE = "test_metric";

  private MergedAnomalyResultManager anomalyDAO;
  private SubscriptionGroupDTO subscriptionGroupDTO;
  private ThirdEyeCoordinatorConfiguration thirdEyeConfig;
  private AlertManager detectionConfigManager;

  @BeforeMethod
  public void beforeMethod() {
    detectionConfigManager = mock(AlertManager.class);

    final AlertDTO alertDTO = new AlertDTO();
    alertDTO.setName(DETECTION_NAME_VALUE);
    alertDTO.setId(1L);
    when(detectionConfigManager.findById(alertDTO.getId())).thenReturn(alertDTO);

    this.subscriptionGroupDTO = new SubscriptionGroupDTO();
    Map<String, Object> properties = new HashMap<>();
    properties.put(PROP_CLASS_NAME,
        "org.apache.pinot.thirdeye.detection.alert.filter.ToAllRecipientsDetectionAlertFilter");
    properties.put(PROP_DETECTION_CONFIG_IDS, Collections.singletonList(alertDTO.getId()));

    Map<String, Set<String>> recipients = new HashMap<>();
    recipients.put(PROP_TO, PROP_TO_VALUE);
    recipients.put(PROP_CC, PROP_CC_VALUE);
    recipients.put(PROP_BCC, PROP_BCC_VALUE);

    Map<String, Object> emailScheme = new HashMap<>();
    emailScheme.put("className", "org.apache.pinot.thirdeye.detection.alert.scheme.RandomAlerter");
    emailScheme.put(PROP_RECIPIENTS, recipients);
    this.subscriptionGroupDTO.setAlertSchemes(Collections.singletonMap("emailScheme", emailScheme));
    this.subscriptionGroupDTO.setProperties(properties);
    this.subscriptionGroupDTO.setFrom(FROM_ADDRESS_VALUE);
    this.subscriptionGroupDTO.setName(ALERT_NAME_VALUE);
    Map<Long, Long> vectorClocks = new HashMap<>();
    this.subscriptionGroupDTO.setVectorClocks(vectorClocks);
    this.subscriptionGroupDTO.setId((long) 2);

    anomalyDAO = mock(MergedAnomalyResultManager.class);
    MergedAnomalyResultDTO anomalyResultDTO = new MergedAnomalyResultDTO();
    anomalyResultDTO.setStartTime(1000L);
    anomalyResultDTO.setEndTime(2000L);
    anomalyResultDTO.setDetectionConfigId(alertDTO.getId());
    anomalyResultDTO.setCollection(COLLECTION_VALUE);
    anomalyResultDTO.setMetric(METRIC_VALUE);
    anomalyResultDTO.setId(11L);

    MergedAnomalyResultDTO anomalyResultDTO2 = new MergedAnomalyResultDTO();
    anomalyResultDTO2.setStartTime(3000L);
    anomalyResultDTO2.setEndTime(4000L);
    anomalyResultDTO2.setDetectionConfigId(alertDTO.getId());
    anomalyResultDTO2.setCollection(COLLECTION_VALUE);
    anomalyResultDTO2.setMetric(METRIC_VALUE);
    anomalyResultDTO.setId(12L);
    when(anomalyDAO.findAll()).thenReturn(Arrays.asList(anomalyResultDTO, anomalyResultDTO2));

    thirdEyeConfig = new ThirdEyeCoordinatorConfiguration();
    thirdEyeConfig.setDashboardHost(DASHBOARD_HOST_VALUE);
    SmtpConfiguration smtpProperties = new SmtpConfiguration();
    smtpProperties.setSmtpHost("test");
    smtpProperties.setSmtpPort(25);
    AlerterConfigurations alerterProps = new AlerterConfigurations();
    alerterProps.setSmtpConfiguration(smtpProperties);
    thirdEyeConfig.setAlerterConfigurations(alerterProps);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {

  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testFailAlertWithNullResult() throws Exception {
    DetectionEmailAlerter alertTaskInfo = new DetectionEmailAlerter(this.subscriptionGroupDTO,
        this.thirdEyeConfig,
        null,
        mock(MetricConfigManager.class),
        mock(AlertManager.class),
        mock(EventManager.class),
        mock(MergedAnomalyResultManager.class));
    alertTaskInfo.run();
  }

  @Test
  public void testSendEmailSuccessful() throws Exception {
    Map<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> result = new HashMap<>();
    SubscriptionGroupDTO subsConfig = SubscriptionUtils.makeChildSubscriptionConfig(
        this.subscriptionGroupDTO,
        ConfigUtils.getMap(this.subscriptionGroupDTO.getAlertSchemes()),
        new HashMap<>());
    result.put(
        new DetectionAlertFilterNotification(subsConfig),
        new HashSet<>(this.anomalyDAO.findAll()));
    DetectionAlertFilterResult notificationResults = new DetectionAlertFilterResult(result);

    final HtmlEmail htmlEmail = mock(HtmlEmail.class);
    when(htmlEmail.getMailSession()).thenReturn(Session.getInstance(new Properties()));
    when(htmlEmail.send()).thenReturn("sent");

    Map<String, Object> expectedResponse = new HashMap<>();
    ThirdEyeRcaRestClient rcaClient = MockThirdEyeRcaRestClient.setupMockClient(expectedResponse);

    MetricAnomaliesContent metricAnomaliesContent = new MetricAnomaliesContent(rcaClient,
        mock(MetricConfigManager.class),
        mock(EventManager.class),
        detectionConfigManager,
        mock(MergedAnomalyResultManager.class));

    DetectionEmailAlerter emailAlerter = new DetectionEmailAlerter(subscriptionGroupDTO,
        thirdEyeConfig,
        notificationResults,
        mock(MetricConfigManager.class),
        detectionConfigManager,
        mock(EventManager.class),
        mock(MergedAnomalyResultManager.class)) {
      @Override
      protected HtmlEmail getHtmlContent(EmailEntity emailEntity) {
        return htmlEmail;
      }

      @Override
      protected BaseNotificationContent getNotificationContent(Properties emailClientConfigs) {
        return metricAnomaliesContent;
      }
    };
    // Executes successfully without errors
    emailAlerter.run();
  }
}
