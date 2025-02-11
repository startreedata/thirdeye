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
import static ai.startree.thirdeye.ThirdEyeTestClient.ANOMALIES_LIST_TYPE;
import static ai.startree.thirdeye.ThirdEyeTestClient.DATASOURCE_LIST_TYPE;
import static ai.startree.thirdeye.ThirdEyeTestClient.TASK_LIST_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.aspect.TimeProvider;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
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
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Scheduler tests. Time is mocked with the TimeProvider.
 * - create datasource, dataset
 * - fix time
 * - create alert. check lastTimestamp
 * - wait for onboarding task to run. Check lastTimestamp
 * - advance time to next cron run.
 * - wait for detection task to run. Check lastTimestamp
 *
 * Note: if run within IntelliJ, run with the following JVM option:
 * -javaagent:[USER_PATH]/.m2/repository/org/aspectj/aspectjweaver/1.9.21/aspectjweaver-1.9.21.jar --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED
 * IntelliJ does not use the pom surefire config: https://youtrack.jetbrains.com/issue/IDEA-52286
 */
public class SchedulingTest {

  private static final Logger log = LoggerFactory.getLogger(SchedulingTest.class);

  private static final AlertApi ALERT_API;

  private static final TimeProvider CLOCK = TimeProvider.instance();
  private static final long PAGEVIEWS_DATASET_START_TIME = 1580601600000L;
  // alert can be created at any time in the day
  private static final long MARCH_24_2020_15H33 = 1585063980_000L;
  // = MARCH_24_2020_15H33 - delay P3D and floor granularity P1D (config in alert json)
  private static final long MARCH_21_2020_00H00 = 1584748800_000L;

  private static final long MARCH_25_2020_05H00 = 1585112400_000L;
  // = MARCH_25_2020_05H00 - delay P3D and floor granularity P1D (see config in alert json)
  private static final long MARCH_22_2020_00H00 = 1584835200_000L;

  private static final long MARCH_26_2020_05H00 = 1585198800_000L;
  // = MARCH_26_2020_05H00 - delay P3D and floor granularity P1D (see config in alert json)
  private static final long MARCH_23_2020_00H00 = 1584921600_000L;
  public static final Set<TaskStatus> TASK_PENDING_STATUSES = Set.of(TaskStatus.WAITING, TaskStatus.RUNNING);

  static {
    try {
      ALERT_API = loadAlertApi("/scheduling/payloads/alert.json");
    } catch (final IOException e) {
      throw new RuntimeException(String.format("Could not load alert json: %s", e));
    }
  }

  private final ThirdEyeIntegrationTestSupport support = new ThirdEyeIntegrationTestSupport(
      "scheduling/config/server.yaml"
  );
  private ThirdEyeTestClient client;
  private long alertId;
  private DataSourceApi pinotDataSourceApi;
  private Long onboardingTaskId;

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
    CLOCK.useSystemTime();
    support.tearDown();
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
  public void testCreateAlertLastTimestamp() {
    // fix clock : time is now controlled manually
    CLOCK.useMockTime(MARCH_24_2020_15H33);

    final Response createResponse = client.request("api/alerts")
        .post(Entity.json(List.of(ALERT_API)));
    assertThat(createResponse.getStatus()).isEqualTo(200);
    final List<AlertApi> alerts = createResponse.readEntity(ALERT_LIST_TYPE);
    alertId = alerts.getFirst().getId();

    // time advancing should not impact lastTimestamp
    CLOCK.tick(5);

    // check that lastTimestamp just after creation is set to the start of the dataset
    final long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(PAGEVIEWS_DATASET_START_TIME);
  }

  @Test(dependsOnMethods = "testCreateAlertLastTimestamp", timeOut = 60000L)
  public void testTaskIsCreated() throws Exception {
    final List<TaskApi> tasks = getTasks();
    assertThat(tasks).hasSize(1);
    final TaskApi task = tasks.getFirst();
    assertThat(task.getTaskType()).isEqualTo(TaskType.DETECTION);
    assertThat(task.getTaskSubType()).isEqualTo(TaskSubType.DETECTION_HISTORICAL_DATA_AFTER_CREATE);
    onboardingTaskId = task.getId(); 
  }

