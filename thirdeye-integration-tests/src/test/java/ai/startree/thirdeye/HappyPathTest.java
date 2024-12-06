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

import static ai.startree.thirdeye.DropwizardTestUtils.alertEvaluationApi;
import static ai.startree.thirdeye.DropwizardTestUtils.buildClient;
import static ai.startree.thirdeye.DropwizardTestUtils.buildSupport;
import static ai.startree.thirdeye.DropwizardTestUtils.loadAlertApi;
import static ai.startree.thirdeye.IntegrationTestUtils.NODE_NAME_CHILD_ROOT;
import static ai.startree.thirdeye.IntegrationTestUtils.NODE_NAME_ROOT;
import static ai.startree.thirdeye.IntegrationTestUtils.assertAnomalyAreTheSame;
import static ai.startree.thirdeye.IntegrationTestUtils.assertNamespaceConfigurationAreSame;
import static ai.startree.thirdeye.IntegrationTestUtils.combinerNode;
import static ai.startree.thirdeye.IntegrationTestUtils.enumeratorNode;
import static ai.startree.thirdeye.IntegrationTestUtils.forkJoinNode;
import static ai.startree.thirdeye.PinotDataSourceManager.PINOT_DATASET_NAME;
import static ai.startree.thirdeye.PinotDataSourceManager.PINOT_DATA_SOURCE_NAME;
import static ai.startree.thirdeye.ThirdEyeTestClient.ALERT_LIST_TYPE;
import static ai.startree.thirdeye.ThirdEyeTestClient.ALERT_TEMPLATE_LIST_TYPE;
import static ai.startree.thirdeye.ThirdEyeTestClient.ANOMALIES_LIST_TYPE;
import static ai.startree.thirdeye.ThirdEyeTestClient.DATASOURCE_LIST_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import ai.startree.thirdeye.plugins.postprocessor.AnomalyMergerPostProcessor;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.AlertInsightsApi;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.api.AnomalyLabelApi;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.api.CountApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DetectionEvaluationApi;
import ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi;
import ai.startree.thirdeye.spi.api.EmailSchemeApi;
import ai.startree.thirdeye.spi.api.HeatMapResponseApi;
import ai.startree.thirdeye.spi.api.NamespaceConfigurationApi;
import ai.startree.thirdeye.spi.api.NotificationSchemesApi;
import ai.startree.thirdeye.spi.api.PlanNodeApi;
import ai.startree.thirdeye.spi.api.RcaInvestigationApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import ai.startree.thirdeye.spi.api.TimeConfigurationApi;
import ai.startree.thirdeye.spi.detection.AnomalyCause;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.DropwizardTestSupport;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.pinot.testcontainer.PinotContainer.PinotVersion;
import org.joda.time.DateTimeZone;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITest;
import org.testng.TestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * Smoke tests with **Pinot** Datasource and **MySQL** persistence.
 * Test the following flow (the happy path):
 * - create a Pinot datasource
 * - create a dataset
 * - evaluate an alert
 * - create an alert
 * - create a subscription
 * - get anomalies
 * - update the alert
 * - get the anomalies again
 * - get a single anomaly
 * - create a feedback for the anomaly
 * - update the feedback for the anomaly
 * - get the count of anomalies for the alert
 * - get the anomaly breakdown (heatmap)
 * - test authorization
 */
public class HappyPathTest implements ITest {

  private ThreadLocal<String> testNameInternal = new ThreadLocal<>();

  @Override
  public String getTestName() {
    return testNameInternal.get();
  }

  @BeforeMethod
  public void beforeMethod(Method method) {
    testNameInternal.set(method.getName() + " (Pinot " + pinotVersion.getTag() + ")");
  }

  @Factory
  public static Object[] createInstances() {
    // TODO CYRIL - running tests for all pinot versions requires some docker/testcontainers/testng tuning
    //return Arrays.stream(PinotVersion.values()).map(HappyPathTest::new).toArray();
    // picking a specific version 
    //return Stream.of(PinotVersion.v1_2_0).map(HappyPathTest::new).toArray();

    return Stream.of(PinotVersion.recommendedVersion()).map(HappyPathTest::new).toArray();
  }

  private final PinotVersion pinotVersion;

  public HappyPathTest(final PinotVersion pinotVersion) {
    this.pinotVersion = pinotVersion;
  }

  public static final String THRESHOLD_TEMPLATE_NAME = "startree-threshold";
  private static final Logger log = LoggerFactory.getLogger(HappyPathTest.class);

  private static final AlertApi CREATE_ALERT_API;
  private static final AlertApi UPDATE_ALERT_API;
  private static final long EVALUATE_END_TIME = 1596326400000L;
  private static final long PAGEVIEWS_DATASET_START_TIME_PLUS_ONE_DAY = 1580688000000L;
  private static final long PAGEVIEWS_DATASET_END_TIME_PLUS_ONE_DAY = 1596153600000L;
  private static final long PAGEVIEWS_DATASET_END_TIME = 1596067200000L;
  private static final long PAGEVIEWS_DATASET_START_TIME = 1580601600000L;

