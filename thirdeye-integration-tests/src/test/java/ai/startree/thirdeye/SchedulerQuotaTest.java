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
package ai.startree.thirdeye;

import static ai.startree.thirdeye.DropwizardTestUtils.loadAlertApi;
import static ai.startree.thirdeye.HappyPathTest.assert200;
import static ai.startree.thirdeye.PinotDataSourceManager.PINOT_DATASET_NAME;
import static ai.startree.thirdeye.ThirdEyeTestClient.ALERT_LIST_TYPE;
import static ai.startree.thirdeye.ThirdEyeTestClient.DATASOURCE_LIST_TYPE;
import static ai.startree.thirdeye.ThirdEyeTestClient.TASK_LIST_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.aspect.TimeProvider;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.EmailSchemeApi;
import ai.startree.thirdeye.spi.api.NamespaceConfigurationApi;
import ai.startree.thirdeye.spi.api.NotificationSchemesApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.spi.api.TaskApi;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskSubType;
import ai.startree.thirdeye.spi.task.TaskType;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Scheduler Tasks Quota tests
 * - create datasource, dataset
 * - verify task quotas
 * - create alert and ensure first detection task creation
 * - create subscription group
 * - wait for detection and notification tasks to schedule & run
 * - verify that quota has been hit and no new tasks are being created
 * - create another task and subscription group
 * - verify that no new tasks are created except historical after create detection type
 */
public class SchedulerQuotaTest {

  private static final Logger log = LoggerFactory.getLogger(SchedulerQuotaTest.class);

  private static final AlertApi ALERT_API;

  private static final TimeProvider CLOCK = TimeProvider.instance();
  private final ThirdEyeIntegrationTestSupport support = new ThirdEyeIntegrationTestSupport(
      "schedulingquota/config/server.yaml"
  );
  private ThirdEyeTestClient client;
  private DataSourceApi pinotDataSourceApi;
  private long alertId;

  static {
    try {
      ALERT_API = loadAlertApi("/schedulingquota/payloads/alert.json");
    } catch (final IOException e) {
      throw new RuntimeException(String.format("Could not load quota alert json: %s", e));
    }
  }

  @BeforeClass
  public void beforeClass() throws Exception {
    // ensure time is controlled via the TimeProvider CLOCK - ie weaving is working correctly
    assertThat(CLOCK.isTimeMockWorking()).isTrue();

    support.setup();
    pinotDataSourceApi = support.getPinotDataSourceApi();
    client = support.getClient();
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    support.tearDown();
    CLOCK.useSystemTime();
  }

  @Test
  public void setUpData() {
    Response response = client.request("internal/ping").get();
    assert200(response);

    // create datasource
    response = client.request("api/data-sources")
        .post(Entity.json(List.of(pinotDataSourceApi)));
    assert200(response);
    final DataSourceApi dataSourceInResponse = response.readEntity(DATASOURCE_LIST_TYPE).getFirst();
    pinotDataSourceApi.setId(dataSourceInResponse.getId());

    // create dataset
    final MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add("dataSourceId", String.valueOf(pinotDataSourceApi.getId()));
    formData.add("datasetName", PINOT_DATASET_NAME);
    response = client.request("api/data-sources/onboard-dataset/")
        .post(Entity.form(formData));
    assert200(response);
  }

  @Test(dependsOnMethods = "setUpData")
  public void testVerifyTaskQuotas() {
    final Response response = client.request("api/workspace-configuration").get();
    assertThat(response.getStatus()).isEqualTo(200);
    final NamespaceConfigurationApi gotCfgApi = response.readEntity(
        NamespaceConfigurationApi.class);
    assertThat(gotCfgApi.getNamespaceQuotasConfiguration().getTaskQuotasConfiguration()
        .getMaximumDetectionTasksPerMonth()).isEqualTo(2);
    assertThat(gotCfgApi.getNamespaceQuotasConfiguration().getTaskQuotasConfiguration()
        .getMaximumNotificationTasksPerMonth()).isEqualTo(1);
  }

  @Test(dependsOnMethods = "testVerifyTaskQuotas")
  public void testTaskIsCreated() {
    CLOCK.useMockTime(new DateTime(2025, 1, 1, 0, 0, 1, DateTimeZone.UTC).getMillis());
    // create alert that schedules every 10 seconds
    final Response response = client.request("api/alerts")
        .post(Entity.json(List.of(ALERT_API)));
    assert200(response);
    alertId = response.readEntity(ALERT_LIST_TYPE).getFirst().getId();
  }

  @Test(dependsOnMethods = "testTaskIsCreated")
  public void testSubscriptionGroupIsCreated() {
    // create subscription group that notifies every 10 seconds
    final SubscriptionGroupApi subscriptionGroupApi = getSubscriptionGroupApi(
        "testSubscriptionFirst", alertId);
    ;
    final Response response = client.request("api/subscription-groups").post(
        Entity.json(List.of(subscriptionGroupApi)));
    assert200(response);
  }