  @Test(dependsOnMethods = "testTaskIsCreated", timeOut = 60000L)
  public void testOnboardingLastTimestamp() throws Exception {
    // wait for onboarding task to be completed
    TaskApi onboardingTask = getTask(onboardingTaskId);
    while (TASK_PENDING_STATUSES.contains(onboardingTask.getStatus())) {
      // see taskDriver server config for optimization
      Thread.sleep(500);
      onboardingTask = getTask(onboardingTaskId); 
    }

    // check that lastTimestamp is the endTime of the Onboarding task: March 21 1H
    final long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(MARCH_21_2020_00H00);
  }

  @Test(dependsOnMethods = "testOnboardingLastTimestamp", timeOut = 60000L)
  public void testAfterDetectionCronLastTimestamp() throws InterruptedException {
    // advance detection time to March 22, 2020, 00:00:00 UTC
    // this should trigger the cron - and a new anomaly is expected on [March 21 - March 22]
    CLOCK.useMockTime(MARCH_25_2020_05H00);
    // not exact time should not impact lastTimestamp
    CLOCK.tick(5);

    // wait for the new task to be created - proxy to know when the detection is triggered
    List<TaskApi> tasks = getTasks();
    while (tasks.size() == 1 || TASK_PENDING_STATUSES.contains(tasks.getLast().getStatus())) {
      Thread.sleep(500);
      tasks = getTasks();
    }
     assertThat(tasks.getLast().getTaskSubType()).isEqualTo(TaskSubType.DETECTION_TRIGGERED_BY_CRON);

    // check that lastTimestamp after detection is the runTime of the cron
    final long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(MARCH_22_2020_00H00);
  }

  @Test(dependsOnMethods = "testAfterDetectionCronLastTimestamp", timeOut = 60000L)
  public void testSecondAnomalyIsMerged() throws InterruptedException {
    List<AnomalyApi> anomalies = getAnomalies();
    final int numAnomaliesBeforeDetectionRun = anomalies.size();

    // advance detection time to March 23, 2020, 00:00:00 UTC
    // this should trigger the cron - and a new anomaly is expected on [March 22 - March 23]
    CLOCK.useMockTime(MARCH_26_2020_05H00);
    // not exact time should not impact lastTimestamp
    CLOCK.tick(5);

    // wait for a new anomaly to be created - proxy to know when the detection has run
    while (anomalies.size() == numAnomaliesBeforeDetectionRun) {
      Thread.sleep(500);
      anomalies = getAnomalies();
    }

    // check that lastTimestamp after detection is the runTime of the cron
    final long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(MARCH_23_2020_00H00);

    // find anomalies starting on MARCH 21 - there should be 2
    final List<AnomalyApi> march21Anomalies = anomalies.stream()
        .filter(a -> a.getStartTime().getTime() == MARCH_21_2020_00H00)
        .toList();
    assertThat(march21Anomalies.size()).isEqualTo(2);
    // check that one anomaly finishes on MARCH 22: the child anomaly
    assertThat(march21Anomalies.stream()
        .anyMatch(a -> a.getEndTime().getTime() == MARCH_22_2020_00H00)).isTrue();
    // check that one anomaly finishes on MARCH 23: the parent anomaly
    assertThat(march21Anomalies.stream()
        .anyMatch(a -> a.getEndTime().getTime() == MARCH_23_2020_00H00)).isTrue();
  }

  private List<AnomalyApi> getAnomalies() {
    final Response response = client.request("api/anomalies").get();
    assert200(response);
    return response.readEntity(ANOMALIES_LIST_TYPE);
  }

  private List<TaskApi> getTasks() {
    final Response response = client.request("api/tasks").get();
    assert200(response);
    final List<TaskApi> taskApis = response.readEntity(TASK_LIST_TYPE);
    taskApis.sort(Comparator.comparingLong(TaskApi::getId));
    return taskApis;
  }

  private TaskApi getTask(final long id) {
    final Response response = client.request("api/tasks/" + id).get();
    assert200(response);
    return response.readEntity(TaskApi.class);
  }

  private long getAlertLastTimestamp() {
    final Response getResponse = client.request("api/alerts/" + alertId).get();
    assertThat(getResponse.getStatus()).isEqualTo(200);
    final AlertApi alert = getResponse.readEntity(AlertApi.class);
    return alert.getLastTimestamp().getTime();
  }
}