  static {
    try {
      CREATE_ALERT_API = loadAlertApi("/happypath/payloads/" + "alert_create.json");
      UPDATE_ALERT_API = loadAlertApi("/happypath/payloads/" + "alert_update.json");
    } catch (final IOException e) {
      throw new RuntimeException(String.format("Could not load alert json: %s", e));
    }
  }

  private DataSourceApi pinotDataSourceApi;
  private DropwizardTestSupport<ThirdEyeServerConfiguration> SUPPORT;
  private Client client;

  // this attribute is shared between tests
  private long anomalyId;
  private long alertLastUpdateTime;
  private long alertId;
  private long namespaceConfigurationId;

  @BeforeClass
  public void beforeClass() throws Exception {
    final Future<DataSourceApi> pinotDataSourceFuture = PinotDataSourceManager.getPinotDataSourceApi(pinotVersion);
    final DatabaseConfiguration dbConfiguration = MySqlTestDatabase.newDatabaseConfiguration();

    // Setup plugins dir so ThirdEye can load it
    IntegrationTestUtils.setupPluginsDirAbsolutePath();

    SUPPORT = buildSupport(dbConfiguration, "happypath/config/server.yaml");
    SUPPORT.before();
    client = buildClient("happy-path-test-client", SUPPORT);
    pinotDataSourceApi = pinotDataSourceFuture.get();
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    log.info("Stopping Thirdeye at port: {}", SUPPORT.getLocalPort());
    SUPPORT.after();
    MySqlTestDatabase.cleanSharedDatabase();
  }

  @Test()
  public void testPing() {
    final Response response = request("internal/ping").get();
    assert200(response);
  }

  @Test(dependsOnMethods = "testPing")
  public void testCreatePinotDataSource() {

    final Response response = request("api/data-sources").post(Entity.json(List.of(
        pinotDataSourceApi)));
    assert200(response);
    final DataSourceApi dataSourceInResponse = response.readEntity(DATASOURCE_LIST_TYPE).getFirst();
    pinotDataSourceApi.setId(dataSourceInResponse.getId());
    
  }

  @Test(dependsOnMethods = "testCreatePinotDataSource", timeOut = 5000)
  public void testPinotDataSourceHealth() {
    final Response response = request(
        "api/data-sources/validate?id=" + pinotDataSourceApi.getId()).get();
    assert200(response);
  }

  @Test(dependsOnMethods = "testPing")
  public void testSwaggerApiJson() throws JsonProcessingException {
    final Response response = request("openapi.json").get();
    assert200(response);
    final JsonNode r = new ObjectMapper().readTree(
        response.readEntity(JSONObject.class).toJSONString());
    assertThat(r.get("openapi").textValue()).isEqualTo("3.0.1");
    final JsonNode oauthSecurityConfig = r.get("components").get("securitySchemes").get("oauth");
    assertThat(oauthSecurityConfig.get("in").textValue()).isEqualTo("header");
    assertThat(oauthSecurityConfig.get("name").textValue()).isEqualTo("Authorization");
    // test a POST formData path
    final JsonNode alertRunPath = r.get("paths").get("/api/alerts/{id}/run");
    assertThat(alertRunPath.get("post")
        .get("requestBody")
        .get("content")
        .get("application/x-www-form-urlencoded")
        .get("schema")
        .get("properties")
        .get("start")
        .get("type")
        .textValue()).isEqualTo("integer");
    // test a POST json data path
    final JsonNode evaluatePath = r.get("paths").get("/api/alerts/evaluate");
    assertThat(evaluatePath.get("post")
        .get("requestBody")
        .get("content")
        .get("application/json")
        .get("schema")
        .get("$ref")
        .textValue()).isEqualTo("#/components/schemas/AlertEvaluationApi");
  }

