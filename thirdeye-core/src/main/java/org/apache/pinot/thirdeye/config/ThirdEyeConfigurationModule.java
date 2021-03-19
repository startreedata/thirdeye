package org.apache.pinot.thirdeye.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.anomaly.monitor.MonitorConfiguration;
import org.apache.pinot.thirdeye.auto.onboard.AutoOnboardConfiguration;

public class ThirdEyeConfigurationModule extends AbstractModule {

  private final ConfigurationHolder configurationHolder;

  public ThirdEyeConfigurationModule(final ConfigurationHolder configurationHolder) {
    this.configurationHolder = configurationHolder;
  }

  @Override
  protected void configure() {
    bind(ConfigurationHolder.class).toInstance(configurationHolder);

    // Create bindings for all declared configurations
    //noinspection unchecked
    configurationHolder
        .getConfigClassMap()
        .keySet()
        .forEach(c -> bind(c).toInstance(configurationHolder.createConfigurationInstance(c)));
  }

  @Singleton
  @Provides
  public HolidayEventsLoaderConfiguration getHolidayEventsLoaderConfiguration(
      ThirdEyeSchedulerConfiguration configuration) {
    return configuration.getHolidayEventsLoaderConfiguration();
  }

  @Singleton
  @Provides
  public AutoOnboardConfiguration getAutoOnboardConfiguration(
      ThirdEyeSchedulerConfiguration configuration) {
    return configuration.getAutoOnboardConfiguration();
  }

  @Singleton
  @Provides
  public MonitorConfiguration getMonitorConfiguration(
      ThirdEyeSchedulerConfiguration schedulerConfiguration) {
    return schedulerConfiguration.getMonitorConfiguration();
  }
}
