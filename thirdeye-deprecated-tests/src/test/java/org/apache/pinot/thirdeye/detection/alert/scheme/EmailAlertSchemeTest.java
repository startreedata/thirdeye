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

import static org.apache.pinot.thirdeye.detection.alert.filter.AlertFilterUtils.PROP_BCC_VALUE;
import static org.apache.pinot.thirdeye.detection.alert.filter.AlertFilterUtils.PROP_CC_VALUE;
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
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.config.UiConfiguration;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.detection.alert.filter.SubscriptionUtils;
import org.apache.pinot.thirdeye.notification.commons.EmailEntity;
import org.apache.pinot.thirdeye.notification.commons.NotificationConfiguration;
import org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EmailAlertSchemeTest {

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
  private ThirdEyeServerConfiguration thirdEyeConfig;
  private AlertManager alertManager;

  @BeforeMethod
  public void beforeMethod() {
    alertManager = mock(AlertManager.class);

    final AlertDTO alertDTO = new AlertDTO();
    alertDTO.setName(DETECTION_NAME_VALUE);
    alertDTO.setId(1L);
    when(alertManager.findById(alertDTO.getId())).thenReturn(alertDTO);

    subscriptionGroupDTO = new SubscriptionGroupDTO();
    final Map<String, Object> properties = new HashMap<>();
    properties.put(PROP_CLASS_NAME,
        "org.apache.pinot.thirdeye.detection.alert.filter.ToAllRecipientsDetectionAlertFilter");
    properties.put(PROP_DETECTION_CONFIG_IDS, Collections.singletonList(alertDTO.getId()));

    final EmailSchemeDto recipients = new EmailSchemeDto()
    .setTo(PROP_TO_VALUE)
    .setCc(PROP_CC_VALUE)
    .setBcc(PROP_BCC_VALUE);

    subscriptionGroupDTO.setNotificationSchemes(new NotificationSchemesDto().setEmailScheme(recipients));
    subscriptionGroupDTO.setProperties(properties);
    subscriptionGroupDTO.setFrom(FROM_ADDRESS_VALUE);
    subscriptionGroupDTO.setName(ALERT_NAME_VALUE);
    final Map<Long, Long> vectorClocks = new HashMap<>();
    subscriptionGroupDTO.setVectorClocks(vectorClocks);
    subscriptionGroupDTO.setId((long) 2);

    anomalyDAO = mock(MergedAnomalyResultManager.class);
    final MergedAnomalyResultDTO anomalyResultDTO = new MergedAnomalyResultDTO();
    anomalyResultDTO.setStartTime(1000L);
    anomalyResultDTO.setEndTime(2000L);
    anomalyResultDTO.setDetectionConfigId(alertDTO.getId());
    anomalyResultDTO.setCollection(COLLECTION_VALUE);
    anomalyResultDTO.setMetric(METRIC_VALUE);
    anomalyResultDTO.setId(11L);

    final MergedAnomalyResultDTO anomalyResultDTO2 = new MergedAnomalyResultDTO();
    anomalyResultDTO2.setStartTime(3000L);
    anomalyResultDTO2.setEndTime(4000L);
    anomalyResultDTO2.setDetectionConfigId(alertDTO.getId());
    anomalyResultDTO2.setCollection(COLLECTION_VALUE);
    anomalyResultDTO2.setMetric(METRIC_VALUE);
    anomalyResultDTO.setId(12L);
    when(anomalyDAO.findAll()).thenReturn(Arrays.asList(anomalyResultDTO, anomalyResultDTO2));

    thirdEyeConfig = new ThirdEyeServerConfiguration();
    thirdEyeConfig.setUiConfiguration(new UiConfiguration().setExternalUrl(DASHBOARD_HOST_VALUE));
    final SmtpConfiguration smtpProperties = new SmtpConfiguration();
    smtpProperties.setHost("test");
    smtpProperties.setPort(25);
    final NotificationConfiguration alerterProps = new NotificationConfiguration();
    alerterProps.setSmtpConfiguration(smtpProperties);
    thirdEyeConfig.setAlerterConfigurations(alerterProps);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {

  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testFailAlertWithNullResult() throws Exception {
    final EmailAlertScheme alertTaskInfo = new EmailAlertScheme();
    alertTaskInfo.run(subscriptionGroupDTO, null);
  }

  @Test
  public void testSendEmailSuccessful() throws Exception {
    final Map<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> result = new HashMap<>();
    final SubscriptionGroupDTO subsConfig = SubscriptionUtils.makeChildSubscriptionConfig(
        subscriptionGroupDTO,
        subscriptionGroupDTO.getNotificationSchemes(),
        new HashMap<>());
    result.put(
        new DetectionAlertFilterNotification(subsConfig),
        new HashSet<>(anomalyDAO.findAll()));
    final DetectionAlertFilterResult notificationResults = new DetectionAlertFilterResult(result);

    final HtmlEmail htmlEmail = mock(HtmlEmail.class);
    when(htmlEmail.getMailSession()).thenReturn(Session.getInstance(new Properties()));
    when(htmlEmail.send()).thenReturn("sent");

    final EmailAlertScheme emailAlerter = new EmailAlertScheme() {
      @Override
      protected HtmlEmail getHtmlContent(final EmailEntity emailEntity) {
        return htmlEmail;
      }
    };
    // Executes successfully without errors
    emailAlerter.run(subscriptionGroupDTO, mock(DetectionAlertFilterResult.class));
  }
}