  @Test(dependsOnMethods = "testPing")
  public void testCreateDxTemplate() {
    final Response response = request("api/alert-templates").get();
    assert200(response);
    final List<AlertTemplateApi> templates = response.readEntity(ALERT_TEMPLATE_LIST_TYPE);
    final AlertTemplateApi thresholdTemplate = templates.stream().filter(t -> t.getName().equals(THRESHOLD_TEMPLATE_NAME)).findFirst()
        .orElseThrow(() -> new TestException("Failed to fetch template " + THRESHOLD_TEMPLATE_NAME));
    final List<PlanNodeApi> nodes = thresholdTemplate.getNodes();
    final List<PlanNodeApi> childRootNodes = nodes
        .stream()
        .filter(node -> NODE_NAME_ROOT.equals(node.getName()))
        .collect(Collectors.toList());
    assertThat(childRootNodes).hasSize(1);

    childRootNodes.iterator().next().setName(NODE_NAME_CHILD_ROOT);
    nodes.add(enumeratorNode());
    nodes.add(forkJoinNode());
    nodes.add(combinerNode());

    thresholdTemplate
        .setName(thresholdTemplate.getName() + "-dx")
        .setId(null);

    final Response updateResponse = request("api/alert-templates")
        .post(Entity.json(List.of(thresholdTemplate)));
    assertThat(updateResponse.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testPinotDataSourceHealth")
  public void testCreateDataset() {
    final MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add("dataSourceId", String.valueOf(pinotDataSourceApi.getId()));
    formData.add("datasetName", PINOT_DATASET_NAME);

    final Response response = request("api/data-sources/onboard-dataset/").post(
        Entity.form(formData));
    assert200(response);
  }

  @Test(dependsOnMethods = "testCreateDataset", timeOut = 12000)
  public void testEvaluateAlert() {
    final AlertEvaluationApi alertEvaluationApi = alertEvaluationApi(UPDATE_ALERT_API,
        PAGEVIEWS_DATASET_START_TIME, EVALUATE_END_TIME);

    final Response response = request("api/alerts/evaluate").post(Entity.json(alertEvaluationApi));
    assert200(response);
  }

  @Test(dependsOnMethods = "testEvaluateAlert")
  public void testCreateAlert() {
    final Response response = request("api/alerts").post(Entity.json(List.of(CREATE_ALERT_API)));

    assert200(response);
    final List<AlertApi> alerts = response.readEntity(ALERT_LIST_TYPE);
    alertId = alerts.getFirst().getId();
    UPDATE_ALERT_API.setId(alertId);
  }

  @DataProvider(name = "happyPathAlerts")
  public Object[][] happyPathAlerts() {
    // one alert for each template
    return new Object[][]{
        {"startree-absolute-rule-alert.json"},
        {"startree-absolute-rule-percentile-alert.json"},
        {"startree-mean-variance-alert.json"},
        {"startree-mean-variance-percentile-alert.json"},
        {"startree-percentage-rule-alert.json"},
        {"startree-percentage-rule-percentile-alert.json"},
        {"startree-threshold-alert.json"},
        {"startree-threshold-percentile-alert.json"}
    };
  }

  @Test(dependsOnMethods = "testEvaluateAlert", timeOut = 10000L, dataProvider = "happyPathAlerts")
  public void testEvaluateAllHappyPathAlerts(final String alertJson) throws IOException {
    final AlertApi alertApi = loadAlertApi("/happypath/payloads/" + alertJson);
    final AlertEvaluationApi alertEvaluationApi = alertEvaluationApi(alertApi,
        PAGEVIEWS_DATASET_START_TIME, EVALUATE_END_TIME);

    final Response response = request("api/alerts/evaluate").post(Entity.json(alertEvaluationApi));
    assert200(response);
  }

  @Test(dependsOnMethods = "testEvaluateAlert", timeOut = 10000L, dataProvider = "happyPathAlerts")
  public void testEvaluateAllHappyPathAlertsOnEmptyTimeframe(final String alertJson) throws IOException {
    // a detection pipeline should always work on an empty timeframe, and return an empty list
    final AlertApi alertApi = loadAlertApi("/happypath/payloads/" + alertJson);
    final AlertEvaluationApi alertEvaluationApi = alertEvaluationApi(alertApi,
        PAGEVIEWS_DATASET_START_TIME, PAGEVIEWS_DATASET_START_TIME);

    final Response response = request("api/alerts/evaluate").post(Entity.json(alertEvaluationApi));
    assert200(response);
    final AlertEvaluationApi alertEvaluation = response.readEntity(AlertEvaluationApi.class);
    assertThat(alertEvaluation.getDetectionEvaluations()).isNotEmpty();
    final DetectionEvaluationApi detectionEvaluation = alertEvaluation.getDetectionEvaluations()
        .values()
        .iterator()
        .next();
    assertThat(detectionEvaluation.getData().getTimestamp()).isEmpty();
    assertThat(detectionEvaluation.getData().getCurrent()).isEmpty();
    assertThat(detectionEvaluation.getData().getExpected()).isEmpty();
    assertThat(detectionEvaluation.getData().getLowerBound()).isEmpty();
    assertThat(detectionEvaluation.getData().getUpperBound()).isEmpty();
  }

  @Test(dependsOnMethods = "testCreateAlert")
  public void testAlertInsights() {
    final Response response = request("api/alerts/" + alertId + "/insights").get();
    assert200(response);
    final AlertInsightsApi insights = response.readEntity(AlertInsightsApi.class);
    assertThat(insights.getTemplateWithProperties().getMetadata().getGranularity()).isEqualTo(
        "P1D");
    assertThat(insights.getDefaultStartTime()).isEqualTo(PAGEVIEWS_DATASET_START_TIME_PLUS_ONE_DAY);
    assertThat(insights.getDefaultEndTime()).isEqualTo(PAGEVIEWS_DATASET_END_TIME_PLUS_ONE_DAY);
    assertThat(insights.getDatasetStartTime()).isEqualTo(PAGEVIEWS_DATASET_START_TIME);
    assertThat(insights.getDatasetEndTime()).isEqualTo(PAGEVIEWS_DATASET_END_TIME);
  }

  @Test(dependsOnMethods = "testCreateAlert")
  public void testCreateSubscription() {
    final SubscriptionGroupApi subscriptionGroupApi = new SubscriptionGroupApi().setName(
            "testSubscription")
        .setCron("0 0/5 0 ? * * *")
        .setNotificationSchemes(new NotificationSchemesApi().setEmail(
            new EmailSchemeApi().setTo(List.of("analyst@fake.mail"))))
        .setAlerts(List.of(new AlertApi().setId(alertId)));
    final Response response = request("api/subscription-groups").post(
        Entity.json(List.of(subscriptionGroupApi)));
    assert200(response);
  }

  @Test(dependsOnMethods = "testCreateAlert", timeOut = 50000L)
  public void testGetAnomaliesCreate() throws InterruptedException {
    // test get anomalies
    // need to wait for the taskRunner to run the onboard task - can take some time
    List<AnomalyApi> anomalies = List.of();
    while (anomalies.size() == 0) {
      // see taskDriver server config for optimization
      Thread.sleep(1000);
      final Response response = request("api/anomalies?isChild=false").get();
      assert200(response);
      anomalies = response.readEntity(ANOMALIES_LIST_TYPE);
    }

    assertThat(anomalies).hasSize(8);
    // the fifth anomaly is the March 21 - March 23 anomaly
    assertThat(anomalies.get(3).getStartTime().getTime()).isEqualTo(1584748800000L);
    assertThat(anomalies.get(3).getEndTime().getTime()).isEqualTo(1584921600000L);
    anomalyId = anomalies.get(3).getId();
  }

  @Test(dependsOnMethods = "testGetAnomaliesCreate", timeOut = 50000L)
  public void testUpdateAlert() throws InterruptedException {
    final Response response = request("api/alerts").put(Entity.json(List.of(UPDATE_ALERT_API)));
    assert200(response);
    final List<AlertApi> alerts = response.readEntity(ALERT_LIST_TYPE);
    assertThat(alerts.getFirst().getId()).isEqualTo(alertId);
    alertLastUpdateTime = alerts.getFirst().getUpdated().getTime();
  }

  @Test(dependsOnMethods = "testUpdateAlert", timeOut = 50000L)
  public void testGetAnomaliesAfterUpdate() throws InterruptedException {
    // test get anomalies after the update
    // need to wait for the taskRunner to run the soft-reset - can take some time
    long newAlertLastUpdateTime = alertLastUpdateTime;
    while (newAlertLastUpdateTime == alertLastUpdateTime) {
      // see taskDriver server config for optimization
      Thread.sleep(1000);
      newAlertLastUpdateTime = getAlertLastUpdatedTime();
    }
    final Response response = request("api/anomalies?isChild=false").get();
    final List<AnomalyApi> anomalies = response.readEntity(ANOMALIES_LIST_TYPE);
    final List<AnomalyApi> outdatedAnomalies = new ArrayList<>();
    final List<AnomalyApi> remainingAnomalies = new ArrayList<>();
    outer: for (final var a : anomalies) {
      if (a.getAnomalyLabels() != null) {
        for (final AnomalyLabelApi l : a.getAnomalyLabels()) {
          if (AnomalyMergerPostProcessor.OUTDATED_AFTER_REPLAY_LABEL_NAME.equals(l.getName())) {
            outdatedAnomalies.add(a);
            continue outer;
          }
        }
      }
      remainingAnomalies.add(a);
    }
    // the update alert is less sensitive 2 anomalies are now outdated
    assertThat(outdatedAnomalies).hasSize(2);
    assertThat(remainingAnomalies).hasSize(6);
    // the third anomaly is the March 21 - March 23 anomaly
    assertThat(remainingAnomalies.get(1).getStartTime().getTime()).isEqualTo(1584748800000L);
    assertThat(remainingAnomalies.get(1).getEndTime().getTime()).isEqualTo(1584921600000L);
    // the id should not be changed by a soft-reset
    assertThat(remainingAnomalies.get(1).getId()).isEqualTo(anomalyId);
  }

  @Test(dependsOnMethods = "testGetAnomaliesAfterUpdate")
  public void testGetSingleAnomaly() {
    // test get a single anomaly
    final Response response = request("api/anomalies/" + anomalyId).get();
    assert200(response);
  }

  @Test(dependsOnMethods = "testGetAnomaliesAfterUpdate")
  public void testEvaluateWithAlertIdOnly() {
    // corresponds to the evaluate call performed from an anomaly page - only the alertId is passed
    final AlertEvaluationApi alertEvaluationApi = alertEvaluationApi(new AlertApi().setId(alertId),
        PAGEVIEWS_DATASET_START_TIME, EVALUATE_END_TIME);

    final Response response = request("api/alerts/evaluate").post(Entity.json(alertEvaluationApi));
    assert200(response);
  }

  @Test(dependsOnMethods = "testGetAnomaliesAfterUpdate")
  public void testCreateAnomalyFeedback() {
    final Response responseBeforeFeedback = request("api/anomalies/" + anomalyId).get();
    assertThat(responseBeforeFeedback.getStatus()).isEqualTo(200);
    final AnomalyApi anomalyApiBefore = responseBeforeFeedback.readEntity(AnomalyApi.class);
    assertThat(anomalyApiBefore.getFeedback()).isNull();

    final AnomalyFeedbackApi feedback = new AnomalyFeedbackApi()
        .setType(AnomalyFeedbackType.ANOMALY)
        .setComment("Valid anomaly");
    final Response response = request(String.format("api/anomalies/%d/feedback", anomalyId))
        .post(Entity.json(feedback));
    assert200(response);

    // test get a single anomaly
    final Response responseAfterFeedback = request("api/anomalies/" + anomalyId).get();
    final AnomalyApi anomalyApi = responseAfterFeedback.readEntity(AnomalyApi.class);

    assertThat(responseAfterFeedback.getStatus()).isEqualTo(200);
    final AnomalyFeedbackApi actual = anomalyApi.getFeedback();
    assertThat(actual).isNotNull();
    assertThat(actual.getId()).isNotNull();
    assertThat(actual.getType()).isEqualTo(feedback.getType());
    assertThat(actual.getComment()).isEqualTo(feedback.getComment());
    assertThat(actual.getCreated()).isNotNull();
    assertThat(actual.getUpdated()).isNotNull();
    assertThat(actual.getOwner().getPrincipal()).isEqualTo("no-auth-user");
    assertThat(actual.getUpdatedBy().getPrincipal()).isEqualTo("no-auth-user");
  }

  @Test(dependsOnMethods = "testCreateAnomalyFeedback")
  public void testUpdateAnomalyFeedback() {
    final Response responseBeforeFeedback = request("api/anomalies/" + anomalyId).get();
    assertThat(responseBeforeFeedback.getStatus()).isEqualTo(200);
    final AnomalyApi anomalyApiBefore = responseBeforeFeedback.readEntity(AnomalyApi.class);
    final AnomalyFeedbackApi feedbackBefore = anomalyApiBefore.getFeedback();
    assertThat(feedbackBefore).isNotNull();

    final AnomalyFeedbackApi feedback = new AnomalyFeedbackApi()
        .setId(feedbackBefore.getId())
        .setType(AnomalyFeedbackType.NOT_ANOMALY)
        .setCause(AnomalyCause.PLATFORM_UPGRADE);
    final Response response = request(String.format("api/anomalies/%d/feedback", anomalyId))
        .post(Entity.json(feedback));
    assert200(response);

    // test get a single anomaly
    final Response responseAfterFeedback = request("api/anomalies/" + anomalyId).get();
    final AnomalyApi anomalyApi = responseAfterFeedback.readEntity(AnomalyApi.class);

    assertThat(responseAfterFeedback.getStatus()).isEqualTo(200);
    final AnomalyFeedbackApi actual = anomalyApi.getFeedback();
    assertThat(actual).isNotNull();
    assertThat(actual.getId()).isEqualTo(feedbackBefore.getId());
    assertThat(actual.getType()).isEqualTo(feedback.getType());
    assertThat(actual.getComment()).isEmpty();
    assertThat(actual.getCause()).isEqualTo(feedback.getCause());
    assertThat(actual.getUpdated()).isAfter(feedbackBefore.getUpdated());
  }

  @Test(dependsOnMethods = "testGetSingleAnomaly")
  public void testAnomalyCount() {
    // +2 below is the number of outdated anomalies
    // without filters
    Response response = request("api/anomalies/count").get();
    assert200(response);
    Long anomalyCount = response.readEntity(CountApi.class).getCount();
    assertThat(anomalyCount).isEqualTo(22 + 2);

    // there are only 6 parent anomalies
    response = request("api/anomalies/count?isChild=false").get();
    assert200(response);
    anomalyCount = response.readEntity(CountApi.class).getCount();
    assertThat(anomalyCount).isEqualTo(6 + 2);

    // there are only 2 anomalies that have startTime greater than or equal this value
    long startTime = 1585353600000L;

    // with filters
    response = request("api/anomalies/count?isChild=false&startTime=[gte]" + startTime).get();
    assert200(response);
    anomalyCount = response.readEntity(CountApi.class).getCount();
    assertThat(anomalyCount).isEqualTo(3);
  }

  @Test(dependsOnMethods = "testAnomalyCount")
  public void testReplayIsIdemPotent() throws InterruptedException {
    // use update time as a way to know when the replay is done
    final long lastUpdatedTime = getAlertLastUpdatedTime();
    final String alertAnomaliesRoute = "api/anomalies?alert.id=" + alertId;
    final Response beforeReplayResponse = request(alertAnomaliesRoute).get();
    assertThat(beforeReplayResponse.getStatus()).isEqualTo(200);
    final List<AnomalyApi> beforeReplayAnomalies = beforeReplayResponse.readEntity(
        ANOMALIES_LIST_TYPE);

    final MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add("start", String.valueOf(PAGEVIEWS_DATASET_START_TIME));
    final Response replayResponse = request("api/alerts/" + alertId + "/run").post(
        Entity.form(formData));
    assertThat(replayResponse.getStatus()).isEqualTo(200);

    while (getAlertLastUpdatedTime() == lastUpdatedTime) {
      Thread.sleep(1000);
    }

    final Response afterReplayResponse = request(alertAnomaliesRoute).get();
    assertThat(afterReplayResponse.getStatus()).isEqualTo(200);
    final List<AnomalyApi> afterReplayAnomalies = afterReplayResponse.readEntity(
        ANOMALIES_LIST_TYPE);
    // the only contract of the replay is to be user-facing idempotent - hence this test can break if we chose in the implementation to save all anomalies, even the ones at replay
    assertThat(afterReplayAnomalies).hasSize(beforeReplayAnomalies.size());
    for (int i = 0; i < beforeReplayAnomalies.size(); i++){
      assertAnomalyAreTheSame(afterReplayAnomalies.get(i), beforeReplayAnomalies.get(i));
    }
  }

  @Test(dependsOnMethods = "testGetSingleAnomaly")
  public void testGetHeatmap() {
    final Response response = request("api/rca/metrics/heatmap?id=" + anomalyId).get();
    assert200(response);
    final HeatMapResponseApi heatmap = response.readEntity(HeatMapResponseApi.class);
    assertThat(heatmap.getBaseline().getBreakdown().size()).isGreaterThan(0);
    assertThat(heatmap.getCurrent().getBreakdown().size()).isGreaterThan(0);
  }

  @Test(dependsOnMethods = "testGetSingleAnomaly")
  public void testGetTopContributors() {
    final Response response = request("api/rca/dim-analysis?id=" + anomalyId).get();
    assert200(response);
    final DimensionAnalysisResultApi dimensionAnalysisResultApi = response.readEntity(
        DimensionAnalysisResultApi.class);
    assertThat(dimensionAnalysisResultApi.getResponseRows().size()).isGreaterThan(0);
  }

  @Test(dependsOnMethods = "testGetSingleAnomaly")
  public void testSaveInvestigation() {
    final RcaInvestigationApi rcaInvestigationApi = new RcaInvestigationApi().setName(
            "investigationName")
        .setText("textDescription")
        .setAnomaly(new AnomalyApi().setId(anomalyId))
        .setUiMetadata(Map.of("uiKey1", List.of(1, 2), "uiKey2", "foo"));

    final Response response = request("api/rca/investigations").post(
        Entity.json(List.of(rcaInvestigationApi)));
    assert200(response);
  }

  // todo cyril authz move this to authz tests
//  @Test(dependsOnMethods = "testAnomalyCount")
//  @Deprecated
//  public void testCreateAnomalyWithAuth() {
//    final var createAnomalyResp = request("api/anomalies").put(Entity.json(List.of(
//        new AnomalyApi()
//            .setAlert(new AlertApi().setId(alertId))
//            .setAuth(new AuthorizationConfigurationApi().setNamespace("anomaly-namespace"))
//    )));
//    // Anomalies cannot be created with a namespace.
//    assertThat(createAnomalyResp.getStatus()).isEqualTo(400);
//  }
//
//  @Test(timeOut = 60000, dependsOnMethods = "testAnomalyCount")
//  @Deprecated
//  public void testGetAnomalyAuth() throws InterruptedException {
//    var alertId = mustCreateAlert(
//        newRunnableAlertApiWithAuth("TestGetAnomalyAuth", "alert-namespace"));
//
//    waitForAnyAnomalies(alertId);
//    final var anomalyApi = mustGetAnomaliesForAlert(alertId).get(0);
//    assertThat(anomalyApi.getAuth()).isNotNull();
//    assertThat(anomalyApi.getAuth().getNamespace()).isEqualTo("alert-namespace");
//  }
//
//  @Test(timeOut = 60000, dependsOnMethods = "testAnomalyCount")
//  // at write time the namespace is not resolved - either it is correct because passed explicitely, either it is correct because it is the principal active namespace, either the method should fail todo authz test implement these cases in the authz tests
//  @Deprecated 
//  public void testGetRcaInvestigationAuth() throws InterruptedException {
//    final long alertId = mustCreateAlert(
//        newRunnableAlertApiWithAuth("TestGetRcaInvestigationAuth", "alert-namespace"));
//
//    waitForAnyAnomalies(alertId);
//    final Long anomalyId = mustGetAnomaliesForAlert(alertId).get(0).getId();
//    final long investigationId = mustCreateInvestigation(new RcaInvestigationApi()
//        .setName("my-investigation")
//        .setAuth(new AuthorizationConfigurationApi().setNamespace("alert-namespace"))
//        .setAnomaly(new AnomalyApi().setId(anomalyId)));
//
//    final RcaInvestigationApi investigationApi = mustGetInvestigation(investigationId);
//    assertThat(investigationApi.getAuth()).isNotNull();
//    assertThat(investigationApi.getAuth().getNamespace()).isEqualTo("alert-namespace");
//  }

  @Test
  public void testCreateSubscriptionGroup() {
    final var sg = new SubscriptionGroupApi()
        .setName("test-subscription-group")
        .setCron("0 0/5 0 ? * * *");

    final var sgWithHistoricalAnomalies = new SubscriptionGroupApi()
        .setName("test-subscription-group-with-historical-anomalies")
        .setCron("0 0/5 0 ? * * *")
        .setNotifyHistoricalAnomalies(true);

    final var response = request("api/subscription-groups").post(
        Entity.json(List.of(sg, sgWithHistoricalAnomalies)));
    assertThat(response.getStatus()).isEqualTo(200);

    final var response2 = request("api/subscription-groups").get();
    assertThat(response2.getStatus()).isEqualTo(200);
    final var gotSgs = response2.readEntity(new GenericType<List<SubscriptionGroupApi>>() {});
    assertThat(gotSgs).hasSize(2);

    gotSgs.stream().map(SubscriptionGroupApi::getName)
        .forEach(name -> assertThat(name).isIn(sg.getName(), sgWithHistoricalAnomalies.getName()));
  }

  @Test
  public void testGetNamespaceConfiguration() {
    final Response response = request("api/workspace-configuration").get();
    assertThat(response.getStatus()).isEqualTo(200);

    final NamespaceConfigurationApi gotCfgApi = response.readEntity(
        NamespaceConfigurationApi.class);
    namespaceConfigurationId = gotCfgApi.getId();
    assertThat(gotCfgApi.getAuth().getNamespace()).isNull();
    assertThat(gotCfgApi.getTimeConfiguration().getTimezone().toString()).isEqualTo("UTC");
    assertThat(gotCfgApi.getTimeConfiguration().getDateTimePattern()).isEqualTo(
        "MMM dd, yyyy HH:mm");
    assertThat(gotCfgApi.getTimeConfiguration().getMinimumOnboardingStartTime()).isEqualTo(
        946684800000L);

    // fetch again - should return same config
    final Response response2 = request("api/workspace-configuration").get();
    assertThat(response2.getStatus()).isEqualTo(200);
    final NamespaceConfigurationApi gotCfgApi2 = response2.readEntity(
        NamespaceConfigurationApi.class);
    assertNamespaceConfigurationAreSame(gotCfgApi2, gotCfgApi);
  }

  @Test(dependsOnMethods = "testGetNamespaceConfiguration")
  public void testUpdateNamespaceConfiguration() {
    NamespaceConfigurationApi updatedCfg = new NamespaceConfigurationApi();
    updatedCfg.setTimeConfiguration(
        new TimeConfigurationApi()
            .setTimezone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Asia/Kolkata")))
            .setDateTimePattern("MMM dd, yyyy HH:mm")
            .setMinimumOnboardingStartTime(996684800000L));
    updatedCfg.setAuth(new AuthorizationConfigurationApi());
    updatedCfg.setId(namespaceConfigurationId);
    final Response response = request("api/workspace-configuration").put(
        Entity.json(updatedCfg));
    assertThat(response.getStatus()).isEqualTo(200);

    final NamespaceConfigurationApi gotCfgApi = response.readEntity(
        NamespaceConfigurationApi.class);
    assertNamespaceConfigurationAreSame(gotCfgApi, updatedCfg);

    // fetch again after update - should return updated config
    final Response response2 = request("api/workspace-configuration").get();
    assertThat(response2.getStatus()).isEqualTo(200);
    final NamespaceConfigurationApi gotCfgApi2 = response2.readEntity(
        NamespaceConfigurationApi.class);
    assertNamespaceConfigurationAreSame(gotCfgApi2, gotCfgApi);
  }

  @Test(dependsOnMethods = "testUpdateNamespaceConfiguration")
  public void testResetNamespaceConfiguration() {
    final Response response = request("api/workspace-configuration/reset").post(
        Entity.json(Collections.emptyList()));
    assertThat(response.getStatus()).isEqualTo(200);

    final NamespaceConfigurationApi gotCfgApi = response.readEntity(
        NamespaceConfigurationApi.class);
    assertThat(gotCfgApi.getId()).isEqualTo(namespaceConfigurationId);
    assertThat(gotCfgApi.getAuth().getNamespace()).isNull();
    assertThat(gotCfgApi.getTimeConfiguration().getTimezone().toString()).isEqualTo("UTC");
    assertThat(gotCfgApi.getTimeConfiguration().getDateTimePattern()).isEqualTo(
        "MMM dd, yyyy HH:mm");
    assertThat(gotCfgApi.getTimeConfiguration().getMinimumOnboardingStartTime()).isEqualTo(
        946684800000L);

    // fetch again after reset - should return resetted config
    final Response response2 = request("api/workspace-configuration").get();
    assertThat(response2.getStatus()).isEqualTo(200);
    final NamespaceConfigurationApi gotCfgApi2 = response2.readEntity(
        NamespaceConfigurationApi.class);
    assertNamespaceConfigurationAreSame(gotCfgApi2, gotCfgApi);
  }

  private Builder request(final String urlFragment) {
    return client.target(endPoint(urlFragment)).request();
  }

  private String endPoint(final String pathFragment) {
    return String.format("http://localhost:%d/%s", SUPPORT.getLocalPort(), pathFragment);
  }

  private long getAlertLastUpdatedTime() {
    final Response getResponse = request("api/alerts/" + alertId).get();
    assert200(getResponse);
    final AlertApi alert = getResponse.readEntity(AlertApi.class);
    return alert.getUpdated().getTime();
  }

  public static void assert200(final Response response) {
    try {
      assertThat(response.getStatus()).isEqualTo(200);
    } catch (AssertionError e) {
      try {
        System.out.printf("Status 200 assertion failed. Response Status: %s Response content: %s%n",
            response.getStatus(), response.readEntity(Object.class)); 
      } catch (Exception e2) {
        System.out.printf("Status 200 assertion failed and failed to read the response entity. Response Status: %s Response headers: %s.%n",
            response.getStatus(), response.getHeaders());
      }
      throw e;
    }
  }

  private long mustCreateAlert(final AlertApi alertApi) {
    final var response = request("api/alerts")
        .post(Entity.json(List.of(alertApi)));
    assertThat(response.getStatus()).isEqualTo(200);
    final var gotApi = response.readEntity(new GenericType<List<AlertApi>>() {}).getFirst();
    assertThat(gotApi).isNotNull();
    assertThat(gotApi.getId()).isNotNull();
    return gotApi.getId();
  }

  private long mustCreateInvestigation(final RcaInvestigationApi investigationApi) {
    final var response = request("api/rca/investigations")
        .post(Entity.json(List.of(investigationApi)));
    assertThat(response.getStatus()).isEqualTo(200);
    final var gotApi = response.readEntity(new GenericType<List<RcaInvestigationApi>>() {}).getFirst();
    assertThat(gotApi).isNotNull();
    assertThat(gotApi.getId()).isNotNull();
    return gotApi.getId();
  }

  private AlertApi mustGetAlert(long alertId) {
    final var response = request("api/alerts/" + alertId).get();
    assertThat(response.getStatus()).isEqualTo(200);
    final var alertApi = response.readEntity(AlertApi.class);
    assertThat(alertApi).isNotNull();
    return alertApi;
  }

  private AnomalyApi mustGetAnomaly(long anomalyId) {
    final var response = request("api/anomaly/" + anomalyId).get();
    assertThat(response.getStatus()).isEqualTo(200);
    final var anomalyApi = response.readEntity(AnomalyApi.class);
    assertThat(anomalyApi).isNotNull();
    return anomalyApi;
  }

  private List<AnomalyApi> mustGetAnomaliesForAlert(long alertId) {
    final Response resp = request("api/anomalies?alert.id=" + alertId).get();
    assertThat(resp.getStatus()).isEqualTo(200);
    return resp.readEntity(new GenericType<>() {});
  }

  private RcaInvestigationApi mustGetInvestigation(long id) {
    final Response response = request("api/rca/investigations/" + id).get();
    assertThat(response.getStatus()).isEqualTo(200);
    final RcaInvestigationApi investigationApi = response.readEntity(RcaInvestigationApi.class);
    assertThat(investigationApi).isNotNull();
    return investigationApi;
  }

  private void waitForAnyAnomalies(final long alertId) throws InterruptedException {
    List<AnomalyApi> gotAnomalies = mustGetAnomaliesForAlert(alertId);
    while (gotAnomalies.size() == 0) {
      Thread.sleep(1000);
      gotAnomalies = mustGetAnomaliesForAlert(alertId);
    }
  }

  private static AlertApi newRunnableAlertApiWithAuth(final String name, final String namespace) {
    return new AlertApi()
        .setName(name)
        .setCron("0 0 * * * ? *")
        .setTemplate(new AlertTemplateApi().setName("startree-threshold"))
        .setAuth(new AuthorizationConfigurationApi().setNamespace(namespace))
        .setTemplateProperties(Map.of(
            "dataSource", PINOT_DATA_SOURCE_NAME,
            "dataset", PINOT_DATASET_NAME,
            "monitoringGranularity", "P1D",
            "aggregationColumn", "views",
            "aggregationFunction", "sum",
            "max", "1",
            "min", "0"
        ));
  }
}


