package org.apache.pinot.thirdeye;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.config.AuthConfiguration;
import org.apache.pinot.thirdeye.config.JwtConfiguration;
import org.apache.pinot.thirdeye.config.MockEventsConfiguration;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeCoordinatorModule extends AbstractModule {

  private final ThirdEyeCoordinatorConfiguration configuration;
  private final DataSource dataSource;
  private final MetricRegistry metricRegistry;

  public ThirdEyeCoordinatorModule(final ThirdEyeCoordinatorConfiguration configuration,
      final DataSource dataSource, final MetricRegistry metricRegistry) {
    this.configuration = configuration;
    this.dataSource = dataSource;
    this.metricRegistry = metricRegistry;
  }

  @Override
  protected void configure() {
    install(new ThirdEyeCoreModule(dataSource, configuration));

    bind(MetricRegistry.class).toInstance(metricRegistry);
    bind(ThirdEyeCoordinatorConfiguration.class).toInstance(configuration);
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

  @Singleton
  @Provides
  public MockEventsConfiguration getMockEventsLoaderConfiguration() {
    return configuration.getMockEventsConfiguration();
  }
}
