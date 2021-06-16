package org.apache.pinot.thirdeye;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.apache.pinot.thirdeye.auth.AuthConfiguration;
import org.apache.pinot.thirdeye.config.MockEventsConfiguration;
import org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration;

public class ThirdEyeCoordinatorConfiguration extends Configuration {

  @JsonProperty("auth")
  private AuthConfiguration authConfiguration;

  @JsonProperty("database")
  private DatabaseConfiguration databaseConfiguration;

  @JsonProperty("swagger")
  private SwaggerBundleConfiguration swaggerBundleConfiguration;

  @JsonProperty("mockEvents")
  private MockEventsConfiguration mockEventsConfiguration = new MockEventsConfiguration();

  private String configPath = "config";
  private boolean schedulerEnabled = false;
  private boolean taskDriverEnabled = false;

  public AuthConfiguration getAuthConfiguration() {
    return authConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setAuthConfiguration(
      final AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
    return this;
  }

  public SwaggerBundleConfiguration getSwaggerBundleConfiguration() {
    return swaggerBundleConfiguration;
  }

  public void setSwaggerBundleConfiguration(
      final SwaggerBundleConfiguration swaggerBundleConfiguration) {
    this.swaggerBundleConfiguration = swaggerBundleConfiguration;
  }

  public DatabaseConfiguration getDatabaseConfiguration() {
    return databaseConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setDatabaseConfiguration(
      final DatabaseConfiguration databaseConfiguration) {
    this.databaseConfiguration = databaseConfiguration;
    return this;
  }

  public String getConfigPath() {
    return configPath;
  }

  public ThirdEyeCoordinatorConfiguration setConfigPath(final String configPath) {
    this.configPath = configPath;
    return this;
  }

  public MockEventsConfiguration getMockEventsConfiguration() {
    return mockEventsConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setMockEventsConfiguration(
      final MockEventsConfiguration mockEventsConfiguration) {
    this.mockEventsConfiguration = mockEventsConfiguration;
    return this;
  }

  public boolean isSchedulerEnabled() {
    return schedulerEnabled;
  }

  public ThirdEyeCoordinatorConfiguration setSchedulerEnabled(final boolean schedulerEnabled) {
    this.schedulerEnabled = schedulerEnabled;
    return this;
  }

  public boolean isTaskDriverEnabled() {
    return taskDriverEnabled;
  }

  public ThirdEyeCoordinatorConfiguration setTaskDriverEnabled(final boolean taskDriverEnabled) {
    this.taskDriverEnabled = taskDriverEnabled;
    return this;
  }
}
