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
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.auth.ThirdEyeAuthenticatorDisabled;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.auth.basic.BasicAuthConfiguration;
import ai.startree.thirdeye.auth.basic.ThirdEyeBasicAuthenticator;
import ai.startree.thirdeye.auth.oauth.OAuthConfiguration;
import ai.startree.thirdeye.auth.oauth.OAuthManager;
import ai.startree.thirdeye.auth.oauth.ThirdEyeOAuthAuthenticator;
import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.notification.ThirdEyeNotificationModule;
import ai.startree.thirdeye.scheduler.ThirdEyeSchedulerModule;
import ai.startree.thirdeye.scheduler.events.MockEventsConfiguration;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.worker.ThirdEyeWorkerModule;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import java.util.ArrayList;
import java.util.List;
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
  public OAuthConfiguration getOAuthConfig(
      AuthConfiguration authConfiguration) {
    return authConfiguration.getOAuthConfig();
  }

  @Singleton
  @Provides
  public BasicAuthConfiguration getBasicAuthConfig(
      AuthConfiguration authConfiguration) {
    return authConfiguration.getBasicAuthConfig();
  }

  @Singleton
  @Provides
  public AuthFilter getAuthFilters(final OAuthManager oAuthManager) {
    AuthConfiguration config = configuration.getAuthConfiguration();
    List<AuthFilter> filters = new ArrayList<>();
    if(config.isEnabled()) {
      optional(getBasicAuthFilter()).ifPresent(filters::add);
      optional(getOAuthFilter(oAuthManager)).ifPresent(filters::add);
    } else {
      filters.add(getNoAuthFilter());
    }
    if(filters.isEmpty()) {
      throw new ThirdEyeException(ThirdEyeStatus.ERR_AUTH_FILTERS_DISABLED);
    }
    return new ChainedAuthFilter<>(filters);
  }

  private OAuthCredentialAuthFilter<ThirdEyePrincipal> getNoAuthFilter() {
    final ThirdEyeAuthenticatorDisabled authenticator = new ThirdEyeAuthenticatorDisabled();
    return new OAuthCredentialAuthFilter.Builder<ThirdEyePrincipal>()
        .setAuthenticator(authenticator)
        .setPrefix(AUTH_BEARER)
        .buildAuthFilter();
  }

  private OAuthCredentialAuthFilter<ThirdEyePrincipal> getOAuthFilter(final OAuthManager authManager) {
    OAuthConfiguration config = configuration.getAuthConfiguration().getOAuthConfig();
    if (config != null && config.isEnabled()) {
      final ThirdEyeOAuthAuthenticator authenticator = new ThirdEyeOAuthAuthenticator(authManager);
      return new OAuthCredentialAuthFilter.Builder<ThirdEyePrincipal>()
          .setAuthenticator(authenticator)
          .setPrefix(AUTH_BEARER)
          .buildAuthFilter();
    } else {
      return null;
    }
  }

  private BasicCredentialAuthFilter<ThirdEyePrincipal> getBasicAuthFilter() {
    BasicAuthConfiguration config = configuration.getAuthConfiguration().getBasicAuthConfig();
    if (config != null && config.isEnabled()) {
      final ThirdEyeBasicAuthenticator authenticator = new ThirdEyeBasicAuthenticator(config);
      return new BasicCredentialAuthFilter.Builder<ThirdEyePrincipal>()
          .setAuthenticator(authenticator)
          .setPrefix(AUTH_BASIC)
          .buildAuthFilter();
    } else {
      return null;
    }
  }
}
