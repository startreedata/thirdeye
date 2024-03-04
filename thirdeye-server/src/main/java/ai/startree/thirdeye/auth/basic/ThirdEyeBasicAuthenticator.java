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
package ai.startree.thirdeye.auth.basic;

import ai.startree.thirdeye.spi.auth.AuthenticationType;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.auth.ThirdEyeServerPrincipal;
import com.google.inject.Inject;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

public class ThirdEyeBasicAuthenticator implements
    Authenticator<BasicCredentials, ThirdEyePrincipal> {

  private final BasicAuthConfiguration configuration;

  @Inject
  public ThirdEyeBasicAuthenticator(@Nullable final BasicAuthConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public Optional<ThirdEyePrincipal> authenticate(final BasicCredentials basicCredentials) {
    final UserCredentialConfiguration user = new UserCredentialConfiguration()
        .setUsername(basicCredentials.getUsername())
        .setPassword(basicCredentials.getPassword());
    if (configuration.getUsers().contains(user)) {
      return Optional.of(new ThirdEyeServerPrincipal(basicCredentials.getUsername(),
          "",
          AuthenticationType.BASIC_AUTH,
          // FIXME CYRIL - next iteration  - add support for namespace in basic auth
          null, List.of(), false));
    }
    return Optional.empty();
  }
}
