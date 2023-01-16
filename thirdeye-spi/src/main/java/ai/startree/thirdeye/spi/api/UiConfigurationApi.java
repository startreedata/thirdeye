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
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UiConfigurationApi implements ThirdEyeApi {

    private String clientId;
    private boolean authEnabled;

    public String getClientId() {
        return clientId;
    }

    public UiConfigurationApi setClientId(final String clientId) {
        this.clientId = clientId;
        return this;
    }

  public boolean isAuthEnabled() {
    return authEnabled;
  }

  public UiConfigurationApi setAuthEnabled(final boolean authEnabled) {
    this.authEnabled = authEnabled;
    return this;
  }
}
