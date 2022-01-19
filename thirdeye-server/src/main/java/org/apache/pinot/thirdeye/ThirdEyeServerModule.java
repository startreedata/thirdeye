package org.apache.pinot.thirdeye;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.events.MockEventsConfiguration;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeServerModule extends AbstractModule {

  private final ThirdEyeServerConfiguration configuration;
  private final DataSource dataSource;
  private final MetricRegistry metricRegistry;

  public ThirdEyeServerModule(final ThirdEyeServerConfiguration configuration,
    final DataSource dataSource, final MetricRegistry metricRegistry) {
    this.configuration = configuration;
    this.dataSource = dataSource;
    this.metricRegistry = metricRegistry;
  }

  @Override
  protected void configure() {
    install(new ThirdEyeCoreModule(dataSource, configuration));

    bind(MetricRegistry.class).toInstance(metricRegistry);
    bind(ThirdEyeServerConfiguration.class).toInstance(configuration);
  }

  @Singleton
  @Provides
  public MockEventsConfiguration getMockEventsLoaderConfiguration() {
    return configuration.getMockEventsConfiguration();
  }
}
