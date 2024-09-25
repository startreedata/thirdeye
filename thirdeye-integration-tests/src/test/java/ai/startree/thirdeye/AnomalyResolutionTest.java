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
import static ai.startree.thirdeye.DropwizardTestUtils.loadApi;
import static ai.startree.thirdeye.HappyPathTest.assert200;
import static ai.startree.thirdeye.PinotDataSourceManager.PINOT_DATASET_NAME;
import static ai.startree.thirdeye.ThirdEyeTestClient.ALERT_LIST_TYPE;
import static ai.startree.thirdeye.ThirdEyeTestClient.DATASOURCE_LIST_TYPE;
import static ai.startree.thirdeye.ThirdEyeTestClient.SUBSCRIPTION_GROUP_LIST_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.aspect.TimeProvider;
import ai.startree.thirdeye.notification.AlertUtils;
import ai.startree.thirdeye.notification.NotificationServiceRegistry;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertAssociationApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import com.google.inject.Injector;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.joda.time.Period;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AnomalyResolutionTest {

  private static final ZoneOffset UTC = ZoneOffset.UTC;
  private static final long TEST_IMEOUT = 60_000L;
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

  private final ThirdEyeIntegrationTestSupport support = new ThirdEyeIntegrationTestSupport(
      "anomalyresolution/config/server.yaml"
  );
  private int nDetectionTaskRuns = 0;
  private int nNotificationTaskRuns = 0;
  private ThirdEyeTestClient client;
  private long alertId;
  private Long subscriptionGroupId;
  private DataSourceApi pinotDataSourceApi;
  private TestNotificationServiceFactory nsf;
  private AlertTemplateRenderer alertTemplateRenderer;
  private AlertManager alertManager;

  private static long epoch(final String dateTime) {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    final LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
    return localDateTime.toInstant(UTC).toEpochMilli();
  }

  private static long jumpToTime(final String dateTime) throws InterruptedException {
    final long jumpTime = epoch(dateTime);
    CLOCK.useMockTime(jumpTime);
    CLOCK.tick(5); // simulate move time forward

    // give thread to detectionCronScheduler and to quartz scheduler -
    // (quartz idle time is weaved to 100 ms for test speed)
    Thread.sleep(1000);
    return jumpTime;
  }

  @BeforeClass
  public void beforeClass() throws Exception {
    // ensure time is controlled via the TimeProvider CLOCK - ie weaving is working correctly
    assertThat(CLOCK.isTimeMockWorking()).isTrue();

    // Initialize the test support
    support.setup();

    client = support.getClient();
    Injector injector = support.getInjector();
    pinotDataSourceApi = support.getPinotDataSourceApi();

    alertTemplateRenderer = injector.getInstance(AlertTemplateRenderer.class);
    alertManager = injector.getInstance(AlertManager.class);

    nsf = new TestNotificationServiceFactory();
    injector
        .getInstance(NotificationServiceRegistry.class)
        .addNotificationServiceFactory(nsf);
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
    final DataSourceApi dataSourceInResponse = response.readEntity(DATASOURCE_LIST_TYPE).get(0);
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
    CLOCK.useMockTime(epoch("2020-02-16 15:00"));

    final Response createResponse = client.request("api/alerts")
        .post(Entity.json(List.of(ALERT_API)));
    assertThat(createResponse.getStatus()).isEqualTo(200);
    final List<AlertApi> alerts = createResponse.readEntity(ALERT_LIST_TYPE);
    alertId = alerts.get(0).getId();

    final AlertDTO alert = alertManager.findById(alertId);
    final AlertTemplateDTO renderedTemplate = alertTemplateRenderer.renderAlert(alert);
    final Period mergeMaxGap = AlertUtils.getMergeMaxGap(renderedTemplate);
    assertThat(mergeMaxGap).isEqualTo(Period.days(3));

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

  @Test(dependsOnMethods = "testCreateSubscriptionGroup", timeOut = TEST_IMEOUT)
  public void testOnboardingTaskRun() throws Exception {
    waitForDetectionRun();

    // check that lastTimestamp is the endTime of the Onboarding task: March 21 1H
    final long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(epoch("2020-02-16 00:00"));
  }

  @Test(dependsOnMethods = "testOnboardingTaskRun", timeOut = TEST_IMEOUT)
  public void testDailyFeb21() throws InterruptedException {
    // get current number of anomalies
    final int numAnomaliesBeforeDetectionRun = client.getAnomalies().size();

    final List<AnomalyApi> parentAnomalies = client.getParentAnomalies();
    assertThat(parentAnomalies).hasSize(1);

    // No notifications sent yet.
    assertThat(nsf.getCount()).isZero();
    long jumpTime = jumpToTime("2020-02-21 00:00");

    waitForDetectionRun();
    // wait for new anomalies to be created
    while (client.getAnomalies().size() == numAnomaliesBeforeDetectionRun) {
      Thread.sleep(1000);
    }

    // check that lastTimestamp after detection is the runTime of the cron
    assertThat(getAlertLastTimestamp()).isEqualTo(jumpTime);

    assertThat(client.getParentAnomalies()).hasSize(2);

    // There is at least 1 successful subscription group task
    waitForNotificationTaskRun();
    assertThat(nsf.getCount()).isEqualTo(0);

    // Move time forward by 5 minutes to make subscription group task run again
    jumpToTime("2020-02-21 00:05");

    waitForNotificationTaskRun();
    assertThat(nsf.getCount()).isEqualTo(1);

    final NotificationPayloadApi notificationPayload = nsf.getNotificationPayload();
    assertThat(notificationPayload.getAnomalyReports()).hasSize(1);

    final AnomalyApi anomalyApi = notificationPayload.getAnomalyReports().get(0).getAnomaly();
    assertThat(anomalyApi.getStartTime()).isEqualTo(new Date(epoch("2020-02-17 00:00")));
    assertThat(anomalyApi.getEndTime()).isEqualTo(new Date(epoch("2020-02-21 00:00")));
  }

  @Test(dependsOnMethods = "testDailyFeb21", timeOut = TEST_IMEOUT)
  public void testDailyFeb22() throws InterruptedException {
    jumpToTimeAndWait("2020-02-22 00:06");
    assertThat(client.getParentAnomalies()).hasSize(2);
    assertThat(nsf.getCount()).isEqualTo(1); // no new notifications
  }

  @Test(dependsOnMethods = "testDailyFeb22", timeOut = TEST_IMEOUT)
  public void testDailyFeb23() throws InterruptedException {
    jumpToTimeAndWait("2020-02-23 00:06");
    assertThat(client.getParentAnomalies()).hasSize(2);
    assertThat(nsf.getCount()).isEqualTo(1); // no new notifications
  }

  @Test(dependsOnMethods = "testDailyFeb23", timeOut = TEST_IMEOUT)
  public void testDailyFeb24() throws InterruptedException {
    jumpToTimeAndWait("2020-02-24 00:06");
    assertThat(client.getParentAnomalies()).hasSize(2);
    assertThat(nsf.getCount()).isEqualTo(1); // no new notifications
  }

  @Test(dependsOnMethods = "testDailyFeb24", timeOut = TEST_IMEOUT)
  public void testDailyFeb25() throws InterruptedException {
    jumpToTimeAndWait("2020-02-25 00:06");

    final List<AnomalyApi> parentAnomalies = client.getParentAnomalies();
    assertThat(parentAnomalies).hasSize(2);

    final AnomalyApi ongoingAnomaly = parentAnomalies.stream()
        .filter(a -> a.getStartTime().equals(new Date(epoch("2020-02-17 00:00"))))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Anomaly not found"));

    // anomalies are merged here
    assertThat(ongoingAnomaly.getEndTime()).isEqualTo(new Date(epoch("2020-02-25 00:00")));

    // No new notifications yet
    assertThat(nsf.getCount()).isEqualTo(1);
  }

  @Test(dependsOnMethods = "testDailyFeb25", timeOut = TEST_IMEOUT)
  public void testDailyMar3() throws InterruptedException {
    jumpToTimeAndWait("2020-03-03 00:06");
    assertThat(client.getParentAnomalies()).hasSize(3);
    assertThat(nsf.getCount()).isEqualTo(1);
  }

  @Test(dependsOnMethods = "testDailyMar3", timeOut = TEST_IMEOUT)
  public void testDailyMar4() throws InterruptedException {
    jumpToTimeAndWait("2020-03-04 00:06");

    final List<AnomalyApi> parentAnomalies = client.getParentAnomalies();
    assertThat(parentAnomalies).hasSize(3);

    // No new notifications yet
    assertThat(nsf.getCount()).isEqualTo(2);

    final NotificationPayloadApi notificationPayload = nsf.getNotificationPayload();
    assertThat(notificationPayload.getAnomalyReports()).hasSize(1);

    final AnomalyApi anomalyApi = notificationPayload.getAnomalyReports().get(0).getAnomaly();
    assertThat(anomalyApi.getStartTime()).isEqualTo(new Date(epoch("2020-03-02 00:00")));
    assertThat(anomalyApi.getEndTime()).isEqualTo(new Date(epoch("2020-03-03 00:00")));
  }

  @Test(dependsOnMethods = "testDailyMar4", timeOut = TEST_IMEOUT)
  public void testDailyMar5() throws InterruptedException {
    jumpToTimeAndWait("2020-03-05 00:06");
    assertThat(client.getParentAnomalies()).hasSize(3);
    assertThat(nsf.getCount()).isEqualTo(3);

    final NotificationPayloadApi payload = nsf.getNotificationPayload();
    assertThat(payload.getAnomalyReports()).hasSize(0);
    assertThat(payload.getCompletedAnomalyReports()).hasSize(1);
  }

  private void jumpToTimeAndWait(final String dateTime) throws InterruptedException {
    jumpToTime(dateTime); // allow both detection and notification to run
    waitForDetectionRun();
    waitForNotificationTaskRun();
  }

  private void waitForDetectionRun() throws InterruptedException {
    nDetectionTaskRuns = waitFor(alertId, nDetectionTaskRuns);

    // Even after the task is complete, anomalies are persisted async. Giving another sec
    Thread.sleep(1000);
  }

  private void waitForNotificationTaskRun() throws InterruptedException {
    nNotificationTaskRuns = waitFor(subscriptionGroupId, nNotificationTaskRuns);
  }

  private int waitFor(final Long refId, int currentCount) throws InterruptedException {
    int nTasks;
    do {
      nTasks = client.getSuccessfulTasks(refId).size();
      if (!(nTasks <= currentCount)) {
        break;
      }
      // should trigger another task after time jump
      Thread.sleep(1000);
    } while (true);

    return nTasks;
  }

  private long getAlertLastTimestamp() {
    return client.getAlert(alertId).getLastTimestamp().getTime();
  }
}


