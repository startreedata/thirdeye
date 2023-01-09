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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.accessControl.AccessControl;
import ai.startree.thirdeye.spi.accessControl.AccessType;
import ai.startree.thirdeye.spi.accessControl.ResourceIdentifier;
import com.google.inject.Provider;

public class AccessControlProvider implements Provider<AccessControl> {

  public final static AccessControl alwaysAllow = (
      final String token,
      final ResourceIdentifier identifiers,
      final AccessType accessType
  ) -> true;

  public final static AccessControl alwaysDeny = (
      final String token,
      final ResourceIdentifier identifiers,
      final AccessType accessType
  ) -> false;

  private AccessControl accessControl = null;

  public void set(AccessControl accessControl) {
    if (this.accessControl != null) {
      throw new RuntimeException("Access control source can only be set once!");
    }
    this.accessControl = accessControl;
  }

  public AccessControl get() {
    return optional(this.accessControl).orElse(alwaysAllow);
  }
}
