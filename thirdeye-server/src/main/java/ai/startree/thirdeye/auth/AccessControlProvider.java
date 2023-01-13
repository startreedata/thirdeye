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

import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.accessControl.AccessControl;
import ai.startree.thirdeye.spi.accessControl.AccessControlConfiguration;
import ai.startree.thirdeye.spi.accessControl.AccessType;
import ai.startree.thirdeye.spi.accessControl.ResourceIdentifier;

/**
 * AccessControlProvider serves as a mutable layer between Guice bindings and the access control
 * implementation from plugins.
 */
public class AccessControlProvider implements AccessControl {

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

  private final AccessControlConfiguration config;
  private AccessControl accessControl = null;

  public AccessControlProvider(AccessControlConfiguration config) {
    this.config = config;
  }

  public void setAccessControl(AccessControl accessControl) {
    if (this.accessControl != null) {
      throw new RuntimeException("Access control source can only be set once!");
    }
    this.accessControl = accessControl;
  }

  public AccessControl getAccessControl() {
    if (!config.enabled) {
      return alwaysAllow;
    }

    checkState(this.accessControl != null,
        "Access control is enabled, but no provider has been configured!");
    return this.accessControl;
  }

  public AccessControlConfiguration getConfig() {
    return config;
  }

  @Override
  public boolean hasAccess(final String token, final ResourceIdentifier identifier,
      final AccessType accessType) {
    return getAccessControl().hasAccess(token, identifier, accessType);
  }
}
