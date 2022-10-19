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

import static ai.startree.thirdeye.DropwizardTestUtils.alertEvaluationApi;
import static ai.startree.thirdeye.DropwizardTestUtils.buildClient;
import static ai.startree.thirdeye.DropwizardTestUtils.buildSupport;
import static ai.startree.thirdeye.DropwizardTestUtils.loadAlertApi;
import static ai.startree.thirdeye.PinotContainerManager.PINOT_DATASET_NAME;
import static ai.startree.thirdeye.PinotContainerManager.PINOT_DATA_SOURCE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.api.AlertInsightsApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi;
import ai.startree.thirdeye.spi.api.EmailSchemeApi;
import ai.startree.thirdeye.spi.api.HeatMapResponseApi;
import ai.startree.thirdeye.spi.api.NotificationSchemesApi;
import ai.startree.thirdeye.spi.api.RcaInvestigationApi;
import ai.startree.thirdeye.spi.api.SubscriptionGroupApi;
import io.dropwizard.testing.DropwizardTestSupport;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.pinot.testcontainer.PinotContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
 * - get a single anomaly
 * - get the anomaly breakdown (heatmap)
 */
public class HappyPathTest {

  private static final Logger log = LoggerFactory.getLogger(HappyPathTest.class);

  private static final AlertApi MAIN_ALERT_API;
  private static final long PAGEVIEWS_DATASET_START_TIME_PLUS_ONE_DAY = 1580688000000L;
  private static final long PAGEVIEWS_DATASET_END_TIME = 1596067200000L;
  private static final long PAGEVIEWS_DATASET_START_TIME = 1580601600000L;

  public static final long EVALUATE_END_TIME = 1596326400000L;

  static {
    try {
      MAIN_ALERT_API = loadAlertApi("/happypath/payloads/" + "alert.json");
    } catch (IOException e) {
      throw new RuntimeException(String.format("Could not load alert json: %s", e));
    }
  }

  private PinotContainer pinotContainer;
  private DropwizardTestSupport<ThirdEyeServerConfiguration> SUPPORT;
  private Client client;

  // this attribute is shared between tests
  private long anomalyId;
  private long alertId;

  @BeforeClass
  public void beforeClass() throws Exception {
    pinotContainer = PinotContainerManager.getInstance();
    final DatabaseConfiguration dbConfiguration = MySqlTestDatabase.sharedDatabaseConfiguration();

    // Setup plugins dir so ThirdEye can load it
    IntegrationTestUtils.setupPluginsDirAbsolutePath();

    SUPPORT = buildSupport(dbConfiguration, "happypath/config/server.yaml");
    SUPPORT.before();
    client = buildClient("happy-path-test-client", SUPPORT);
  }

  @AfterClass(alwaysRun = true)
  public void afterClass() {
    log.info("Stopping Thirdeye at port: {}", SUPPORT.getLocalPort());
    SUPPORT.after();
    MySqlTestDatabase.cleanSharedDatabase();
  }

