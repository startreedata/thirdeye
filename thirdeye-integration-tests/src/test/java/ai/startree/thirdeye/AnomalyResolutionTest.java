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
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
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
    CLOCK.useMockTime(epoch("2020-02-16 15:00"));

    final Response createResponse = client.request("api/alerts")
        .post(Entity.json(List.of(ALERT_API)));
    assertThat(createResponse.getStatus()).isEqualTo(200);
    final List<AlertApi> alerts = createResponse.readEntity(ALERT_LIST_TYPE);
    alertId = alerts.getFirst().getId();

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
      subscriptionGroupId = r.readEntity(SUBSCRIPTION_GROUP_LIST_TYPE).getFirst().getId();
    }
  }

  @Test(dependsOnMethods = "testCreateSubscriptionGroup", timeOut = TEST_IMEOUT)
  public void testOnboardingTaskRun() throws Exception {
    waitForDetectionRun();

    // check that lastTimestamp is the endTime of the Onboarding task: March 21 1H
    final long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(epoch("2020-02-16 00:00"));
  }

  /**
   * the alert detects anomalies on [feb 3 , feb6), [feb 10, feb 13), [feb 17, feb 20), [feb 24, feb
   * 27), etc...
   * this test checks the behaviour for the anomaly on [feb 17, feb 20)
   * time is currently "2020-02-16 15:00". time is increased day by day from feb 16 to feb 24, and
   * the state is checked every day
   * the max merge gap is P3D, so once there is no anomaly detected on [feb 23, feb 24), the
   * completed anomaly notification should be sent
   */
  @Test(dependsOnMethods = "testOnboardingTaskRun", timeOut = TEST_IMEOUT)
  public void testCompletedAnomalyIsSentCorrectly() throws InterruptedException {
    // sanity checks after the onboarding task 
    assertThat(client.getParentAnomalies()).hasSize(2);
    // no notification happened yet - time has not increased since subscription group creation
    assertThat(nsf.notificationSentCount()).isZero();

    // run the detections every day and check the results
    // the cron is at 5 am, so moving to 6 means the cron at 5 will be triggered
    // no anomaly on [Feb 16, Feb 17)
    CLOCK.useMockTime(epoch("2020-02-17 06:00"));
    waitForDetectionRun();
    // notification cron is every day at 7 am, so moving to 8 will trigger a notification
    CLOCK.useMockTime(epoch("2020-02-17 08:00"));
    waitForNotificationTaskRun();
    assertThat(nsf.notificationSentCount()).isZero();

    // anomaly on [Feb 17, Feb 18)
    CLOCK.useMockTime(epoch("2020-02-18 06:00"));
    assertThat(nsf.notificationSentCount()).isZero();
    waitForDetectionRun();
    CLOCK.useMockTime(epoch("2020-02-18 08:00"));
    waitForNotificationTaskRun();
    assertThat(nsf.notificationSentCount()).isEqualTo(1); // a new anomaly is detected and notified
    final NotificationPayloadApi notificationPayload = nsf.lastNotificationPayload();
    assertThat(notificationPayload.getAnomalyReports()).hasSize(1);
    final AnomalyApi anomalyApi = notificationPayload.getAnomalyReports().getFirst().getAnomaly();
    assertThat(anomalyApi.getStartTime()).isEqualTo(new Date(epoch("2020-02-17 00:00")));
    assertThat(anomalyApi.getEndTime()).isEqualTo(new Date(epoch("2020-02-18 00:00")));

    // anomaly on [Feb 18, Feb 19)
    CLOCK.useMockTime(epoch("2020-02-19 06:00"));
    waitForDetectionRun();
    CLOCK.useMockTime(epoch("2020-02-19 08:00"));
    waitForNotificationTaskRun();
    assertThat(nsf.notificationSentCount()).isEqualTo(
        1); // this point is anomalous but merged in current anomaly

    // anomaly on [Feb 19, Feb 20)
    CLOCK.useMockTime(epoch("2020-02-20 06:00"));
    waitForDetectionRun();
    CLOCK.useMockTime(epoch("2020-02-20 08:00"));
    waitForNotificationTaskRun();
    assertThat(nsf.notificationSentCount()).isEqualTo(
        1); // this point is anomalous but merged in current anomaly

    // sanity checks on the detection state
    // check that lastTimestamp after detection is the expected runTime of the cron, floored to alert granularity 2020-02-20 06:00 --> 2020-02-20 00:00
    assertThat(getAlertLastTimestamp()).isEqualTo(epoch("2020-02-20 00:00"));
    assertThat(client.getParentAnomalies()).hasSize(3);
    final int anomaliesCurrentCount = client.getAnomalies().size();

    // no anomaly on [Feb 20, Feb 21)
    CLOCK.useMockTime(epoch("2020-02-21 06:00"));
    waitForDetectionRun();
    // ensure the number of anomalies hasn't changed 
    assertThat(client.getAnomalies()).hasSize(anomaliesCurrentCount);
    CLOCK.useMockTime(epoch("2020-02-21 08:00"));
    waitForNotificationTaskRun();
    // ensure the number of notification hasn't changed
    assertThat(nsf.notificationSentCount()).isEqualTo(1);

    // no anomaly on [Feb 21, Feb 22) - same checks as above
    CLOCK.useMockTime(epoch("2020-02-22 06:00"));
    waitForDetectionRun();
    assertThat(client.getAnomalies()).hasSize(anomaliesCurrentCount);
    CLOCK.useMockTime(epoch("2020-02-22 08:00"));
    waitForNotificationTaskRun();
    assertThat(nsf.notificationSentCount()).isEqualTo(1);

    // no anomaly on [Feb 22, Feb 23) - same checks as above
    CLOCK.useMockTime(epoch("2020-02-23 06:00"));
    waitForDetectionRun();
    assertThat(client.getAnomalies()).hasSize(anomaliesCurrentCount);
    CLOCK.useMockTime(epoch("2020-02-23 08:00"));
    waitForNotificationTaskRun();
    assertThat(nsf.notificationSentCount()).isEqualTo(1);

    // no anomaly on [Feb 23, Feb 24)
    CLOCK.useMockTime(epoch("2020-02-24 06:00"));
    waitForDetectionRun();
    assertThat(client.getAnomalies()).hasSize(anomaliesCurrentCount);
    CLOCK.useMockTime(epoch("2020-02-24 08:00"));
    waitForNotificationTaskRun();
    // maxMergeGap is P3D, so a notification for completed anomaly can be sent now
    assertThat(nsf.notificationSentCount()).isEqualTo(2);
    // sanity check on number of anomalies
    assertThat(client.getParentAnomalies()).hasSize(3);
    // ensure the completed anomaly notification was sent
    final NotificationPayloadApi payload = nsf.lastNotificationPayload();
    assertThat(payload.getAnomalyReports()).hasSize(0);
    assertThat(payload.getCompletedAnomalyReports()).hasSize(1);
  }

  private void waitForDetectionRun() throws InterruptedException {
    nDetectionTaskRuns = waitFor(alertId, nDetectionTaskRuns);
  }

  private void waitForNotificationTaskRun() throws InterruptedException {
    nNotificationTaskRuns = waitFor(subscriptionGroupId, nNotificationTaskRuns);
  }

  private int waitFor(final Long refId, int currentCount) throws InterruptedException {
    int nTasks;
    do {
      nTasks = client.getSuccessfulTasks(refId).size();
      if (nTasks > currentCount) {
        break;
      }
      // should give time to run tasks
      Thread.sleep(500);
    } while (true);

    return nTasks;
  }

  private long getAlertLastTimestamp() {
    return client.getAlert(alertId).getLastTimestamp().getTime();
  }
}


