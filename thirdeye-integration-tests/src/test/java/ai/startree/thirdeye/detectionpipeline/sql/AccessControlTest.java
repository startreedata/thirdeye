package ai.startree.thirdeye.detectionpipeline.sql;

import static ai.startree.thirdeye.DropwizardTestUtils.buildClient;
import static ai.startree.thirdeye.DropwizardTestUtils.buildSupport;
import static ai.startree.thirdeye.PinotDataSourceManager.PINOT_DATASET_NAME;
import static ai.startree.thirdeye.PinotDataSourceManager.PINOT_DATA_SOURCE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.IntegrationTestUtils;
import ai.startree.thirdeye.PinotDataSourceManager;
import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.api.AnomalyApi;
import ai.startree.thirdeye.spi.api.AuthorizationConfigurationApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.RcaInvestigationApi;
import io.dropwizard.testing.DropwizardTestSupport;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

// This test validates access control config inheritance for anomalies and investigations.

public class AccessControlTest {

  static private final String ALERT_NAME = "alert-with-auth";
  static private final String ALERT_NAMESPACE = "alert-namespace";

  private DataSourceApi pinotDataSourceApi;
  private DropwizardTestSupport<ThirdEyeServerConfiguration> dropwizardTestSupport;
  private Client client;
  private long alertId;

  @BeforeClass
  void beforeClass() throws Exception {
    pinotDataSourceApi = PinotDataSourceManager.getPinotDataSourceApi();
    final DatabaseConfiguration dbConfiguration = MySqlTestDatabase.sharedDatabaseConfiguration();

    // Setup plugins dir so ThirdEye can load it
    IntegrationTestUtils.setupPluginsDirAbsolutePath();

    dropwizardTestSupport = buildSupport(dbConfiguration, "happypath/config/server.yaml");
    dropwizardTestSupport.before();
    client = buildClient("happy-path-test-client", dropwizardTestSupport);

    mustCreateDatasource();
    mustCreateDataset();
    alertId = mustCreateAlert();
  }

  // Wait until the first run produced an anomaly

  private Builder request(final String urlFragment) {
    final var url = String.format("http://localhost:%d/%s", dropwizardTestSupport.getLocalPort(), urlFragment);
    return client.target(url).request();
  }

  @Test
  public void TestCreateAnomalyWithAuth() {
    final var createAnomalyResp = request("api/anomalies").put(Entity.json(List.of(
        new AnomalyApi()
            .setAlert(new AlertApi().setId(alertId))
            .setAuth(new AuthorizationConfigurationApi().setNamespace("anomaly-namespace"))
    )));
    // Anomalies cannot be created with a namespace.
    assertThat(createAnomalyResp.getStatus()).isEqualTo(400);
  }

  @Test
  public void TestCreateInvestigationWithAuth() throws InterruptedException {
    waitForAnomalies();
    final var anomaly = mustGetAnomaliesForAlert().get(0);
    final var createInvestigationResp = request("api/rca/investigations").post(Entity.json(List.of(
        new RcaInvestigationApi()
            .setName("my-investigation")
            .setAnomaly(new AnomalyApi().setId(anomaly.getId()))
            .setAuth(new AuthorizationConfigurationApi().setNamespace("anomaly-namespace"))
    )));
    // Investigations cannot be created with a namespace.
    assertThat(createInvestigationResp.getStatus()).isEqualTo(400);
  }

  @Test
  public void TestGetAnomalyAuth() throws InterruptedException {
    waitForAnomalies();
    final var anomalyApi = mustGetAnomaliesForAlert().get(0);
    assertThat(anomalyApi.getAuth()).isNotNull();
    assertThat(anomalyApi.getAuth().getNamespace()).isEqualTo(ALERT_NAMESPACE);
  }

  @Test
  public void TestGetRcaInvestigationAuth() throws InterruptedException {
    waitForAnomalies();
    final var investigationId = mustCreateInvestigation();
    final var investigationApi = mustGetInvestigation(investigationId);
    assertThat(investigationApi.getAuth()).isNotNull();
    assertThat(investigationApi.getAuth().getNamespace()).isEqualTo(ALERT_NAMESPACE);
  }

