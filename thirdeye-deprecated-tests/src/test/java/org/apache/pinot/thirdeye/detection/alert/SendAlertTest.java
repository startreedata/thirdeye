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

package org.apache.pinot.thirdeye.detection.alert;

import static org.mockito.Mockito.mock;

import com.codahale.metrics.MetricRegistry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.config.UiConfiguration;
import org.apache.pinot.thirdeye.datalayer.bao.TestDbEnv;
import org.apache.pinot.thirdeye.datasource.DAORegistry;
import org.apache.pinot.thirdeye.notification.NotificationServiceRegistry;
import org.apache.pinot.thirdeye.notification.commons.NotificationConfiguration;
import org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration;
import org.apache.pinot.thirdeye.notification.content.templates.EntityGroupKeyContent;
import org.apache.pinot.thirdeye.notification.content.templates.MetricAnomaliesContent;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.task.DetectionAlertTaskInfo;
import org.apache.pinot.thirdeye.task.TaskContext;
import org.apache.pinot.thirdeye.task.runner.NotificationTaskRunner;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SendAlertTest {

  private static final String PROP_CLASS_NAME = "className";
  private static final String PROP_DETECTION_CONFIG_IDS = "detectionConfigIds";
  private static final String FROM_ADDRESS_VALUE = "test3@test.test";
  private static final String ALERT_NAME_VALUE = "alert_name";
  private static final String DASHBOARD_HOST_VALUE = "dashboard";
  private static final String COLLECTION_VALUE = "test_dataset";
  private static final String DETECTION_NAME_VALUE = "test detection";
  private static final String METRIC_VALUE = "test_metric";

  private TestDbEnv testDAOProvider;
  private NotificationTaskRunner taskRunner;
  private SubscriptionGroupManager alertConfigDAO;
  private MergedAnomalyResultManager anomalyDAO;
  private AlertManager detectionDAO;
  private MetricConfigManager metricDAO;
  private DatasetConfigManager dataSetDAO;
  private SubscriptionGroupDTO alertConfigDTO;
  private Long alertConfigId;
  private Long detectionConfigId;

  @BeforeMethod
  public void beforeMethod() throws Exception {
    this.testDAOProvider = new TestDbEnv();
    DAORegistry daoRegistry = TestDbEnv.getInstance();
    this.alertConfigDAO = daoRegistry.getDetectionAlertConfigManager();
    this.anomalyDAO = daoRegistry.getMergedAnomalyResultDAO();
    this.detectionDAO = daoRegistry.getDetectionConfigManager();
    this.metricDAO = daoRegistry.getMetricConfigDAO();
    this.dataSetDAO = daoRegistry.getDatasetConfigDAO();

    MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    metricConfigDTO.setName(METRIC_VALUE);
    metricConfigDTO.setDataset(COLLECTION_VALUE);
    metricConfigDTO.setAlias(COLLECTION_VALUE + ":" + METRIC_VALUE);
    this.metricDAO.save(metricConfigDTO);

    AlertDTO detectionConfig = new AlertDTO();
    detectionConfig.setName(DETECTION_NAME_VALUE);
    detectionConfig.setActive(true);
    this.detectionConfigId = this.detectionDAO.save(detectionConfig);

    this.alertConfigDTO = new SubscriptionGroupDTO();
    Map<String, Object> properties = new HashMap<>();
    properties.put(PROP_CLASS_NAME,
        "org.apache.pinot.thirdeye.detection.alert.filter.ToAllRecipientsDetectionAlertFilter");
    properties.put(PROP_DETECTION_CONFIG_IDS, Collections.singletonList(this.detectionConfigId));

    this.alertConfigDTO.setNotificationSchemes(new NotificationSchemesDto());
    this.alertConfigDTO.setProperties(properties);
    this.alertConfigDTO.setFrom(FROM_ADDRESS_VALUE);
    this.alertConfigDTO.setName(ALERT_NAME_VALUE);
    Map<Long, Long> vectorClocks = new HashMap<>();
    vectorClocks.put(this.detectionConfigId, 0l);
    this.alertConfigDTO.setVectorClocks(vectorClocks);
    this.alertConfigId = this.alertConfigDAO.save(this.alertConfigDTO);

    MergedAnomalyResultDTO anomalyResultDTO = new MergedAnomalyResultDTO();
    anomalyResultDTO.setStartTime(1000L);
    anomalyResultDTO.setEndTime(2000L);
    anomalyResultDTO.setDetectionConfigId(this.detectionConfigId);
    anomalyResultDTO.setCollection(COLLECTION_VALUE);
    anomalyResultDTO.setMetric(METRIC_VALUE);
    this.anomalyDAO.save(anomalyResultDTO);

    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setDataset(COLLECTION_VALUE);
    this.dataSetDAO.save(datasetConfigDTO);

    final NotificationSchemeFactory notificationSchemeFactory = new NotificationSchemeFactory(null,
        null,
        null,
        null,
        mock(EntityGroupKeyContent.class),
        mock(MetricAnomaliesContent.class),
        new MetricRegistry(),
        mock(NotificationServiceRegistry.class));
    this.taskRunner = new NotificationTaskRunner(
        mock(NotificationServiceRegistry.class),
        notificationSchemeFactory,
        TestDbEnv.getInstance().getDetectionAlertConfigManager(),
        TestDbEnv.getInstance().getMergedAnomalyResultDAO(),
        new MetricRegistry());
  }

  @AfterMethod(alwaysRun = true)
  void afterMethod() {
    testDAOProvider.cleanup();
  }

  @Test(enabled = false)
  public void testSendAlert() throws Exception {
    DetectionAlertTaskInfo alertTaskInfo = new DetectionAlertTaskInfo();
    alertTaskInfo.setDetectionAlertConfigId(alertConfigId);

    SmtpConfiguration smtpProperties = new SmtpConfiguration();
    smtpProperties.setHost("test");
    smtpProperties.setPort(25);
    NotificationConfiguration alerterProps = new NotificationConfiguration();
    alerterProps.setSmtpConfiguration(smtpProperties);

    ThirdEyeServerConfiguration thirdEyeConfig = new ThirdEyeServerConfiguration();
    thirdEyeConfig.setUiConfiguration(new UiConfiguration().setExternalUrl(DASHBOARD_HOST_VALUE));
    thirdEyeConfig.setAlerterConfigurations(alerterProps);

    final TaskContext taskContext = new TaskContext()
        .setThirdEyeWorkerConfiguration(thirdEyeConfig);

    taskRunner.execute(alertTaskInfo, taskContext);

    SubscriptionGroupDTO alert = alertConfigDAO.findById(this.alertConfigId);
    Assert.assertTrue(alert.getVectorClocks().get(this.detectionConfigId) > 0);
  }
}
