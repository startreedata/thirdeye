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
package ai.startree.thirdeye.plugins.oauth;

import ai.startree.thirdeye.spi.auth.OpenIdConfigurationProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import java.util.HashSet;

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
      final OAuthConfiguration config) {
    final JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
    config.getExactMatch().forEach(builder::claim);
    var exactMatchClaimsSet = builder.build();

    return new DefaultJWTClaimsVerifier<>(exactMatchClaimsSet, new HashSet<>(config.getRequired()));
  }

  @Provides
  @Singleton
  public OidcContext getOidcContext() {
    return new OidcContext();
  }
}
