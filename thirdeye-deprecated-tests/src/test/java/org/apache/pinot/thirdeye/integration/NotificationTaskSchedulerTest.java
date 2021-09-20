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

package org.apache.pinot.thirdeye.integration;

import static org.apache.pinot.thirdeye.datalayer.DaoTestUtils.getTestDatasetConfig;
import static org.apache.pinot.thirdeye.datalayer.DaoTestUtils.getTestMetricConfig;
import static org.apache.pinot.thirdeye.spi.Constants.CTX_INJECTOR;
import static org.mockito.Mockito.mock;

import com.google.inject.Injector;
import java.util.List;
import org.apache.pinot.thirdeye.datalayer.DaoTestUtils;
import org.apache.pinot.thirdeye.datalayer.bao.TestDbEnv;
import org.apache.pinot.thirdeye.detection.DefaultDataProvider;
import org.apache.pinot.thirdeye.detection.alert.filter.ToAllRecipientsDetectionAlertFilter;
import org.apache.pinot.thirdeye.detection.alert.scheme.EmailAlertScheme;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionAlertRegistry;
import org.apache.pinot.thirdeye.detection.annotation.registry.DetectionRegistry;
import org.apache.pinot.thirdeye.detection.cache.builder.AnomaliesCacheBuilder;
import org.apache.pinot.thirdeye.detection.cache.builder.TimeSeriesCacheBuilder;
import org.apache.pinot.thirdeye.detection.components.detectors.ThresholdRuleDetector;
import org.apache.pinot.thirdeye.scheduler.DetectionCronScheduler;
import org.apache.pinot.thirdeye.scheduler.SubscriptionCronScheduler;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.ApplicationManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EvaluationManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.ApplicationDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskDTO;
import org.apache.pinot.thirdeye.spi.datasource.loader.AggregationLoader;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.apache.pinot.thirdeye.spi.task.TaskType;
import org.quartz.SchedulerException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This tests the notification task scheduler.
 * The notification task scheduler should not schedule notification task if there is no anomaly
 * generated.
 */
public class NotificationTaskSchedulerTest {

  private final String detectionConfigFile = "/sample-detection-config.yml";
  private final String alertConfigFile = "/sample-alert-config.yml";
  private final String metric = "cost";
  private final String collection = "test-collection";

  private DetectionCronScheduler detectionJobScheduler = null;
  private SubscriptionCronScheduler alertJobScheduler = null;
  private TestDbEnv testDAOProvider = null;
  private MetricConfigManager metricDAO;
  private DatasetConfigManager datasetDAO;
  private EventManager eventDAO;
  private MergedAnomalyResultManager anomalyDAO;
  private TaskManager taskDAO;
  private EvaluationManager evaluationDAO;
  private ApplicationManager appDAO;
  private long detectionId;
  private AlertManager alertManager;
  private SubscriptionGroupManager subscriptionGroupManager;

  @BeforeClass
  void beforeClass() {
    testDAOProvider = new TestDbEnv();
    Injector injector = testDAOProvider.getInjector();

    metricDAO = injector.getInstance(MetricConfigManager.class);
    datasetDAO = injector.getInstance(DatasetConfigManager.class);
    eventDAO = injector.getInstance(EventManager.class);
    taskDAO = injector.getInstance(TaskManager.class);
    anomalyDAO = injector.getInstance(MergedAnomalyResultManager.class);
    evaluationDAO = injector.getInstance(EvaluationManager.class);
    appDAO = injector.getInstance(ApplicationManager.class);
    alertManager = injector.getInstance(AlertManager.class);
    subscriptionGroupManager = injector.getInstance(SubscriptionGroupManager.class);

    alertJobScheduler = injector.getInstance(SubscriptionCronScheduler.class);
    alertJobScheduler.addToContext(CTX_INJECTOR, injector);

    detectionJobScheduler = injector.getInstance(DetectionCronScheduler.class);
    detectionJobScheduler.addToContext(CTX_INJECTOR, injector);

    initRegistries();
  }

  @AfterClass(alwaysRun = true)
  void afterClass() throws Exception {
    cleanup_schedulers();
    testDAOProvider.cleanup();
  }

  void initRegistries() {
    DetectionRegistry.registerComponent(ThresholdRuleDetector.class.getName(), "THRESHOLD");
    DetectionAlertRegistry.getInstance()
        .registerAlertScheme("EMAIL", EmailAlertScheme.class.getName());
    DetectionAlertRegistry.getInstance().registerAlertFilter("DEFAULT_ALERTER_PIPELINE",
        ToAllRecipientsDetectionAlertFilter.class.getName());
  }

  private void cleanup_schedulers() throws SchedulerException {
    if (detectionJobScheduler != null) {
      detectionJobScheduler.shutdown();
    }
    if (alertJobScheduler != null) {
      alertJobScheduler.shutdown();
    }
  }

  private void setup() throws Exception {
    // create test dataset config
    datasetDAO.save(getTestDatasetConfig(collection));
    metricDAO.save(getTestMetricConfig(collection, metric, null));

    ApplicationDTO app = new ApplicationDTO();
    app.setApplication("thirdeye");
    app.setRecipients("test@test");
    this.appDAO.save(app);

    DataProvider provider = new DefaultDataProvider(metricDAO,
        datasetDAO,
        eventDAO,
        evaluationDAO,
        mock(AggregationLoader.class),
        mock(TimeSeriesCacheBuilder.class),
        mock(AnomaliesCacheBuilder.class));

    detectionId = alertManager
        .save(DaoTestUtils.getTestDetectionConfig(provider, detectionConfigFile));

    // create test alert configuration
    subscriptionGroupManager
        .save(DaoTestUtils.getTestDetectionAlertConfig(alertConfigFile));
  }

  @Test
  // TODO spyne fixme test runs too long
  public void testNotificationJobCreation() throws Exception {
    // setup test environment
    setup();

    // start detection scheduler
    detectionJobScheduler.start();

    // start alert scheduler
    alertJobScheduler.start();

    // check only detection task is created, but detection alert task is not created
    Thread.sleep(10000);
    List<TaskDTO> tasks = taskDAO.findAll();
    Assert.assertTrue(tasks.size() > 0);
    //Assert.assertTrue(tasks.stream().anyMatch(x -> x.getTaskType() == TaskConstants.TaskType.DETECTION));
    Assert.assertTrue(
        tasks.stream().noneMatch(x -> x.getTaskType() == TaskType.NOTIFICATION));

    // generate an anomaly
    MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setDetectionConfigId(detectionId);
    anomaly.setStartTime(System.currentTimeMillis() - 1000);
    anomaly.setEndTime(System.currentTimeMillis());
    anomalyDAO.save(anomaly);

    // check the detection alert task is created
    Thread.sleep(10000);
    tasks = taskDAO.findAll();
    Assert.assertTrue(tasks.size() > 0);
    Assert.assertTrue(
        tasks.stream().anyMatch(x -> x.getTaskType() == TaskType.NOTIFICATION));
  }
}