  @Test()
  public void testPing() {
    Response response = request("internal/ping").get();
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testPing")
  public void testCreatePinotDataSource() {
    DataSourceApi dataSourceApi = new DataSourceApi().setName(PINOT_DATA_SOURCE_NAME)
        .setType("pinot")
        .setProperties(
            Map.of("zookeeperUrl", "localhost:" + pinotContainer.getZookeeperPort(), "brokerUrl",
                pinotContainer.getPinotBrokerUrl().replace("http://", ""), "clusterName",
                "QuickStartCluster", "controllerConnectionScheme", "http", "controllerHost",
                "localhost", "controllerPort", pinotContainer.getControllerPort()));

    Response response = request("api/data-sources").post(Entity.json(List.of(dataSourceApi)));
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testPing")
  public void testCreateDefaultTemplates() {
    MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add("updateExisting", "true");
    Response response = request("/api/alert-templates/load-defaults").post(Entity.form(formData));
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testCreatePinotDataSource")
  public void testCreateDataset() {
    MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add("dataSourceName", PINOT_DATA_SOURCE_NAME);
    formData.add("datasetName", PINOT_DATASET_NAME);

    Response response = request("api/data-sources/onboard-dataset/").post(Entity.form(formData));
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testCreateDataset")
  public void testEvaluateAlert() {
    AlertEvaluationApi alertEvaluationApi = alertEvaluationApi(MAIN_ALERT_API,
        PAGEVIEWS_DATASET_START_TIME, EVALUATE_END_TIME);

    Response response = request("api/alerts/evaluate").post(Entity.json(alertEvaluationApi));
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testEvaluateAlert")
  public void testCreateAlert() {
    Response response = request("api/alerts").post(Entity.json(List.of(MAIN_ALERT_API)));

    assertThat(response.getStatus()).isEqualTo(200);
    List<Map<String, Object>> alerts = response.readEntity(List.class);
    alertId = ((Number) alerts.get(0).get("id")).longValue();
  }

  @DataProvider(name = "happyPathAlerts")
  public Object[][] happyPathAlerts() {
    // one alert for each template
    return new Object[][]{{"startree-absolute-rule-alert.json"},
        {"startree-absolute-rule-percentile-alert.json"}, {"startree-mean-variance-alert.json"},
        {"startree-mean-variance-percentile-alert.json"}, {"startree-percentage-rule-alert.json"},
        {"startree-percentage-rule-percentile-alert.json"}, {"startree-threshold-alert.json"},
        {"startree-threshold-percentile-alert.json"}};
  }

  @Test(dependsOnMethods = "testEvaluateAlert", timeOut = 10000L, dataProvider = "happyPathAlerts")
  public void testEvaluateAllHappyPathAlerts(final String alertJson) throws IOException {
    final AlertApi alertApi = loadAlertApi("/happypath/payloads/" + alertJson);
    final AlertEvaluationApi alertEvaluationApi = alertEvaluationApi(alertApi,
        PAGEVIEWS_DATASET_START_TIME, EVALUATE_END_TIME);

    Response response = request("api/alerts/evaluate").post(Entity.json(alertEvaluationApi));
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testCreateAlert")
  public void testAlertInsights() {
    final Response response = request("api/alerts/" + alertId + "/insights").get();
    assertThat(response.getStatus()).isEqualTo(200);
    AlertInsightsApi insights = response.readEntity(AlertInsightsApi.class);
    assertThat(insights.getTemplateWithProperties().getMetadata().getGranularity()).isEqualTo(
        "P1D");
    assertThat(insights.getDefaultStartTime()).isEqualTo(PAGEVIEWS_DATASET_START_TIME_PLUS_ONE_DAY);
    assertThat(insights.getDefaultEndTime()).isEqualTo(PAGEVIEWS_DATASET_END_TIME);
    assertThat(insights.getDatasetStartTime()).isEqualTo(PAGEVIEWS_DATASET_START_TIME);
    assertThat(insights.getDatasetEndTime()).isEqualTo(PAGEVIEWS_DATASET_END_TIME);
  }

  @Test(dependsOnMethods = "testCreateAlert")
  public void testCreateSubscription() {
    SubscriptionGroupApi subscriptionGroupApi = new SubscriptionGroupApi().setName(
            "testSubscription")
        .setCron("")
        .setNotificationSchemes(new NotificationSchemesApi().setEmail(
            new EmailSchemeApi().setTo(List.of("analyst@fake.mail"))))
        .setAlerts(List.of(new AlertApi().setId(alertId)));
    Response response = request("api/subscription-groups").post(
        Entity.json(List.of(subscriptionGroupApi)));
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testCreateAlert", timeOut = 50000L)
  public void testGetAnomalies() throws InterruptedException {
    // test get anomalies
    // need to wait for the taskRunner to run the onboard task - can take some time
    List<Map<String, Object>> anomalies = List.of();
    while (anomalies.size() == 0) {
      // see taskDriver server config for optimization
      Thread.sleep(1000);
      Response response = request("api/anomalies").get();
      assertThat(response.getStatus()).isEqualTo(200);
      anomalies = response.readEntity(List.class);
    }
    // the second anomaly is the March 21 - March 23 anomaly
    anomalyId = (int) anomalies.get(1).get("id");
  }

  @Test(dependsOnMethods = "testGetAnomalies")
  public void testGetSingleAnomaly() {
    // test get a single anomaly
    Response response = request("api/anomalies/" + anomalyId).get();
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testGetSingleAnomaly")
  public void testGetHeatmap() {
    Response response = request("api/rca/metrics/heatmap?id=" + anomalyId).get();
    assertThat(response.getStatus()).isEqualTo(200);
    HeatMapResponseApi heatmap = response.readEntity(HeatMapResponseApi.class);
    assertThat(heatmap.getBaseline().getBreakdown().size()).isGreaterThan(0);
    assertThat(heatmap.getCurrent().getBreakdown().size()).isGreaterThan(0);
  }

  @Test(dependsOnMethods = "testGetSingleAnomaly")
  public void testGetTopContributors() {
    Response response = request("api/rca/dim-analysis?id=" + anomalyId).get();
    assertThat(response.getStatus()).isEqualTo(200);
    DimensionAnalysisResultApi dimensionAnalysisResultApi = response.readEntity(
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
    assertThat(response.getStatus()).isEqualTo(200);
  }

  private Builder request(final String urlFragment) {
    return client.target(endPoint(urlFragment)).request();
  }

  private String endPoint(final String pathFragment) {
    return String.format("http://localhost:%d/%s", SUPPORT.getLocalPort(), pathFragment);
  }
}


