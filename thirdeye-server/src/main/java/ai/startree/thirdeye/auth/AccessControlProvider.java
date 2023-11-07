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

import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.auth.AccessControl;
import ai.startree.thirdeye.spi.auth.AccessControlFactory;
import ai.startree.thirdeye.spi.auth.AccessType;
import ai.startree.thirdeye.spi.auth.ResourceIdentifier;

/**
 * AccessControlProvider serves as a mutable layer between Guice bindings and the access control
 * implementation from plugins.
 */
public class AccessControlProvider implements AccessControl {

  public final static AccessControl ALWAYS_ALLOW = (
      final String token,
      final ResourceIdentifier identifiers,
      final AccessType accessType
  ) -> true;

  public final static AccessControl ALWAYS_DENY = (
      final String token,
      final ResourceIdentifier identifiers,
      final AccessType accessType
  ) -> false;

  private final AccessControlConfiguration config;
  private AccessControl accessControl = null;

  public AccessControlProvider(final AccessControlConfiguration config) {
    this.config = config;
  }

  public void addAccessControlFactory(final AccessControlFactory f) {
    // No lazy-loading here. Immediately build the access control handler.
    if (!config.isEnabled()) {
      return;
    }

    final var accessControl = f.build(config.getPlugins().get(f.getName()));
    if (accessControl == null) {
      return;
    }

    if (this.accessControl != null) {
      throw new RuntimeException("Access control source can only be set once!");
    }
    this.accessControl = accessControl;
  }

  public AccessControl getAccessControl() {
    if (!config.isEnabled()) {
      return ALWAYS_ALLOW;
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
