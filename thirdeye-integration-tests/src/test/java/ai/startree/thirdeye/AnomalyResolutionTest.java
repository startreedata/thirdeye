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

import static ai.startree.thirdeye.DropwizardTestUtils.buildClient;
import static ai.startree.thirdeye.DropwizardTestUtils.buildSupport;
import static ai.startree.thirdeye.DropwizardTestUtils.loadAlertApi;
import static ai.startree.thirdeye.DropwizardTestUtils.loadApi;
import static ai.startree.thirdeye.HappyPathTest.assert200;
import static ai.startree.thirdeye.PinotDataSourceManager.PINOT_DATASET_NAME;
import static ai.startree.thirdeye.PinotDataSourceManager.PINOT_DATA_SOURCE_NAME;
import static ai.startree.thirdeye.ThirdEyeTestClient.ALERT_LIST_TYPE;
import static ai.startree.thirdeye.ThirdEyeTestClient.SUBSCRIPTION_GROUP_LIST_TYPE;
import static ai.startree.thirdeye.datalayer.MySqlTestDatabase.useLocalMysqlInstance;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.aspect.TimeProvider;
import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import ai.startree.thirdeye.notification.NotificationServiceRegistry;
import ai.startree.thirdeye.notification.NotificationTaskFilter;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertAssociationApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import com.google.inject.Injector;
import io.dropwizard.testing.DropwizardTestSupport;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Future;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
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
 * -javaagent:[USER_PATH]/.m2/repository/org/aspectj/aspectjweaver/1.9.21/aspectjweaver-1.9.21.jar
 * --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED
 * --add-opens java.base/java.util=ALL-UNNAMED
 * IntelliJ does not use the pom surefire config: https://youtrack.jetbrains.com/issue/IDEA-52286
 */
public class AnomalyResolutionTest {

  public static final ZoneOffset UTC = ZoneOffset.UTC;
  private static final Logger log = LoggerFactory.getLogger(AnomalyResolutionTest.class);
  private static final AlertApi ALERT_API;
  private static final SubscriptionGroupApi SUBSCRIPTION_GROUP_API;
  private static final TimeProvider CLOCK = TimeProvider.instance();
  private static final long T_PAGEVIEWS_DATASET_START = epoch("2020-02-02 00:00");

  static {
    try {
      ALERT_API = loadAlertApi("/anomalyresolution/payloads/alert-1.json");
      SUBSCRIPTION_GROUP_API = loadApi(
          "/anomalyresolution/payloads/subscription-group.json",
          SubscriptionGroupApi.class);
    } catch (final IOException e) {
      throw new RuntimeException(String.format("Could not load json: %s", e));
    }
  }

  private DropwizardTestSupport<ThirdEyeServerConfiguration> SUPPORT;
  private ThirdEyeTestClient client;

  private long alertId;
  private Long subscriptionGroupId;
  private DataSourceApi pinotDataSourceApi;
  private Injector injector;
  private SubscriptionGroupManager subscriptionGroupManager;
  private NotificationTaskFilter notificationTaskFilter;
  private TestNotificationServiceFactory nsf;

  private static long epoch(final String dateTime) {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    final LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
    return localDateTime.toInstant(UTC).toEpochMilli();
  }

  @SuppressWarnings("unused")
  private static String fromEpoch(final long epochMilli) {
    final Instant instant = Instant.ofEpochMilli(epochMilli);
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(UTC);
    return formatter.format(instant);
  }

  @BeforeClass
  public void beforeClass() throws Exception {
    // ensure time is controlled via the TimeProvider CLOCK - ie weaving is working correctly
    assertThat(CLOCK.isTimeMockWorking()).isTrue();

    final Future<DataSourceApi> pinotDataSourceFuture = PinotDataSourceManager.getPinotDataSourceApi();
    final DatabaseConfiguration dbConfiguration = MySqlTestDatabase.sharedDatabaseConfiguration();

    if(useLocalMysqlInstance()) {
      MySqlTestDatabase.cleanSharedDatabase();
    }

    // Setup plugins dir so ThirdEye can load it
    IntegrationTestUtils.setupPluginsDirAbsolutePath();

    SUPPORT = buildSupport(dbConfiguration, "anomalyresolution/config/server.yaml");
    SUPPORT.before();
    final Client c = buildClient("scheduling-test-client", SUPPORT);
    client = new ThirdEyeTestClient(c, SUPPORT.getLocalPort());
    pinotDataSourceApi = pinotDataSourceFuture.get();

    injector = ((ThirdEyeServer) SUPPORT.getApplication()).getInjector();
    subscriptionGroupManager = injector.getInstance(SubscriptionGroupManager.class);
    notificationTaskFilter = injector.getInstance(NotificationTaskFilter.class);

    nsf = new TestNotificationServiceFactory();
    injector
        .getInstance(NotificationServiceRegistry.class)
        .addNotificationServiceFactory(nsf);
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
    Response response = client.request("internal/ping").get();
    assert200(response);

    // create datasource
    response = client.request("api/data-sources")
        .post(Entity.json(List.of(pinotDataSourceApi)));
    assert200(response);

    // create dataset
    final MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add("dataSourceName", PINOT_DATA_SOURCE_NAME);
    formData.add("datasetName", PINOT_DATASET_NAME);
    response = client.request("api/data-sources/onboard-dataset/")
        .post(Entity.form(formData));
    assert200(response);
  }

