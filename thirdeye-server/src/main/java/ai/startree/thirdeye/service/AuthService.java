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

package ai.startree.thirdeye.service;

import ai.startree.thirdeye.auth.AuthRegistry;
import ai.startree.thirdeye.auth.oauth.OAuthConfiguration;
import ai.startree.thirdeye.spi.api.AuthInfoApi;
import ai.startree.thirdeye.spi.auth.OpenIdConfigurationProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.annotation.Nullable;

@Singleton
public class AuthService {

  private final AuthRegistry authRegistry;
  private final OAuthConfiguration oAuthConfiguration;
  private OpenIdConfigurationProvider openIdConfigurationProvider = null;

  @Inject
  public AuthService(final AuthRegistry authRegistry,
      @Nullable final OAuthConfiguration oAuthConfiguration) {
    this.authRegistry = authRegistry;
    this.oAuthConfiguration = oAuthConfiguration;
  }

  public AuthInfoApi getOpenIdConfiguration() {
    if (oAuthConfiguration != null) {
      if (openIdConfigurationProvider == null) {
        // TODO: fix hack. This is loaded via a plugin and is not available at startup
        openIdConfigurationProvider =
            authRegistry.createDefaultOpenIdConfigurationProvider(oAuthConfiguration);
      }
      return openIdConfigurationProvider.getOpenIdConfiguration();
    }
    /* When oauth is disabled return an empty object.
     * TODO spyne. remove. kept for legacy reasons.
     */
    return new AuthInfoApi();
  }
}
