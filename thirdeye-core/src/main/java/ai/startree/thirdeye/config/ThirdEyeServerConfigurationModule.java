/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.config;

import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.auth.OAuthConfiguration;
import ai.startree.thirdeye.datasource.AutoOnboardConfiguration;
import ai.startree.thirdeye.detection.anomaly.monitor.MonitorConfiguration;
import ai.startree.thirdeye.detection.cache.CacheConfig;
import ai.startree.thirdeye.events.HolidayEventsLoaderConfiguration;
import ai.startree.thirdeye.rootcause.impl.RCAConfiguration;
import ai.startree.thirdeye.scheduler.ThirdEyeSchedulerConfiguration;
import ai.startree.thirdeye.task.TaskDriverConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

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
