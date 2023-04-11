/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye;

import ai.startree.thirdeye.auth.AccessControlProvider;
import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.auth.ThirdEyeAuthModule;
import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.detectionpipeline.ThirdEyeDetectionPipelineModule;
import ai.startree.thirdeye.notification.ThirdEyeNotificationModule;
import ai.startree.thirdeye.scheduler.ThirdEyeSchedulerModule;
import ai.startree.thirdeye.scheduler.events.MockEventsConfiguration;
import ai.startree.thirdeye.spi.accessControl.AccessControl;
import ai.startree.thirdeye.worker.ThirdEyeWorkerModule;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeServerModule extends AbstractModule {

  private final ThirdEyeServerConfiguration configuration;
  private final DataSource dataSource;
  private final MetricRegistry metricRegistry;
  private final AccessControlProvider accessControlProvider;

  public ThirdEyeServerModule(
      final ThirdEyeServerConfiguration configuration,
      final DataSource dataSource,
      final MetricRegistry metricRegistry) {
    this.configuration = configuration;
    this.dataSource = dataSource;
    this.metricRegistry = metricRegistry;

    this.accessControlProvider = new AccessControlProvider(
        configuration.getAccessControlConfiguration());
  }

  @Override
  protected void configure() {
    // authConfiguration is expected to always exist
    install(new ThirdEyeAuthModule(configuration.getAuthConfiguration()));

    install(new ThirdEyeCoreModule(dataSource,
        configuration.getCacheConfig(),
        configuration.getRcaConfiguration(),
        configuration.getUiConfiguration(),
        configuration.getTimeConfiguration()));
    install(new ThirdEyeNotificationModule(configuration.getNotificationConfiguration()));
    install(new ThirdEyeDetectionPipelineModule(configuration.getDetectionPipelineConfiguration()));
    install(new ThirdEyeWorkerModule(configuration.getTaskDriverConfiguration()));
    install(new ThirdEyeSchedulerModule(configuration.getSchedulerConfiguration()));

    bind(AuthConfiguration.class).toInstance(configuration.getAuthConfiguration());
    bind(MetricRegistry.class).toInstance(metricRegistry);
    bind(ThirdEyeServerConfiguration.class).toInstance(configuration);
  }

  @Singleton
  @Provides
  public MockEventsConfiguration getMockEventsLoaderConfiguration() {
    return configuration.getMockEventsConfiguration();
  }

  @Singleton
  @Provides
  public AccessControlProvider getAccessControlProvider() {
    return this.accessControlProvider;
  }

  @Singleton
  @Provides
  public AccessControl getAccessControl() {
    return this.accessControlProvider;
  }
}
