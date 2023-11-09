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
package ai.startree.thirdeye.auth;

import ai.startree.thirdeye.spi.auth.AuthenticationType;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;

public class ThirdEyeServerPrincipal implements ThirdEyePrincipal {

  private final String name;
  private final String authToken;
  private final AuthenticationType authenticationType;

  public ThirdEyeServerPrincipal(final String name,
      final String authToken,
      final AuthenticationType authenticationType) {
    this.name = name;
    this.authToken = authToken;
    this.authenticationType = authenticationType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getAuthToken() {
    return authToken;
  }

  @Override
  public AuthenticationType getAuthenticationType() {
    return authenticationType;
  }
}
