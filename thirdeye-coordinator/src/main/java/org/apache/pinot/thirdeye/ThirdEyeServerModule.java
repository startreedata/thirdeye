package org.apache.pinot.thirdeye;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.auth.JwtConfiguration;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeServerModule extends AbstractModule {

  private final ThirdEyeServerConfiguration configuration;
  private final DataSource dataSource;

  public ThirdEyeServerModule(final ThirdEyeServerConfiguration configuration,
      final DataSource dataSource) {
    this.configuration = configuration;
    this.dataSource = dataSource;
  }

  @Override
  protected void configure() {
    install(new ThirdEyeCoreModule(dataSource));
  }

  @Singleton
  @Provides
  public JwtConfiguration getJwtConfiguration() {
    return configuration.getAuthConfiguration().getJwtConfiguration();
  }
}
