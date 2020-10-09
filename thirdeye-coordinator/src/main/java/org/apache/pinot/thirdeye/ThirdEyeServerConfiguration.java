package org.apache.pinot.thirdeye;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.apache.pinot.thirdeye.auth.AuthConfiguration;
import org.apache.pinot.thirdeye.datalayer.util.PersistenceConfig.DatabaseConfiguration;

public class ThirdEyeServerConfiguration extends Configuration {

  @JsonProperty("auth")
  private AuthConfiguration authConfiguration;

  @JsonProperty("database")
  private DatabaseConfiguration databaseConfiguration;

  @JsonProperty("swagger")
  private SwaggerBundleConfiguration swaggerBundleConfiguration;

  public AuthConfiguration getAuthConfiguration() {
    return authConfiguration;
  }

  public ThirdEyeServerConfiguration setAuthConfiguration(
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

  public ThirdEyeServerConfiguration setDatabaseConfiguration(
      final DatabaseConfiguration databaseConfiguration) {
    this.databaseConfiguration = databaseConfiguration;
    return this;
  }
}
