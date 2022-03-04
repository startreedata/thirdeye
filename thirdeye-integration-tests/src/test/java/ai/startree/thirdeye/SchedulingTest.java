/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye;

import static ai.startree.thirdeye.spi.Constants.SYS_PROP_THIRDEYE_PLUGINS_DIR;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.database.ThirdEyeH2DatabaseServer;
import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.utils.TimeProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.DropwizardTestSupport;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Scheduler tests. Time is mocked with the TimeProvider.
 *
 * Note: if run within IntelliJ, run with the following JVM option:
 * -javaagent:[USER_PATH]/.m2/repository/org/aspectj/aspectjweaver/1.9.6/aspectjweaver-1.9.6.jar
 * IntelliJ does not use the pom surefire config: https://youtrack.jetbrains.com/issue/IDEA-52286
 */
// todo cyril pinot is not necessary - implement and use csv/in-memory datasource instead
public class SchedulingTest extends PinotBasedIntegrationTest {

  private static final Logger log = LoggerFactory.getLogger(SchedulingTest.class);
  private static final String RESOURCES_PATH = "/scheduling";
  private static final String THIRDEYE_CONFIG = "./src/test/resources/scheduling/config";
  private static final String MYSQL_DOCKER_IMAGE = "mysql:8.0";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final AlertApi ALERT_API;

  private static final TimeProvider CLOCK = TimeProvider.instance();
  private static final long ONE_DAY_MILLIS = 86400000L;
  private static final long MARCH_21_2020 = 1584748800_000L;
  private static final long MARCH_22_2020 = MARCH_21_2020 + ONE_DAY_MILLIS;

  private ThirdEyeH2DatabaseServer db;

  static {
    try {
      String alertPath = String.format("%s/payloads/alert.json", RESOURCES_PATH);
      String alertApiJson = IOUtils.resourceToString(alertPath, StandardCharsets.UTF_8);
      ALERT_API = OBJECT_MAPPER.readValue(alertApiJson, AlertApi.class);
    } catch (IOException e) {
      throw new RuntimeException(String.format("Could not load alert json: %s", e));
    }
  }

  private DropwizardTestSupport<ThirdEyeServerConfiguration> SUPPORT;
  private Client client;

  private long alertId;


  @BeforeClass
  public void beforeClass() throws SQLException {
    db = new ThirdEyeH2DatabaseServer("localhost", 7120, "ThirdEyeIntegrationTest");
    db.start();
    db.truncateAllTables();;
    // Setup plugins dir so ThirdEye can load it
    setupPluginsDirAbsolutePath();

    SUPPORT = new DropwizardTestSupport<>(ThirdEyeServer.class,
        resourceFilePath("scheduling/config/server.yaml"),
        config("configPath", THIRDEYE_CONFIG),
        config("server.connector.port", "0"), // port: 0 implies any port
        config("database.url", db.getDbConfig().getUrl()),
        config("database.user", db.getDbConfig().getUser()),
        config("database.password", db.getDbConfig().getPassword()),
        config("database.driver", db.getDbConfig().getDriver())
    );
    SUPPORT.before();
    final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
    jerseyClientConfiguration.setTimeout(io.dropwizard.util.Duration.minutes(1)); // for timeout issues
    client = new JerseyClientBuilder(SUPPORT.getEnvironment())
        .using(jerseyClientConfiguration)
        .build("test-client-scheduling");
  }

  private void setupPluginsDirAbsolutePath() {
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
    CLOCK.useSystemTime();
    log.info("Stopping Thirdeye at port: {}", SUPPORT.getLocalPort());
    SUPPORT.after();
    log.info("Stopping H2 Db at port: {}", db.getDbConfig().getUrl());
    db.stop();
  }

  @Test()
  public void checkTimeIsControlled() {
    // ensure time is controlled via the TimeProvider CLOCK - ie weaving is working correctly
    CLOCK.useMockTime(0);
    assertThat(System.currentTimeMillis()).isEqualTo(0);
    assertThat(new Date().getTime()).isEqualTo(0);
    CLOCK.tick(20);
    assertThat(System.currentTimeMillis()).isEqualTo(20);
    assertThat(new Date().getTime()).isEqualTo(20);
  }

  @Test(dependsOnMethods = "checkTimeIsControlled")
  public void setUpData() {
    Response response = request("internal/ping").get();
    assertThat(response.getStatus()).isEqualTo(200);

    // create datasource
    DataSourceApi dataSourceApi = new DataSourceApi()
        .setName(PINOT_DATA_SOURCE_NAME)
        .setType("pinot")
        .setProperties(Map.of(
            "zookeeperUrl", "localhost:" + pinotContainer.getZookeeperPort(),
            "brokerUrl", pinotContainer.getPinotBrokerUrl().replace("http://", ""),
            "clusterName", "QuickStartCluster", // really not sure here
            "controllerConnectionScheme", "http",
            "controllerHost", "localhost",
            "controllerPort", pinotContainer.getControllerPort())
        );
    response = request("api/data-sources")
        .post(Entity.json(List.of(dataSourceApi)));
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
    CLOCK.useMockTime(MARCH_21_2020);

    Response createResponse = request("api/alerts")
        .post(Entity.json(List.of(ALERT_API)));
    assertThat(createResponse.getStatus()).isEqualTo(200);
    List<Map<String, Object>> alerts = createResponse.readEntity(List.class);
    alertId = ((Number) alerts.get(0).get("id")).longValue();

    // check that lastTimestamp just after creation is 0
    long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(0);
  }

  @Test(dependsOnMethods = "testCreateAlertLastTimestamp")
  public void testOnboardingLastTimestamp() throws Exception {
    // wait for anomalies - proxy to know when the onboarding task has run
    List<Map<String, Object>> anomalies = List.of();
    while (anomalies.size() == 0) {
      // see taskDriver server config for optimization
      Thread.sleep(8000);
      anomalies = getAnomalies();
    }

    // check that lastTimestamp is the endTime of the Onboarding task: March 21 1H
    long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(MARCH_21_2020);
  }

  @Test(dependsOnMethods = "testOnboardingLastTimestamp")
  public void testAfterDetectionCronLastTimestamp() throws InterruptedException {
    // get current number of anomalies
    List<Map<String, Object>> anomalies = getAnomalies();
    int numAnomaliesBeforeDetectionRun = anomalies.size();

    // advance time to March 22, 2020, 00:00:00 UTC
    // this should trigger the cron - and a new anomaly is expected on [March 21 - March 22]
    CLOCK.useMockTime(MARCH_22_2020);
    // not exact time should not impact lastTimestamp
    CLOCK.tick(5);
    // give thread to quartz scheduler - (quartz idle time is weaved to 1000 ms for test speed)
    Thread.sleep(1000);

    // wait for the new anomaly to be created - proxy to know when the detection has run
    while (anomalies.size() == numAnomaliesBeforeDetectionRun) {
      Thread.sleep(8000);
      anomalies = getAnomalies();
    }

    // check that lastTimestamp after detection is the runTime of the cron
    long alertLastTimestamp = getAlertLastTimestamp();
    assertThat(alertLastTimestamp).isEqualTo(MARCH_22_2020);
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


