/*
 * Copyright 2022 StarTree Inc
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

import static ai.startree.thirdeye.spi.Constants.AUTH_BASIC;
import static ai.startree.thirdeye.spi.Constants.AUTH_BEARER;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.auth.AccessControlModule;
import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.auth.ThirdEyeAuthenticatorDisabled;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.auth.basic.BasicAuthConfiguration;
import ai.startree.thirdeye.auth.basic.ThirdEyeBasicAuthenticator;
import ai.startree.thirdeye.auth.oauth.OAuthConfiguration;
import ai.startree.thirdeye.auth.oauth.ThirdEyeOAuthAuthenticator;
import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.detectionpipeline.ThirdEyeDetectionPipelineModule;
import ai.startree.thirdeye.notification.ThirdEyeNotificationModule;
import ai.startree.thirdeye.scheduler.ThirdEyeSchedulerModule;
import ai.startree.thirdeye.scheduler.events.MockEventsConfiguration;
import ai.startree.thirdeye.worker.ThirdEyeWorkerModule;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeServerModule extends AbstractModule {

  private final ThirdEyeServerConfiguration configuration;
  private final DataSource dataSource;
  private final MetricRegistry metricRegistry;

  public ThirdEyeServerModule(
      final ThirdEyeServerConfiguration configuration,
      final DataSource dataSource,
      final MetricRegistry metricRegistry) {
    this.configuration = configuration;
    this.dataSource = dataSource;
    this.metricRegistry = metricRegistry;
  }

  @Override
  protected void configure() {
    install(new ThirdEyeCoreModule(dataSource,
        configuration.getCacheConfig(),
        configuration.getRcaConfiguration(),
        configuration.getUiConfiguration(),
        configuration.getTimeConfiguration()));
    install(new ThirdEyeNotificationModule(configuration.getNotificationConfiguration()));
    install(new ThirdEyeDetectionPipelineModule(configuration.getDetectionPipelineConfiguration()));
    install(new ThirdEyeWorkerModule(configuration.getTaskDriverConfiguration()));
    install(new ThirdEyeSchedulerModule(configuration.getSchedulerConfiguration()));
    install(new AccessControlModule());

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
  public OAuthConfiguration getOAuthConfig(
      final AuthConfiguration authConfiguration) {
    return authConfiguration.getOAuthConfig();
  }

  @Singleton
  @Provides
  public BasicAuthConfiguration getBasicAuthConfig(
      final AuthConfiguration authConfiguration) {
    return authConfiguration.getBasicAuthConfig();
  }

  @Singleton
  @Provides
  @SuppressWarnings("rawtypes")
  public AuthFilter getAuthFilters(
      final AuthConfiguration authConfig,
      @Nullable final BasicAuthConfiguration basicAuthConfig,
      @Nullable final OAuthConfiguration oauthConfig,
      final Provider<BasicCredentialAuthFilter<ThirdEyePrincipal>> basicAuthFilter,
      final Provider<OAuthCredentialAuthFilter<ThirdEyePrincipal>> oAuthFilter) {
    final List<AuthFilter> filters = new ArrayList<>();
    if (authConfig.isEnabled()) {
      if (oauthConfig != null && oauthConfig.isEnabled()) {
        filters.add(oAuthFilter.get());
      }
      if (basicAuthConfig != null && basicAuthConfig.isEnabled()) {
        filters.add(basicAuthFilter.get());
      }
    } else {
      filters.add(getNoAuthFilter());
    }
    checkState(!filters.isEmpty(), "If auth is enabled, it must have at least 1 filter enabled.");
    return new ChainedAuthFilter<>(filters);
  }

  private OAuthCredentialAuthFilter<ThirdEyePrincipal> getNoAuthFilter() {
    final ThirdEyeAuthenticatorDisabled authenticator = new ThirdEyeAuthenticatorDisabled();
    return new OAuthCredentialAuthFilter.Builder<ThirdEyePrincipal>()
        .setAuthenticator(authenticator)
        .setPrefix(AUTH_BEARER)
        .buildAuthFilter();
  }

  @Singleton
  @Provides
  public OAuthCredentialAuthFilter<ThirdEyePrincipal> getOAuthFilter(
      final ThirdEyeOAuthAuthenticator authenticator) {
    return new OAuthCredentialAuthFilter.Builder<ThirdEyePrincipal>()
        .setAuthenticator(authenticator)
        .setPrefix(AUTH_BEARER)
        .buildAuthFilter();
  }

  @Singleton
  @Provides
  public BasicCredentialAuthFilter<ThirdEyePrincipal> getBasicAuthFilter(
      final ThirdEyeBasicAuthenticator authenticator) {
    return new BasicCredentialAuthFilter.Builder<ThirdEyePrincipal>()
        .setAuthenticator(authenticator)
        .setPrefix(AUTH_BASIC)
        .buildAuthFilter();
  }
}
