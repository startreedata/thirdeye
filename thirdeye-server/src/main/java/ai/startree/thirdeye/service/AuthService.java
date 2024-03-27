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
package ai.startree.thirdeye.service;

import ai.startree.thirdeye.auth.AuthRegistry;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.oauth.OAuthConfiguration;
import ai.startree.thirdeye.spi.api.AuthInfoApi;
import ai.startree.thirdeye.spi.auth.OpenIdConfigurationProvider;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.annotation.Nullable;

@Singleton
public class AuthService {

  private final AuthRegistry authRegistry;
  private final OAuthConfiguration oAuthConfiguration;
  private OpenIdConfigurationProvider openIdConfigurationProvider = null;
  private final AuthorizationManager authorizationManager;

  @Inject
  public AuthService(final AuthRegistry authRegistry,
      @Nullable final OAuthConfiguration oAuthConfiguration,
      final AuthorizationManager authorizationManager) {
    this.authRegistry = authRegistry;
    this.oAuthConfiguration = oAuthConfiguration;
    this.authorizationManager = authorizationManager;
  }

  public AuthInfoApi getOpenIdConfiguration(@Nullable final ThirdEyePrincipal principal) {
    // it is fine if the principal is null - as of today this service does not require an authenticated user
    // TODO CYRIL here we keep the convention of Service class requiring an identity in all public methods (it is enforced by tests)
    //   we should introduce a NOT_AUTHENTICATED identity for such cases 
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
