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
package ai.startree.thirdeye.spi.auth;

import ai.startree.thirdeye.spi.PluginServiceFactory;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import java.util.Map;
import java.util.Optional;

public interface ThirdEyeAuthenticator<CredentialsT> {

  /**
   * Authenticates the given credentials. Returns an empty optional if the credentials could not be
   * authenticated.
   *
   * @param credentials the credentials to authenticate
   * @return an optional containing the authenticated principal
   * @throws ThirdEyeException thrown if there is an error authenticating the credentials
   *     typically using a {@link ThirdEyeStatus#ERR_UNAUTHENTICATED} status
   */
  Optional<ThirdEyePrincipal> authenticate(CredentialsT credentials) throws ThirdEyeException;

  interface OauthThirdEyeAuthenticatorFactory extends
      PluginServiceFactory<ThirdEyeAuthenticator<String>, Map> {
  }
}
