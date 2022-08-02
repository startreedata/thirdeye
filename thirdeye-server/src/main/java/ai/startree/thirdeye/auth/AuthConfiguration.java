/*
 * Copyright 2022 StarTree Inc
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

import ai.startree.thirdeye.auth.oauth.OAuthConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthConfiguration {

  private boolean enabled;

  @JsonProperty("oauth")
  private OAuthConfiguration oAuthConfig;

  public OAuthConfiguration getOAuthConfig() {
    return oAuthConfig;
  }

  public AuthConfiguration setOAuthConfig(final OAuthConfiguration oAuthConfiguration) {
    this.oAuthConfig = oAuthConfiguration;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public AuthConfiguration setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }
}
