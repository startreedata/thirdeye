/*
 * Copyright 2022 StarTree Inc
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

import static ai.startree.thirdeye.DropwizardTestUtils.buildClient;
import static ai.startree.thirdeye.DropwizardTestUtils.buildSupport;
import static ai.startree.thirdeye.DropwizardTestUtils.loadAlertApi;
import static ai.startree.thirdeye.PinotDataSourceManager.PINOT_DATASET_NAME;
import static ai.startree.thirdeye.PinotDataSourceManager.PINOT_DATA_SOURCE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.aspect.TimeProvider;
import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import io.dropwizard.testing.DropwizardTestSupport;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
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
 * -javaagent:[USER_PATH]/.m2/repository/org/aspectj/aspectjweaver/1.9.6/aspectjweaver-1.9.6.jar
 * IntelliJ does not use the pom surefire config: https://youtrack.jetbrains.com/issue/IDEA-52286
 */
// todo cyril pinot is not necessary - implement and use csv/in-memory datasource instead
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

  static {
    try {
      ALERT_API = loadAlertApi("/scheduling/payloads/alert.json");
    } catch (IOException e) {
      throw new RuntimeException(String.format("Could not load alert json: %s", e));
    }
  }

  private DropwizardTestSupport<ThirdEyeServerConfiguration> SUPPORT;
  private Client client;

  private long alertId;
  private DataSourceApi pinotDataSourceApi;

  @BeforeClass
  public void beforeClass() throws Exception {
    // ensure time is controlled via the TimeProvider CLOCK - ie weaving is working correctly
    assertThat(CLOCK.isTimeMockWorking()).isTrue();

    pinotDataSourceApi = PinotDataSourceManager.getPinotDataSourceApi();
    final DatabaseConfiguration dbConfiguration = MySqlTestDatabase.sharedDatabaseConfiguration();
    // Setup plugins dir so ThirdEye can load it
    IntegrationTestUtils.setupPluginsDirAbsolutePath();

    SUPPORT = buildSupport(dbConfiguration, "scheduling/config/server.yaml");
    SUPPORT.before();
    client = buildClient("scheduling-test-client", SUPPORT);
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    CLOCK.useSystemTime();
    log.info("Stopping Thirdeye at port: {}", SUPPORT.getLocalPort());
    SUPPORT.after();
    MySqlTestDatabase.cleanSharedDatabase();
  }

  @Test
  public void setUpData() {
    Response response = request("internal/ping").get();
    assertThat(response.getStatus()).isEqualTo(200);

    // create datasource
    response = request("api/data-sources")
        .post(Entity.json(List.of(pinotDataSourceApi)));
    assertThat(response.getStatus()).isEqualTo(200);

    // create dataset
    MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add("dataSourceName", PINOT_DATA_SOURCE_NAME);
    formData.add("datasetName", PINOT_DATASET_NAME);
    response = request("api/data-sources/onboard-dataset/")
        .post(Entity.form(formData));
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "setUpData")
  public void testCreateAlertLastTimestamp() {
    // fix clock : time is now controlled manually
    CLOCK.useMockTime(MARCH_24_2020_15H33);

    Response createResponse = request("api/alerts")
        .post(Entity.json(List.of(ALERT_API)));
    assertThat(createResponse.getStatus()).isEqualTo(200);
    List<Map<String, Object>> alerts = createResponse.readEntity(List.class);
    alertId = ((Number) alerts.get(0).get("id")).longValue();

    // time advancing should not impact lastTimestamp
    CLOCK.tick(5);

    // check that lastTimestamp just after creation is 0
    long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(PAGEVIEWS_DATASET_START_TIME);
  }

  @Test(dependsOnMethods = "testCreateAlertLastTimestamp", timeOut = 60000L)
  public void testOnboardingLastTimestamp() throws Exception {
    // wait for anomalies - proxy to know when the onboarding task has run
    List<Map<String, Object>> anomalies = List.of();
    while (anomalies.size() == 0) {
      // see taskDriver server config for optimization
      Thread.sleep(1000);
      anomalies = getAnomalies();
    }

    // check that lastTimestamp is the endTime of the Onboarding task: March 21 1H
    long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(MARCH_21_2020_00H00);
  }

  @Test(dependsOnMethods = "testOnboardingLastTimestamp", timeOut = 60000L)
  public void testAfterDetectionCronLastTimestamp() throws InterruptedException {
    // get current number of anomalies
    List<Map<String, Object>> anomalies = getAnomalies();
    int numAnomaliesBeforeDetectionRun = anomalies.size();

    // advance detection time to March 22, 2020, 00:00:00 UTC
    // this should trigger the cron - and a new anomaly is expected on [March 21 - March 22]
    CLOCK.useMockTime(MARCH_25_2020_05H00);
    // not exact time should not impact lastTimestamp
    CLOCK.tick(5);
    // give thread to detectionCronScheduler and to quartz scheduler - (quartz idle time is weaved to 100 ms for test speed)
    Thread.sleep(1000);

    // wait for the new anomaly to be created - proxy to know when the detection has run
    while (anomalies.size() == numAnomaliesBeforeDetectionRun) {
      Thread.sleep(1000);
      anomalies = getAnomalies();
    }

    // check that lastTimestamp after detection is the runTime of the cron
    long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(MARCH_22_2020_00H00);
  }

  @Test(dependsOnMethods = "testAfterDetectionCronLastTimestamp", timeOut = 60000L)
  public void testSecondAnomalyIsMerged() throws InterruptedException {
    List<Map<String, Object>> anomalies = getAnomalies();
    int numAnomaliesBeforeDetectionRun = anomalies.size();

    // advance detection time to March 23, 2020, 00:00:00 UTC
    // this should trigger the cron - and a new anomaly is expected on [March 22 - March 23]
    CLOCK.useMockTime(MARCH_26_2020_05H00);
    // not exact time should not impact lastTimestamp
    CLOCK.tick(5);
    // give thread to quartz scheduler - (quartz idle time is weaved to 1000 ms for test speed)
    Thread.sleep(1000);

    // wait for a new anomaly to be created - proxy to know when the detection has run
    while (anomalies.size() == numAnomaliesBeforeDetectionRun) {
      Thread.sleep(1000);
      anomalies = getAnomalies();
    }

    // check that lastTimestamp after detection is the runTime of the cron
    long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(MARCH_23_2020_00H00);

    // find anomalies starting on MARCH 21 - there should be 2
    List<Map<String, Object>> march21Anomalies = anomalies.stream()
        .filter(a -> (long) a.get("startTime") == MARCH_21_2020_00H00)
        .collect(Collectors.toList());
    assertThat(march21Anomalies.size()).isEqualTo(2);
    // check that one anomaly finishes on MARCH 22: the child anomaly
    assertThat(march21Anomalies.stream()
        .anyMatch(a -> (long) a.get("endTime") == MARCH_22_2020_00H00)).isTrue();
    // check that one anomaly finishes on MARCH 23: the parent anomaly
    assertThat(march21Anomalies.stream()
        .anyMatch(a -> (long) a.get("endTime") == MARCH_23_2020_00H00)).isTrue();
  }

  private List<Map<String, Object>> getAnomalies() {
    Response response = request("api/anomalies").get();
    assertThat(response.getStatus()).isEqualTo(200);
    return response.readEntity(List.class);
  }

  private long getAlertLastTimestamp() {
    Response getResponse = request("api/alerts/" + alertId).get();
    assertThat(getResponse.getStatus()).isEqualTo(200);
    Map<String, Object> alert = getResponse.readEntity(Map.class);
    long alertLastTimestamp = ((Number) alert.get("lastTimestamp")).longValue();
    return alertLastTimestamp;
  }

  private Builder request(final String urlFragment) {
    return client.target(endPoint(urlFragment)).request();
  }

  private String endPoint(final String pathFragment) {
    return String.format("http://localhost:%d/%s", SUPPORT.getLocalPort(), pathFragment);
  }
}


