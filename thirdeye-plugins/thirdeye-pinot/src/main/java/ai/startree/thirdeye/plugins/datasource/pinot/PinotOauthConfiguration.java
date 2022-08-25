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

package ai.startree.thirdeye.plugins.datasource.pinot;

import java.util.Objects;

public class PinotOauthConfiguration {

  private boolean enabled = false;
  private String tokenFilePath;

  public boolean isEnabled() {
    return enabled;
  }

  public PinotOauthConfiguration setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public String getTokenFilePath() {
    return tokenFilePath;
  }

  public PinotOauthConfiguration setTokenFilePath(final String tokenFilePath) {
    this.tokenFilePath = tokenFilePath;
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
    final PinotOauthConfiguration that = (PinotOauthConfiguration) o;
    return enabled == that.enabled && Objects.equals(tokenFilePath, that.tokenFilePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enabled, tokenFilePath);
  }
}
