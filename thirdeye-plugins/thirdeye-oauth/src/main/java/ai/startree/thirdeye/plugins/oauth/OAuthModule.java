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

package ai.startree.thirdeye.plugins.oauth;

import static ai.startree.thirdeye.spi.Constants.OAUTH_ISSUER;
import static ai.startree.thirdeye.spi.Constants.OAUTH_JWKS_URI;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.AuthInfoApi;
import ai.startree.thirdeye.spi.auth.OpenIdConfigurationProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;

public class OAuthModule extends AbstractModule {

  private final OAuthConfiguration oAuthConfiguration;

  public OAuthModule(final OAuthConfiguration oAuthConfiguration) {
    this.oAuthConfiguration = oAuthConfiguration;
  }

  @Override
  protected void configure() {
    bind(OAuthConfiguration.class).toInstance(oAuthConfiguration);
    bind(OpenIdConfigurationProvider.class)
        .to(OpenIdConfigurationProviderImpl.class)
        .in(Scopes.SINGLETON);
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
      final OpenIdConfigurationProviderImpl openIdConfigurationProviderImpl) {
    final AuthInfoApi info = openIdConfigurationProviderImpl.getOpenIdConfiguration();

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
}
