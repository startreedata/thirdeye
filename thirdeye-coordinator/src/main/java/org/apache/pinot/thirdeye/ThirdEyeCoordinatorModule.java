package org.apache.pinot.thirdeye;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.auth.AuthConfiguration;
import org.apache.pinot.thirdeye.auth.JwtConfiguration;
import org.apache.pinot.thirdeye.resources.RootCauseResource;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeCoordinatorModule extends AbstractModule {

  private final ThirdEyeCoordinatorConfiguration configuration;
  private final DataSource dataSource;

  public ThirdEyeCoordinatorModule(final ThirdEyeCoordinatorConfiguration configuration,
      final DataSource dataSource) {
    this.configuration = configuration;
    this.dataSource = dataSource;
  }

  @Override
  protected void configure() {
    install(new ThirdEyeCoreModule(dataSource));

    bind(RootCauseResource.class)
        .toProvider(new RootCauseResourceProvider(configuration));
  }

  @Singleton
  @Provides
  public AuthConfiguration getAuthConfiguration() {
    return configuration.getAuthConfiguration();
  }

  @Singleton
  @Provides
  public JwtConfiguration getJwtConfiguration(AuthConfiguration authConfiguration) {
    return authConfiguration.getJwtConfiguration();
  }
}