  @Test(dependsOnMethods = "testSubscriptionGroupIsCreated")
  public void testTasksAfterEntityCreation() throws InterruptedException {
    final List<TaskApi> tasks = getTasks();
    assertThat(tasks).hasSize(1);
    TaskApi task = tasks.getFirst();
    assertThat(task.getTaskType()).isEqualTo(TaskType.DETECTION);
    assertThat(task.getTaskSubType()).isEqualTo(TaskSubType.DETECTION_HISTORICAL_DATA_AFTER_CREATE);

    // ensure the task completes
    while (task.getStatus() == TaskStatus.WAITING || task.getStatus() == TaskStatus.RUNNING) {
      task = getTasks().getFirst();
      Thread.sleep(500);
    }
    assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
  }

  @Test(dependsOnMethods = "testTasksAfterEntityCreation")
  public void testTasksAfterDelay() throws InterruptedException {
    // the registration of the quartz cron jobs for the detection and notification have not happened yet because time has not been ticked yet 
    // the crons schedulers are configure to update the quartz cron jobs every 1 second - 
    // tick 1 second to get the alert and subscription group quartz cron jobs created 
    CLOCK.tick(1000);
    // both alert and subscription group has cron for every 10 seconds - cron jobs are live but tasks should not exist yet
    assertThat(getTasks()).hasSize(1);

    // ticket by 9 seconds --> the alert and subscription should trigger
    CLOCK.tick(9000);
    // give thread to detectionCronScheduler and to quartz scheduler - 500 ms should be enough for task creation - quartz scheduler is notified of the clock increase
    List<TaskApi> tasks = getTasks();
    while (tasks.size() < 3) {
      // FIXME ANSHUL add timeout to the tests
      Thread.sleep(500);
      tasks = getTasks();
    }
    assertThat(tasks).hasSize(3);
    final Stream<TaskApi> detectionTasks = tasks.stream()
        .filter(e -> e.getTaskType() == TaskType.DETECTION);
    assertThat(detectionTasks).hasSize(2);
    final Stream<TaskApi> notificationTasks = tasks.stream()
        .filter(e -> e.getTaskType() == TaskType.NOTIFICATION);
    assertThat(notificationTasks).hasSize(1);

    // trigger cron schedule update again - quotas should be reached now and no new tasks should be created
    CLOCK.tick(1001);

    // ensure now new tasks are created in the next 30 seconds 
    for (int i = 0; i < 3; i++) {
      CLOCK.tick(10000);
      Thread.sleep(500);
      assertThat(getTasks()).hasSize(3);
    }
  }

  @Test(dependsOnMethods = "testTasksAfterDelay")
  public void testAnotherTaskIsCreated() {
    // create another alert that schedules every 10 seconds
    final Response response = client.request("api/alerts")
        .post(Entity.json(List.of(ALERT_API.setName("simple-threshold-pageviews-second"))));
    assert200(response);

    // ensure the onboarding task is created
    final List<TaskApi> tasks = getTasks();
    assertThat(tasks).hasSize(4);
    final TaskApi task = tasks.getLast();
    assertThat(task.getTaskType()).isEqualTo(TaskType.DETECTION);
    assertThat(task.getTaskSubType()).isEqualTo(TaskSubType.DETECTION_HISTORICAL_DATA_AFTER_CREATE);
  }

  @Test(dependsOnMethods = "testAnotherTaskIsCreated")
  public void testAnotherSubscriptionGroupIsCreated() {
    // create another subscription group that notifies every 10 seconds
    final SubscriptionGroupApi subscriptionGroupApi = getSubscriptionGroupApi(
        "testSubscriptionSecond", alertId);
    final Response response = client.request("api/subscription-groups").post(
        Entity.json(List.of(subscriptionGroupApi)));
    assert200(response);
  }

  @Test(dependsOnMethods = "testAnotherSubscriptionGroupIsCreated")
  public void testTasksAfterSecondDelay() throws InterruptedException {
    // ensure now new tasks are created in the next 30 seconds 
    for (int i = 0; i < 3; i++) {
      CLOCK.tick(10000);
      Thread.sleep(500);
      assertThat(getTasks()).hasSize(4);
    }
  }

  private List<TaskApi> getTasks() {
    final Response response = client.request("api/tasks").get();
    assert200(response);
    final List<TaskApi> taskApis = response.readEntity(TASK_LIST_TYPE);
    taskApis.sort(Comparator.comparingLong(TaskApi::getId));
    return taskApis;
  }

  private SubscriptionGroupApi getSubscriptionGroupApi(String name, Long alertId) {
    return new SubscriptionGroupApi().setName(name)
        .setCron("*/10 * * * * ?")
        .setNotificationSchemes(new NotificationSchemesApi().setEmail(
            new EmailSchemeApi().setTo(List.of("analyst@fake.mail"))))
        .setAlerts(List.of(new AlertApi().setId(alertId)));
  }
}
