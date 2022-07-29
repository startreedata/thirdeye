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
package ai.startree.thirdeye.auth.basic;

import com.google.common.base.Objects;

public class User {

  private String username;
  private String password;

  public String getUsername() {
    return username;
  }

  public User setUsername(final String username) {
    this.username = username;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public User setPassword(final String password) {
    this.password = password;
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final User user = (User) o;
    return Objects.equal(getUsername(), user.getUsername())
        && Objects.equal(getPassword(), user.getPassword());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getUsername(), getPassword());
  }
}
