package org.apache.pinot.thirdeye;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.auth.AuthConfiguration;
import org.apache.pinot.thirdeye.auth.JwtConfiguration;
import org.apache.pinot.thirdeye.config.ConfigurationHolder;
import org.apache.pinot.thirdeye.config.ThirdEyeWorkerConfiguration;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeCoordinatorModule extends AbstractModule {

  private final ThirdEyeCoordinatorConfiguration configuration;
  private final ConfigurationHolder configurationHolder;
  private final DataSource dataSource;

  public ThirdEyeCoordinatorModule(final ThirdEyeCoordinatorConfiguration configuration,
      final DataSource dataSource) {
    this.configuration = configuration;
    this.dataSource = dataSource;
    configurationHolder = new ConfigurationHolder(configuration.getConfigPath());
  }

  @Override
  protected void configure() {
    install(new ThirdEyeCoreModule(dataSource, configurationHolder));
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

  /**
   * Hack. This makes the worker configuration available for the coordinator to use.
   * There are lot of scheduler config files which are kept inside the worker config. This
   * needs to be migrated out of the worker.
   *
   * @return the worker configuration assuming the default filename in the config dir.
   */
  @Singleton
  @Provides
  public ThirdEyeWorkerConfiguration getThirdEyeWorkerConfiguration() {
    return configurationHolder.createConfigurationInstance(ThirdEyeWorkerConfiguration.class);
  }
}
