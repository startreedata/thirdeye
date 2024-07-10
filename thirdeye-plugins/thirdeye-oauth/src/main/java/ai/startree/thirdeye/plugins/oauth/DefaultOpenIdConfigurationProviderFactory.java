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

import static ai.startree.thirdeye.spi.Constants.VANILLA_OBJECT_MAPPER;

import ai.startree.thirdeye.spi.auth.OpenIdConfigurationProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Map;

public class DefaultOpenIdConfigurationProviderFactory implements
    OpenIdConfigurationProvider.Factory {

  @Override
  public String getName() {
    return "openid-default";
  }

  @Override
  public OpenIdConfigurationProvider build(final Map configMap) {
    final OAuthConfiguration oAuthConfiguration = VANILLA_OBJECT_MAPPER.convertValue(configMap,
        OAuthConfiguration.class);
    final Injector injector = Guice.createInjector(new OAuthModule(oAuthConfiguration));

    return injector.getInstance(OpenIdConfigurationProvider.class);
  }
}
