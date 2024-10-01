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
package ai.startree.thirdeye.spi.api;

public class NamespaceConfigurationApi implements ThirdEyeCrudApi<NamespaceConfigurationApi> {

  private Long id;
  private AuthorizationConfigurationApi auth;

  private TimeConfigurationApi timeConfiguration;

  public TimeConfigurationApi getTimeConfiguration() {
    return timeConfiguration;
  }

  public NamespaceConfigurationApi setTimeConfiguration(
      final TimeConfigurationApi timeConfiguration) {
    this.timeConfiguration = timeConfiguration;
    return this;
  }

  public Long getId() {
    return id;
  }

  public NamespaceConfigurationApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public AuthorizationConfigurationApi getAuth() {
    return auth;
  }

  public NamespaceConfigurationApi setAuth(final AuthorizationConfigurationApi auth) {
    this.auth = auth;
    return this;
  }
}
