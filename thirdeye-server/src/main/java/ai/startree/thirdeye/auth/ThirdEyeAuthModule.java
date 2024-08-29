/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.auth;

import static ai.startree.thirdeye.spi.Constants.AUTH_BASIC;
import static ai.startree.thirdeye.spi.Constants.AUTH_BEARER;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.auth.basic.BasicAuthConfiguration;
import ai.startree.thirdeye.auth.basic.BasicNamespacedCredentialAuthFilter;
import ai.startree.thirdeye.auth.basic.ThirdEyeBasicNamespacedAuthenticator;
import ai.startree.thirdeye.auth.oauth.OAuthConfiguration;
import ai.startree.thirdeye.auth.oauth.OAuthCredentialNamespacedAuthFilter;
import ai.startree.thirdeye.auth.oauth.ThirdEyeAuthenticatorDisabled;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class ThirdEyeAuthModule extends AbstractModule {

  private final AuthConfiguration config;

  public ThirdEyeAuthModule(final AuthConfiguration config) {
    this.config = config;
  }

  @Singleton
  @Provides
  public OAuthCredentialNamespacedAuthFilter<ThirdEyeServerPrincipal> getOAuthFilter(
      final AuthRegistry authRegistry,
      final OAuthConfiguration oauthConfig) {
    return new OAuthCredentialNamespacedAuthFilter.Builder<ThirdEyeServerPrincipal>()
        .setAuthenticator(authRegistry.createOAuthAuthenticator(oauthConfig))
        .setPrefix(AUTH_BEARER)
        .buildAuthFilter();
  }

  @Singleton
  @Provides
  public OAuthConfiguration getOAuthConfig() {
    return config.getOAuthConfig();
  }

  @Singleton
  @Provides
  public BasicAuthConfiguration getBasicAuthConfig(final AuthConfiguration authConfiguration) {
    return authConfiguration.getBasicAuthConfig();
  }

  @Singleton
  @Provides
  @SuppressWarnings("rawtypes")
  public AuthFilter getAuthFilter(
      final AuthConfiguration authConfig,
      @Nullable final BasicAuthConfiguration basicAuthConfig,
      @Nullable final OAuthConfiguration oauthConfig,
      final Provider<BasicNamespacedCredentialAuthFilter<ThirdEyeServerPrincipal>> basicAuthFilter,
      final Provider<OAuthCredentialNamespacedAuthFilter<ThirdEyeServerPrincipal>> oAuthFilter) {
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

  private OAuthCredentialNamespacedAuthFilter<ThirdEyeServerPrincipal> getNoAuthFilter() {
    final ThirdEyeAuthenticatorDisabled authenticator = new ThirdEyeAuthenticatorDisabled();
    return new OAuthCredentialNamespacedAuthFilter.Builder<ThirdEyeServerPrincipal>()
        .setAuthenticator(authenticator)
        .setPrefix(AUTH_BEARER)
        .buildAuthFilter();
  }

  @Singleton
  @Provides
  public BasicNamespacedCredentialAuthFilter<ThirdEyeServerPrincipal> getBasicAuthFilter(
      final ThirdEyeBasicNamespacedAuthenticator authenticator) {
    return new BasicNamespacedCredentialAuthFilter.Builder<ThirdEyeServerPrincipal>()
        .setAuthenticator(authenticator)
        .setPrefix(AUTH_BASIC)
        .buildAuthFilter();
  }
}