  @Test(dependsOnMethods = "setUpData")
  public void testCreateAlertLastTimestamp() {
    // fix clock : time is now controlled manually
    CLOCK.useMockTime(epoch("2020-02-16 15:00"));

    final Response createResponse = client.request("api/alerts")
        .post(Entity.json(List.of(ALERT_API)));
    assertThat(createResponse.getStatus()).isEqualTo(200);
    final List<AlertApi> alerts = createResponse.readEntity(ALERT_LIST_TYPE);
    alertId = alerts.get(0).getId();

    // time advancing should not impact lastTimestamp
    CLOCK.tick(5);

    // check that lastTimestamp just after creation is set to the start of the dataset
    final long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(T_PAGEVIEWS_DATASET_START);
  }

  @Test(dependsOnMethods = "setUpData")
  public void testCreateSubscriptionGroup() {
    // TODO spyne fix. This does modify the constant SUBSCRIPTION_GROUP_API
    SUBSCRIPTION_GROUP_API.setAlertAssociations(
        List.of(new AlertAssociationApi().setAlert(new AlertApi().setId(alertId))));

    try (final Response r = client.request("api/subscription-groups")
        .post(Entity.json(List.of(SUBSCRIPTION_GROUP_API)))) {
      assertThat(r.getStatus()).isEqualTo(200);
      subscriptionGroupId = r.readEntity(SUBSCRIPTION_GROUP_LIST_TYPE).get(0).getId();
    }
  }

  @Test(dependsOnMethods = "testCreateSubscriptionGroup", timeOut = 60000L)
  public void testOnboardingTaskRunAndNotificationRun() throws Exception {
    // wait for anomalies - proxy to know when the onboarding task has run
    while (client.getAnomalies().isEmpty()) {
      // see taskDriver server config for optimization
      Thread.sleep(1000);
    }

    // check that lastTimestamp is the endTime of the Onboarding task: March 21 1H
    final long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(epoch("2020-02-16 00:00"));
  }

  @Test(dependsOnMethods = "testOnboardingTaskRunAndNotificationRun", timeOut = 60000L)
  public void testAfterDetectionCronLastTimestamp() throws InterruptedException {
    // get current number of anomalies
    final int numAnomaliesBeforeDetectionRun = client.getAnomalies().size();

    final List<AnomalyApi> parentAnomalies = client.getAnomalies("?isChild=False");
    assertThat(parentAnomalies).hasSize(1);

    // No notifications sent yet.
    assertThat(nsf.getCount()).isZero();


    // advance detection time to March 22, 2020, 00:00:00 UTC
    // this should trigger the cron - and a new anomaly is expected on [March 21 - March 22]
    final long jumpTime = epoch("2020-02-21 00:00");
    CLOCK.useMockTime(jumpTime);
    // not exact time should not impact lastTimestamp
    CLOCK.tick(5);
    // give thread to detectionCronScheduler and to quartz scheduler - (quartz idle time is weaved to 100 ms for test speed)
    Thread.sleep(1000);

    // wait for the new anomaly to be created - proxy to know when the detection has run
    while (client.getAnomalies().size() == numAnomaliesBeforeDetectionRun) {
      Thread.sleep(1000);
    }

    // check that lastTimestamp after detection is the runTime of the cron
    final long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(jumpTime);

    while (client.getSuccessfulTasks(subscriptionGroupId).isEmpty()) {
      Thread.sleep(1000);
    }

    // There is at least 1 successful subscription group task
    assertThat(nsf.getCount()).isEqualTo(0);
  }

  private long getAlertLastTimestamp() {
    final Response getResponse = client.request("api/alerts/" + alertId).get();
    assertThat(getResponse.getStatus()).isEqualTo(200);
    final AlertApi alert = getResponse.readEntity(AlertApi.class);
    return alert.getLastTimestamp().getTime();
  }
}


