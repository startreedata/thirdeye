package org.apache.pinot.thirdeye;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.ThirdEyeH2DatabaseServer.DB_CONFIG;
import static org.apache.pinot.thirdeye.util.DeprecatedInjectorUtil.getInstance;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.util.Duration;
import java.io.IOException;
import java.net.URL;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CoordinatorTest {

  public static final Logger log = LoggerFactory.getLogger(CoordinatorTest.class);
  public static final String THIRDEYE_CONFIG = "./src/test/resources/e2e/config";

  public static final DropwizardTestSupport<ThirdEyeCoordinatorConfiguration> SUPPORT =
      new DropwizardTestSupport<>(ThirdEyeCoordinator.class,
          resourceFilePath("e2e/config/coordinator.yaml"),
          config("configPath", THIRDEYE_CONFIG),
          config("server.connector.port", "0"), // port: 0 implies any port
          config("database.url", DB_CONFIG.getUrl()),
          config("database.user", DB_CONFIG.getUser()),
          config("database.password", DB_CONFIG.getPassword()),
          config("database.driver", DB_CONFIG.getDriver())
      );
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private Client client;
  private ThirdEyeH2DatabaseServer db;

  private static String endPoint(final String pathFragment) {
    return String.format("http://localhost:%d/%s", SUPPORT.getLocalPort(), pathFragment);
  }

  @BeforeClass
  public void beforeClass() {
    db = new ThirdEyeH2DatabaseServer();
    db.start();

    SUPPORT.before();
    final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
    jerseyClientConfiguration.setTimeout(Duration.minutes(1)); // for timeout issues
    client = new JerseyClientBuilder(SUPPORT.getEnvironment())
        .using(jerseyClientConfiguration)
        .build("test client");
  }

  @AfterClass
  public void afterClass() {
    client.close();
    SUPPORT.after();
    db.stop();
  }

  @Test
  public void testPing() {
    Response response = client.target(endPoint("internal/ping"))
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  public void testDataSourcesLoaded() {
    final ThirdEyeCacheRegistry instance = getInstance(ThirdEyeCacheRegistry.class);
    // A single datasource must exist in the db for the tests to proceed
    assertThat(db.executeSql("SELECT * From dataset_config_index").length()).isGreaterThan(0);

    assertThat(instance).isNotNull();
    assertThat(instance.getQueryCache()).isNotNull();
  }

  @Test(dependsOnMethods = "testDataSourcesLoaded", enabled = false)
  public void testEvaluate() throws IOException {
    final URL url = Resources.getResource("e2e/payload_alerts_evaluate.json");
    final AlertEvaluationApi entity = requireNonNull(
        OBJECT_MAPPER.readValue(url, AlertEvaluationApi.class));

    Response response = client.target(endPoint("api/alerts/evaluate"))
        .request()
        .post(Entity.json(entity));

    assertThat(response.getStatus()).isEqualTo(200);

    final AlertEvaluationApi alertEvaluationApi = response.readEntity(AlertEvaluationApi.class);
    assertThat(alertEvaluationApi).isNotNull();
  }
}
