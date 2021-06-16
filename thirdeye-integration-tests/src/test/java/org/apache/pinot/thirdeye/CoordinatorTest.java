package org.apache.pinot.thirdeye;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.Constants.SYS_PROP_THIRDEYE_PLUGINS_DIR;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.util.Duration;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.spi.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.spi.api.DataSourceApi;
import org.apache.pinot.thirdeye.spi.api.DatasetApi;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CoordinatorTest {

  public static final String THIRDEYE_CONFIG = "./src/test/resources/e2e/config";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public DropwizardTestSupport<ThirdEyeCoordinatorConfiguration> SUPPORT;
  private Client client;
  private ThirdEyeH2DatabaseServer db;

  private String endPoint(final String pathFragment) {
    return String.format("http://localhost:%d/%s", SUPPORT.getLocalPort(), pathFragment);
  }

  @BeforeClass
  public void beforeClass() {
    db = new ThirdEyeH2DatabaseServer("localhost", 7124, null);
    db.start();

    // Setup plugins dir so ThirdEye can load it
    setupPluginsDirAbsolutePath();

    SUPPORT = new DropwizardTestSupport<>(ThirdEyeCoordinator.class,
        resourceFilePath("e2e/config/coordinator.yml"),
        config("configPath", THIRDEYE_CONFIG),
        config("server.connector.port", "0"), // port: 0 implies any port
        config("database.url", db.getDbConfig().getUrl()),
        config("database.user", db.getDbConfig().getUser()),
        config("database.password", db.getDbConfig().getPassword()),
        config("database.driver", db.getDbConfig().getDriver())
    );
    SUPPORT.before();
    final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
    jerseyClientConfiguration.setTimeout(Duration.minutes(1)); // for timeout issues
    client = new JerseyClientBuilder(SUPPORT.getEnvironment())
        .using(jerseyClientConfiguration)
        .build("test client");
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
    assertThat(pluginsDir.exists() && pluginsDir.isDirectory()).isTrue();

    System.setProperty(SYS_PROP_THIRDEYE_PLUGINS_DIR, pluginsDir.getAbsolutePath());
  }

  @AfterClass
  public void afterClass() {
    client.close();
    SUPPORT.after();
    db.stop();
  }

  @Test
  public void testPing() {
    Response response = request("internal/ping")
        .get();

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test(dependsOnMethods = "testPing")
  public void testLoadMockDataSource() throws IOException {
    final DataSourceApi dataSourceApi = loadApiFromFile("data-source-mock.json",
        DataSourceApi.class);
    Response response;
    response = request("api/data-sources")
        .post(Entity.json(singletonList(dataSourceApi)));

    assertThat(response.getStatus()).isEqualTo(200);

    // A single datasource must exist in the db for the tests to proceed
    assertThat(db.executeSql("SELECT * From data_source_index").length())
        .isEqualTo(1);

    response = request("api/data-sources/onboard-all")
        .post(Entity.form(new Form().param("dataSourceName", dataSourceApi.getName())));

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(db.executeSql("SELECT * From dataset_config_index").length()).isEqualTo(2);

    response = request("api/datasets").get();
    assertThat(response.getStatus()).isEqualTo(200);

    // validate all datasets should point to the same datasource
    response
        .readEntity(new GenericType<List<DatasetApi>>() {})
        .stream()
        .map(DatasetApi::getDataSource)
        .map(DataSourceApi::getName)
        .forEach(name -> assertThat(name).isEqualTo(dataSourceApi.getName()));

    assertThat(db.executeSql("SELECT * From metric_config_index").length()).isEqualTo(4);
  }

  @Test(dependsOnMethods = "testLoadMockDataSource")
  public void testEvaluate() throws IOException {
    final AlertEvaluationApi entity = loadApiFromFile("payload_alerts_evaluate.json",
        AlertEvaluationApi.class);

    final Response response = request("api/alerts/evaluate")
        .post(Entity.json(entity));

    assertThat(response.getStatus()).isEqualTo(200);

    final AlertEvaluationApi alertEvaluationApi = response.readEntity(AlertEvaluationApi.class);
    assertThat(alertEvaluationApi).isNotNull();
  }

  private Builder request(final String urlFragment) {
    return client.target(endPoint(urlFragment)).request();
  }

  private <T> T loadApiFromFile(final String filename, final Class<T> clazz)
      throws IOException {
    final URL url = Resources.getResource("e2e/" + filename);
    return requireNonNull(OBJECT_MAPPER.readValue(url, clazz));
  }
}
