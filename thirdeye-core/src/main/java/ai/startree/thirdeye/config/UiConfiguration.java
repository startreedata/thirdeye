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
package ai.startree.thirdeye.config;

public class UiConfiguration {

  private String externalUrl;
  private String clientId;

  public String getExternalUrl() {
    return externalUrl;
  }

  public UiConfiguration setExternalUrl(final String externalUrl) {
    this.externalUrl = externalUrl;
    return this;
  }

  public String getClientId() {
    return clientId;
  }

  public UiConfiguration setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }
}
