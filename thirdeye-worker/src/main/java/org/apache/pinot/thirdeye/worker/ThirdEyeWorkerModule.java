package org.apache.pinot.thirdeye.worker;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.ThirdEyeCoreModule;
import org.apache.pinot.thirdeye.anomaly.detection.trigger.utils.DataAvailabilitySchedulingConfiguration;
import org.apache.pinot.thirdeye.config.ConfigurationHolder;
import org.apache.pinot.thirdeye.config.ThirdEyeWorkerConfiguration;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeWorkerModule extends AbstractModule {

  private final DataSource dataSource;
  private final ThirdEyeWorkerConfiguration configuration;
  private final ConfigurationHolder configurationHolder;
  private final MetricRegistry metricRegistry;


  public ThirdEyeWorkerModule(final DataSource dataSource,
      final ThirdEyeWorkerConfiguration configuration,
      final MetricRegistry metricRegistry) {
    this.dataSource = dataSource;
    this.configuration = configuration;

    configurationHolder = new ConfigurationHolder(configuration.getRootDir());
    this.metricRegistry = metricRegistry;
  }

  @Override
  protected void configure() {
    install(new ThirdEyeCoreModule(dataSource, configurationHolder));

    bind(MetricRegistry.class).toInstance(metricRegistry);
    bind(DataAvailabilitySchedulingConfiguration.class)
        .toProvider(configuration::getDataAvailabilitySchedulingConfiguration)
        .in(Scopes.SINGLETON);
  }

  @Singleton
  @Provides
  public ThirdEyeWorkerConfiguration getThirdEyeWorkerConfiguration() {
    return configuration;
  }
}
