package org.apache.pinot.thirdeye.worker;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.ThirdEyeCoreModule;
import org.apache.pinot.thirdeye.anomaly.MockEventsLoaderConfiguration;
import org.apache.pinot.thirdeye.anomaly.ThirdEyeWorkerConfiguration;
import org.apache.pinot.thirdeye.anomaly.detection.trigger.utils.DataAvailabilitySchedulingConfiguration;
import org.apache.pinot.thirdeye.anomaly.monitor.MonitorConfiguration;
import org.apache.pinot.thirdeye.config.ConfigurationHolder;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeWorkerModule extends AbstractModule {

  private final DataSource dataSource;
  private final ThirdEyeWorkerConfiguration configuration;
  private final ConfigurationHolder configurationHolder;

  public ThirdEyeWorkerModule(final DataSource dataSource,
      final ThirdEyeWorkerConfiguration configuration) {
    this.dataSource = dataSource;
    this.configuration = configuration;
    configurationHolder = new ConfigurationHolder(configuration.getRootDir());
  }

  @Override
  protected void configure() {
    install(new ThirdEyeCoreModule(dataSource, configurationHolder));

    bind(MonitorConfiguration.class)
        .toProvider(configuration::getMonitorConfiguration)
        .in(Scopes.SINGLETON);

    bind(DataAvailabilitySchedulingConfiguration.class)
        .toProvider(configuration::getDataAvailabilitySchedulingConfiguration)
        .in(Scopes.SINGLETON);

    bind(MockEventsLoaderConfiguration.class)
        .toProvider(configuration::getMockEventsLoaderConfiguration)
        .in(Scopes.SINGLETON);
  }

  @Singleton
  @Provides
  public ThirdEyeWorkerConfiguration getThirdEyeWorkerConfiguration() {
    return configuration;
  }
}
