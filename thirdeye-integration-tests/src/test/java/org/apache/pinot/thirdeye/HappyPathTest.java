package org.apache.pinot.thirdeye;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.Constants.SYS_PROP_THIRDEYE_PLUGINS_DIR;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.DropwizardTestSupport;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.pinot.testcontainer.AddTable;
import org.apache.pinot.testcontainer.ImportData;
import org.apache.pinot.testcontainer.PinotContainer;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.database.ThirdEyeMySQLContainer;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.spi.api.DataSourceApi;
import org.apache.pinot.thirdeye.spi.api.EmailSchemeApi;
import org.apache.pinot.thirdeye.spi.api.NotificationSchemesApi;
import org.apache.pinot.thirdeye.spi.api.SubscriptionGroupApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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
  private static final String RESOURCES_PATH = "/happypath";
  private static final String THIRDEYE_CONFIG = "./src/test/resources/happypath/config";
  private static final String MYSQL_DOCKER_IMAGE = "mysql:5.7.37";

  private static final String INGESTION_JOB_SPEC_FILENAME = "batch-job-spec.yml";
  private static final String SCHEMA_FILENAME = "schema.json";
  private static final String TABLE_CONFIG_FILENAME = "table-config.json";
  private static final String DATA_FILENAME = "data.csv";
  private static final String DATA_SOURCE_NAME = "PinotContainer";
  private static final String DATASET_NAME = "pageviews";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final AlertApi ALERT_API;

  static {
    try {
      String alertPath = String.format("%s/payloads/alert.json", RESOURCES_PATH);
      String alertApiJson = IOUtils.resourceToString(alertPath, StandardCharsets.UTF_8);
      ALERT_API = OBJECT_MAPPER.readValue(alertApiJson, AlertApi.class);
    } catch (IOException e) {
      throw new RuntimeException(String.format("Could not load alert json: %s", e));
    }
  }

  // make containers singleton to share between instances https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/
  private PinotContainer pinotContainer;
  private DropwizardTestSupport<ThirdEyeServerConfiguration> SUPPORT;
  private Client client;
  private JdbcDatabaseContainer<?> persistenceDbContainer;

  // this attribute is shared between tests
  private long anomalyId;
  private long alertId;

  private PinotContainer startPinot() {
    URL datasetsBaseResource = getClass().getClassLoader().getResource("datasets");
    com.google.common.base.Preconditions.checkNotNull(datasetsBaseResource);

    final String datasetsBasePath = datasetsBaseResource.getFile();
    File[] directories = new File(datasetsBasePath).listFiles(File::isDirectory);
    List<AddTable> addTableList = new ArrayList<>();
    List<ImportData> importDataList = new ArrayList<>();
    for (File dir : directories) {
      String tableName = dir.getName();
      File schemaFile = Paths.get(datasetsBasePath, tableName, SCHEMA_FILENAME).toFile();
      File tableConfigFile = Paths.get(datasetsBasePath, tableName, TABLE_CONFIG_FILENAME).toFile();
      addTableList.add(new AddTable(schemaFile, tableConfigFile));

      File batchJobSpecFile = Paths.get(datasetsBasePath, tableName, INGESTION_JOB_SPEC_FILENAME)
          .toFile();
      File dataFile = Paths.get(datasetsBasePath, tableName, DATA_FILENAME).toFile();
      importDataList.add(new ImportData(batchJobSpecFile, dataFile));
    }
    pinotContainer = new PinotContainer(addTableList, importDataList);
    pinotContainer.start();

    return pinotContainer;
  }

  @BeforeClass
  public void beforeClass() throws Exception {
    persistenceDbContainer = new ThirdEyeMySQLContainer(MYSQL_DOCKER_IMAGE);
    persistenceDbContainer.start();

    // Setup plugins dir so ThirdEye can load it
    setupPluginsDirAbsolutePath();

    pinotContainer = startPinot();
    pinotContainer.addTables();
    SUPPORT = new DropwizardTestSupport<>(ThirdEyeServer.class,
        resourceFilePath("happypath/config/server.yaml"),
        config("configPath", THIRDEYE_CONFIG),
        config("server.connector.port", "0"), // port: 0 implies any port
        config("database.url", persistenceDbContainer.getJdbcUrl() + "?autoreconnect=true"),
        config("database.user", persistenceDbContainer.getUsername()),
        config("database.password", persistenceDbContainer.getPassword()),
        config("database.driver", persistenceDbContainer.getDriverClassName())
    );
    SUPPORT.before();
    final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
    jerseyClientConfiguration.setTimeout(io.dropwizard.util.Duration.minutes(1)); // for timeout issues
    client = new JerseyClientBuilder(SUPPORT.getEnvironment())
        .using(jerseyClientConfiguration)
        .build("test client");
  }

  private void setupPluginsDirAbsolutePath() {
    // fixme cyril dependency on distribution - prevents parallel testing
    final String projectBuildDirectory = requireNonNull(System.getProperty("projectBuildDirectory"),
        "project build dir not set");
    final String projectVersion = requireNonNull(System.getProperty("projectVersion"),
        "project version not set");
    final String pluginsPath = new StringBuilder()
        .append(projectBuildDirectory)
        .append("/../../thirdeye-distribution/target/thirdeye-distribution-")
        .append(projectVersion)
        .append("-dist/thirdeye-distribution-")
        .append(projectVersion)
        .append("/plugins")
        .toString();
    final File pluginsDir = new File(pluginsPath);
    assertThat(pluginsDir.exists()).isTrue();
    assertThat(pluginsDir.isDirectory()).isTrue();

    System.setProperty(SYS_PROP_THIRDEYE_PLUGINS_DIR, pluginsDir.getAbsolutePath());
  }

  @AfterClass
  public void afterClass() {
    log.info("Pinot container port: {}", pinotContainer.getPinotBrokerUrl());
    log.info("Thirdeye port: {}", SUPPORT.getLocalPort());
    SUPPORT.after();
    pinotContainer.stop();
    persistenceDbContainer.stop();
  }

  @Test()
  public void testPing() {
    Response response = request("internal/ping").get();
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testPing")
  public void testCreatePinotDataSource() {
    DataSourceApi dataSourceApi = new DataSourceApi()
        .setName(DATA_SOURCE_NAME)
        .setType("pinot")
        .setProperties(Map.of(
            "zookeeperUrl", "localhost:" + pinotContainer.getZookeeperPort(),
            "brokerUrl", pinotContainer.getPinotBrokerUrl().replace("http://", ""),
            "clusterName", "QuickStartCluster", // really not sure here
            "controllerConnectionScheme", "http",
            "controllerHost", "localhost",
            "controllerPort", pinotContainer.getControllerPort())
        );

    Response response = request("api/data-sources")
        .post(Entity.json(List.of(dataSourceApi)));
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testCreatePinotDataSource")
  public void testCreateDataset() {
    MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add("dataSourceName", DATA_SOURCE_NAME);
    formData.add("datasetName", DATASET_NAME);

    Response response = request("api/data-sources/onboard-dataset/")
        .post(Entity.form(formData));
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testCreateDataset")
  public void testEvaluateAlert() {
    AlertEvaluationApi alertEvaluationApi = new AlertEvaluationApi()
        .setAlert(ALERT_API)
        .setStart(Date.from(Instant.ofEpochMilli(1580601600000L))) //Sunday, 2 February 2020 00:00:00
        .setEnd(Date.from(Instant.ofEpochMilli(1596326400000L)));  //Sunday, 2 August 2020 00:00:00

    Response response = request("api/alerts/evaluate")
        .post(Entity.json(alertEvaluationApi));

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testEvaluateAlert")
  public void testCreateAlert() {
    Response response = request("api/alerts")
        .post(Entity.json(List.of(ALERT_API)));

    assertThat(response.getStatus()).isEqualTo(200);
    List<Map<String, Object>> alerts = response.readEntity(List.class);
    alertId = ((Number) alerts.get(0).get("id")).longValue();
  }

  @Test(dependsOnMethods = "testCreateAlert")
  public void testCreateSubscription() {
    SubscriptionGroupApi subscriptionGroupApi = new SubscriptionGroupApi()
        .setName("testSubscription")
        .setCron("")
        .setNotificationSchemes(new NotificationSchemesApi()
            .setEmail(new EmailSchemeApi().setTo(List.of("analyst@fake.mail"))))
        .setAlerts(List.of(
            new AlertApi().setId(alertId)
        ));
    Response response = request("api/subscription-groups")
        .post(Entity.json(List.of(subscriptionGroupApi)));
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testCreateAlert", timeOut = 50000L, groups = {"longTest"})
  public void testGetAnomalies() throws InterruptedException {
    // test get anomalies
    // need to wait for the taskRunner to run the onboard task - can take some time
    List<Map<String, Object>> anomalies = List.of();
    while (anomalies.size() == 0) {
      // see taskDriver server config for optimization
      Thread.sleep(8000);
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
  public void testGetBreakdown() {
    // todo cyril replace by metrics/heatmap/anomaly/{id} once frontend has changed
    Response response = request("api/rca/metrics/breakdown/anomaly/" + anomalyId).get();
    assertThat(response.getStatus()).isEqualTo(200);
    Map<String, ?> breakdown = response.readEntity(Map.class);
    assertThat(breakdown.size()).isGreaterThan(0);
  }

  private Builder request(final String urlFragment) {
    return client.target(endPoint(urlFragment)).request();
  }

  private String endPoint(final String pathFragment) {
    return String.format("http://localhost:%d/%s", SUPPORT.getLocalPort(), pathFragment);
  }
}


