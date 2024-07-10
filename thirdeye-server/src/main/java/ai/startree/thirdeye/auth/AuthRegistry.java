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

import static ai.startree.thirdeye.spi.Constants.VANILLA_OBJECT_MAPPER;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.auth.oauth.OAuthConfiguration;
import ai.startree.thirdeye.spi.auth.AuthenticationType;
import ai.startree.thirdeye.spi.auth.OpenIdConfigurationProvider;
import ai.startree.thirdeye.spi.auth.OpenIdConfigurationProvider.Factory;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthenticator.OauthThirdEyeAuthenticatorFactory;
import com.google.inject.Singleton;
import io.dropwizard.auth.Authenticator;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class AuthRegistry {

  public static final String OAUTH_DEFAULT = "oauth-default";
  public static final String OPENID_DEFAULT = "openid-default";
  private final Map<String, OauthThirdEyeAuthenticatorFactory> oauthFactories = new HashMap<>();
  private final Map<String, OpenIdConfigurationProvider.Factory> openIdConfigurationFactories = new HashMap<>();

  @SuppressWarnings("unchecked")
  private static Map<String, Object> toMap(final OAuthConfiguration oauthConfig) {
    // TODO spyne: oauthConfig should be moved to a plugin style config
    return VANILLA_OBJECT_MAPPER.convertValue(oauthConfig, Map.class);
  }

  public void registerOAuthFactory(OauthThirdEyeAuthenticatorFactory f) {
    checkState(!oauthFactories.containsKey(f.getName()),
        "Duplicate OauthAuthenticatorFactory: " + f.getName());

    oauthFactories.put(f.getName(), f);
  }

  public void registerOpenIdConfigurationFactory(
      OpenIdConfigurationProvider.Factory f) {
    checkState(!oauthFactories.containsKey(f.getName()),
        "Duplicate OauthAuthenticatorFactory: " + f.getName());

    openIdConfigurationFactories.put(f.getName(), f);
  }

  private OauthThirdEyeAuthenticatorFactory getDefaultOAuthFactory() {
    return requireNonNull(oauthFactories.get(OAUTH_DEFAULT), "oauth plugin not loaded!");
  }

  @SuppressWarnings("unchecked")
  public Authenticator<String, ThirdEyeServerPrincipal> createOAuthAuthenticator(
      final OAuthConfiguration oauthConfig) {
    final Map<String, Object> oauthConfigMap = toMap(oauthConfig);

    final var authenticator = requireNonNull(getDefaultOAuthFactory().build(oauthConfigMap),
        "failed to build authenticator");
    return credentials -> authenticator.authenticate(credentials)
        .map(p -> new ThirdEyeServerPrincipal(p.getName(), credentials, AuthenticationType.OAUTH));
  }

  public OpenIdConfigurationProvider createDefaultOpenIdConfigurationProvider(
      final OAuthConfiguration oAuthConfiguration) {
    return requireNonNull(getDefaultOpenIdConfigurationFactory().build(toMap(oAuthConfiguration)),
        "failed to build OpenIdConfigurationProvider");
  }

  private Factory getDefaultOpenIdConfigurationFactory() {
    return requireNonNull(openIdConfigurationFactories.get(OPENID_DEFAULT),
        "OpenIdConfigurationProvider plugin not loaded!");
  }
}
