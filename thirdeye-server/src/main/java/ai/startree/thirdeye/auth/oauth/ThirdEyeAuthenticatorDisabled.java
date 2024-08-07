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
package ai.startree.thirdeye.auth.oauth;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.spi.auth.AuthenticationType;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthenticator.AuthTokenAndNamespace;
import io.dropwizard.auth.Authenticator;
import java.util.Optional;

public class ThirdEyeAuthenticatorDisabled implements
    Authenticator<AuthTokenAndNamespace, ThirdEyeServerPrincipal> {

  @Override
  public Optional<ThirdEyeServerPrincipal> authenticate(final AuthTokenAndNamespace authTokenAndNamespace) {
    return optional(new ThirdEyeServerPrincipal(authTokenAndNamespace.authToken(), "", AuthenticationType.DISABLED, authTokenAndNamespace.namespace()));
  }
}
