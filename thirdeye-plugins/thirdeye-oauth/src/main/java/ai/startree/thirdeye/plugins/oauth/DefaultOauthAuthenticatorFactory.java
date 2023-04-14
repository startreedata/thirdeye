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

import ai.startree.thirdeye.spi.auth.Authenticator;
import ai.startree.thirdeye.spi.auth.Authenticator.OauthAuthenticatorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.security.Principal;
import java.util.Map;

public class DefaultOauthAuthenticatorFactory implements OauthAuthenticatorFactory {

  @Override
  public String getName() {
    return "oauth-default";
  }

  @Override
  public Authenticator<String, Principal> build(final Map configMap) {
    final OAuthConfiguration oAuthConfiguration = new ObjectMapper().convertValue(configMap,
        OAuthConfiguration.class);
    final Injector injector = Guice.createInjector(new OAuthModule(oAuthConfiguration));

    return injector.getInstance(ThirdEyeOAuthAuthenticator.class);
  }
}
