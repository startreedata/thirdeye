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

import static ai.startree.thirdeye.spi.Constants.OAUTH_ISSUER;
import static ai.startree.thirdeye.spi.Constants.OAUTH_JWKS_URI;
import static ai.startree.thirdeye.spi.Constants.VANILLA_OBJECT_MAPPER;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.AuthInfoApi;
import ai.startree.thirdeye.spi.auth.OpenIdConfigurationProvider;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthenticator;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthenticator.AuthTokenAndNamespace;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthenticator.OauthThirdEyeAuthenticatorFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Map;

public class DefaultOauthThirdEyeAuthenticatorFactory implements OauthThirdEyeAuthenticatorFactory {

  @Override
  public String getName() {
    return "oauth-default";
  }

  @Override
  public ThirdEyeAuthenticator<AuthTokenAndNamespace> build(final Map configMap) {
    final OAuthConfiguration oAuthConfiguration = VANILLA_OBJECT_MAPPER.convertValue(configMap,
        OAuthConfiguration.class);
    final Injector injector = Guice.createInjector(new OAuthModule(oAuthConfiguration));

    /* TODO spyne fix anti-pattern. oauth config should not be mutated and it definitely should not
     *            be here */
    final OpenIdConfigurationProvider instance = injector.getInstance(OpenIdConfigurationProvider.class);
    final AuthInfoApi info = instance.getOpenIdConfiguration();
    optional(info.getOpenidConfiguration())
        .map(oidcConfig -> oidcConfig.get(OAUTH_ISSUER))
        .map(Object::toString)
        .ifPresent(iss -> oAuthConfiguration.getExactMatch().put("iss", iss));

    optional(info.getOpenidConfiguration())
        .map(oidcConfig -> oidcConfig.get(OAUTH_JWKS_URI))
        .map(Object::toString)
        .ifPresent(oAuthConfiguration::setKeysUrl);
    if (oAuthConfiguration.getCache() == null) {
      oAuthConfiguration.setCache(new OauthCacheConfiguration());
    }

    return injector.getInstance(ThirdEyeOAuthThirdEyeAuthenticator.class);
  }
}