  @Test
  public void TestUpdateAlertAuth() throws InterruptedException {
    waitForAnomalies();
    final var rcaId = mustCreateInvestigation();

    final var alertApi = newTestAlertApi();
    alertApi.setAuth(new AuthorizationConfigurationApi().setNamespace("new-alert-namespace"));

    final var updateAlertResp = request("api/alerts").put(Entity.json(List.of(alertApi)));
    assertThat(updateAlertResp.getStatus()).isEqualTo(200);

    final var gotAlertApi = updateAlertResp.readEntity(AlertApi.class);
    assertThat(gotAlertApi.getAuth()).isNotNull();
    assertThat(gotAlertApi.getAuth().getNamespace()).isEqualTo("new-alert-namespace");

    final var anomaly = mustGetAnomaliesForAlert().get(0);
    assertThat(anomaly.getAuth()).isNotNull();
    assertThat(anomaly.getAuth().getNamespace()).isEqualTo("new-alert-namespace");

    final var investigationApi = mustGetInvestigation(rcaId);
    assertThat(investigationApi.getAuth()).isNotNull();
    assertThat(investigationApi.getAuth().getNamespace()).isEqualTo("new-alert-namespace");
  }

  private void mustCreateDatasource() {
    final Response response = request("api/data-sources")
        .post(Entity.json(List.of(pinotDataSourceApi)));
    assertThat(response.getStatus()).isEqualTo(200);
  }

  private void mustCreateDataset() {
    final Response response = request("api/data-sources/onboard-dataset/")
        .post(Entity.form(new MultivaluedHashMap<>() {{
          add("dataSourceName", PINOT_DATA_SOURCE_NAME);
          add("datasetName", PINOT_DATASET_NAME);
        }}));
    assertThat(response.getStatus()).isEqualTo(200);
  }

  private long mustCreateAlert() {
    final var response = request("api/alerts").post(Entity.json(List.of(newTestAlertApi())));
    assertThat(response.getStatus()).isEqualTo(200);
    final var alertApi = response.readEntity(new GenericType<List<AlertApi>>() {}).get(0);
    assertThat(alertApi).isNotNull();
    assertThat(alertApi.getId()).isNotNull();
    return alertApi.getId();
  }

  private AlertApi mustGetAlert(long alertId) {
    final var response = request("api/alerts/" + alertId).get();
    assertThat(response.getStatus()).isEqualTo(200);
    final var alertApi = response.readEntity(AlertApi.class);
    assertThat(alertApi).isNotNull();
    return alertApi;
  }

  static private AlertApi newTestAlertApi() {
    return new AlertApi()
        .setName(ALERT_NAME)
        .setTemplate(new AlertTemplateApi().setName("startree-threshold"))
        .setAuth(new AuthorizationConfigurationApi().setNamespace(ALERT_NAMESPACE))
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

  private void waitForAnomalies() throws InterruptedException {
    List<AnomalyApi> gotAnomalies = mustGetAnomaliesForAlert();
    while (gotAnomalies.size() == 0) {
      Thread.sleep(1000);
      gotAnomalies = mustGetAnomaliesForAlert();
    }
  }

  List<AnomalyApi> mustGetAnomaliesForAlert() {
    final var resp = request("/api/anomalies?alert.id=" + alertId).get();
    assertThat(resp.getStatus()).isEqualTo(200);
    return resp.readEntity(new GenericType<>() {});
  }

  private long mustCreateInvestigation() {
    final var anomaly = mustGetAnomaliesForAlert().get(0);
    final var response = request("api/rca/investigations").post(Entity.json(List.of(
        new RcaInvestigationApi()
            .setName("my-investigation")
            .setAnomaly(new AnomalyApi().setId(anomaly.getId()))
    )));
    assertThat(response.getStatus()).isEqualTo(200);
    final var investigationApi = response.readEntity(new GenericType<List<RcaInvestigationApi>>() {}).get(0);
    assertThat(investigationApi).isNotNull();
    assertThat(investigationApi.getId()).isNotNull();
    return investigationApi.getId();
  }

  private RcaInvestigationApi mustGetInvestigation(long id) {
    final var response = request("/api/rca/investigations/" + id).get();
    assertThat(response.getStatus()).isEqualTo(200);
    final var investigationApi = response.readEntity(RcaInvestigationApi.class);
    assertThat(investigationApi).isNotNull();
    return investigationApi;
  }
}
