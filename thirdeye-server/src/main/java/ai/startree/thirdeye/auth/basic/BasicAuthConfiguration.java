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
package ai.startree.thirdeye.auth.basic;

import java.util.List;

public class BasicAuthConfiguration {

  private boolean enabled = false;
  private List<UserCredentialConfiguration> users;

  public boolean isEnabled() {
    return enabled;
  }

  public BasicAuthConfiguration setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public List<UserCredentialConfiguration> getUsers() {
    return users;
  }

  public BasicAuthConfiguration setUsers(final List<UserCredentialConfiguration> users) {
    this.users = users;
    return this;
  }
}
