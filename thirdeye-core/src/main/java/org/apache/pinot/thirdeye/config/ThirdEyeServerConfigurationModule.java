package org.apache.pinot.thirdeye.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.auth.AuthConfiguration;
import org.apache.pinot.thirdeye.auth.OAuthConfiguration;
import org.apache.pinot.thirdeye.datasource.AutoOnboardConfiguration;
import org.apache.pinot.thirdeye.detection.anomaly.monitor.MonitorConfiguration;
import org.apache.pinot.thirdeye.detection.cache.CacheConfig;
import org.apache.pinot.thirdeye.events.HolidayEventsLoaderConfiguration;
import org.apache.pinot.thirdeye.rootcause.impl.RCAConfiguration;
import org.apache.pinot.thirdeye.scheduler.ThirdEyeSchedulerConfiguration;
import org.apache.pinot.thirdeye.task.TaskDriverConfiguration;

public class ThirdEyeServerConfigurationModule extends AbstractModule {

  private final ThirdEyeServerConfiguration configuration;

  public ThirdEyeServerConfigurationModule(
      final ThirdEyeServerConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    bind(CacheConfig.class)
        .toProvider(configuration::getCacheConfig)
        .in(Scopes.SINGLETON);

    bind(RCAConfiguration.class)
        .toProvider(configuration::getRcaConfiguration)
        .in(Scopes.SINGLETON);

    bind(ThirdEyeSchedulerConfiguration.class)
        .toProvider(configuration::getSchedulerConfiguration)
        .in(Scopes.SINGLETON);

    bind(TaskDriverConfiguration.class)
        .toProvider(configuration::getTaskDriverConfiguration)
        .in(Scopes.SINGLETON);

    bind(UiConfiguration.class)
        .toProvider(configuration::getUiConfiguration)
        .in(Scopes.SINGLETON);

    bind(AuthConfiguration.class)
      .toProvider(configuration::getAuthConfiguration)
      .in(Scopes.SINGLETON);
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

  @Singleton
  @Provides
  public OAuthConfiguration getOAuthConfig(
    AuthConfiguration authConfiguration) {
    return authConfiguration.getOAuthConfig();
  }
}
