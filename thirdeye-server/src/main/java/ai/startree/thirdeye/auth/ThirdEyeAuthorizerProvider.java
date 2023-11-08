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

import ai.startree.thirdeye.spi.auth.AccessControlFactory;
import ai.startree.thirdeye.spi.auth.AccessType;
import ai.startree.thirdeye.spi.auth.IThirdEyePrincipal;
import ai.startree.thirdeye.spi.auth.ResourceIdentifier;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer;

/**
 * AccessControlProvider serves as a mutable layer between Guice bindings and the access control
 * implementation from plugins.
 */
public class ThirdEyeAuthorizerProvider implements ThirdEyeAuthorizer {

  public final static ThirdEyeAuthorizer ALWAYS_ALLOW = (
      final IThirdEyePrincipal principal,
      final ResourceIdentifier identifiers,
      final AccessType accessType
  ) -> true;

  public final static ThirdEyeAuthorizer ALWAYS_DENY = (
      final IThirdEyePrincipal principal,
      final ResourceIdentifier identifiers,
      final AccessType accessType
  ) -> false;

  private final AccessControlConfiguration config;
  private ThirdEyeAuthorizer thirdEyeAuthorizer = null;

  public ThirdEyeAuthorizerProvider(final AccessControlConfiguration config) {
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

    if (this.thirdEyeAuthorizer != null) {
      throw new RuntimeException("Access control source can only be set once!");
    }
    this.thirdEyeAuthorizer = accessControl;
  }

  public ThirdEyeAuthorizer getAccessControl() {
    if (!config.isEnabled()) {
      return ALWAYS_ALLOW;
    }

    checkState(this.thirdEyeAuthorizer != null,
        "Access control is enabled, but no provider has been configured!");
    return this.thirdEyeAuthorizer;
  }

  public AccessControlConfiguration getConfig() {
    return config;
  }

  @Override
  public boolean authorize(final IThirdEyePrincipal principal, final ResourceIdentifier identifier,
      final AccessType accessType) {
    return getAccessControl().authorize(principal, identifier, accessType);
  }
}
