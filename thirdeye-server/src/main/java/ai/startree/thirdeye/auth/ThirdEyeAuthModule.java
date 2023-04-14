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

package ai.startree.thirdeye.auth;

import static ai.startree.thirdeye.spi.Constants.AUTH_BASIC;
import static ai.startree.thirdeye.spi.Constants.AUTH_BEARER;
import static ai.startree.thirdeye.spi.Constants.OAUTH_ISSUER;
import static ai.startree.thirdeye.spi.Constants.OAUTH_JWKS_URI;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.auth.basic.BasicAuthConfiguration;
import ai.startree.thirdeye.auth.basic.ThirdEyeBasicAuthenticator;
import ai.startree.thirdeye.auth.oauth.CachedJWSKeySelector;
import ai.startree.thirdeye.auth.oauth.OAuthConfiguration;
import ai.startree.thirdeye.auth.oauth.OidcContext;
import ai.startree.thirdeye.auth.oauth.OidcJWTProcessor;
import ai.startree.thirdeye.auth.oauth.OpenIdInfoService;
import ai.startree.thirdeye.auth.oauth.ThirdEyeOAuthAuthenticator;
import ai.startree.thirdeye.spi.api.AuthInfoApi;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class ThirdEyeAuthModule extends AbstractModule {

  private final AuthConfiguration config;

  public ThirdEyeAuthModule(final AuthConfiguration config) {
    this.config = config;
  }

  @Override
  protected void configure() {
    bind(new TypeLiteral<DefaultJWTProcessor<OidcContext>>() {})
        .to(OidcJWTProcessor.class);
    bind(new TypeLiteral<JWSKeySelector<OidcContext>>() {})
        .to(CachedJWSKeySelector.class);
  }

  @Singleton
  @Provides
  public JWTClaimsSetVerifier<OidcContext> createJWTClaimsSetVerifier(
      final OidcContext context) {
    return new DefaultJWTClaimsVerifier<>(
        context.getExactMatchClaimsSet(),
        context.getRequiredClaims());
  }

  @Provides
  @Singleton
  public OidcContext getOidcContext(final OAuthConfiguration oauthConfig,
      final OpenIdInfoService openIdInfoService) {
    final AuthInfoApi info = openIdInfoService.getAuthInfoApi();

    optional(info.getOpenidConfiguration())
        .map(oidcConfig -> oidcConfig.get(OAUTH_ISSUER))
        .map(Object::toString)
        .ifPresent(iss -> oauthConfig.getExactMatch().put("iss", iss));

    optional(info.getOpenidConfiguration())
        .map(oidcConfig -> oidcConfig.get(OAUTH_JWKS_URI))
        .map(Object::toString)
        .ifPresent(oauthConfig::setKeysUrl);

    return new OidcContext(oauthConfig);
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
  public BasicCredentialAuthFilter<ThirdEyePrincipal> getBasicAuthFilter(
      final ThirdEyeBasicAuthenticator authenticator) {
    return new BasicCredentialAuthFilter.Builder<ThirdEyePrincipal>()
        .setAuthenticator(authenticator)
        .setPrefix(AUTH_BASIC)
        .buildAuthFilter();
  }
}
