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

import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.auth.OpenIdConfigurationProvider.Factory;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthenticator.OauthThirdEyeAuthenticatorFactory;
import com.google.auto.service.AutoService;
import java.util.List;

@AutoService(Plugin.class)
public class ThirdEyeOAuthPlugin implements Plugin {

  @Override
  public Iterable<Factory> getOpenIdConfigurationProviderFactories() {
    return List.of(new DefaultOpenIdConfigurationProviderFactory());
  }

  @Override
  public Iterable<OauthThirdEyeAuthenticatorFactory> getOAuthAuthenticatorFactories() {
    return List.of(new DefaultOauthThirdEyeAuthenticatorFactory());
  }
}
