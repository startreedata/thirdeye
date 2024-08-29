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
package ai.startree.thirdeye.auth;

import static com.google.common.base.Preconditions.checkState;

import ai.startree.thirdeye.spi.auth.AccessType;
import ai.startree.thirdeye.spi.auth.ResourceIdentifier;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * AccessControlProvider serves as a mutable layer between Guice bindings and the access control
 * implementation from plugins.
 */
public class ThirdEyeAuthorizerProvider implements ThirdEyeAuthorizer {

  public final ThirdEyeAuthorizer alwaysAllowAuthorizer;

  public final static ThirdEyeAuthorizer ALWAYS_DENY = new AlwaysDenyAuthorizer();
  
  private final static List<String> LIST_OF_NULL = Collections.singletonList(null);

  private final AccessControlConfiguration config;
  private ThirdEyeAuthorizer thirdEyeAuthorizer = null;

  public ThirdEyeAuthorizerProvider(final AccessControlConfiguration config) {
    this.config = config;
    alwaysAllowAuthorizer = new AlwaysAllowAuthorizer(config.getStaticNameToNamespaces());
  }

  public void addAccessControlFactory(final ThirdEyeAuthorizerFactory f) {
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

  @VisibleForTesting
  public ThirdEyeAuthorizer getAccessControl() {
    if (!config.isEnabled()) {
      return alwaysAllowAuthorizer;
    }

    checkState(this.thirdEyeAuthorizer != null,
        "Access control is enabled, but no provider has been configured!");
    return this.thirdEyeAuthorizer;
  }

  public AccessControlConfiguration getConfig() {
    return config;
  }

  @Override
  public boolean authorize(final ThirdEyePrincipal principal, final ResourceIdentifier identifier,
      final AccessType accessType) {
    return getAccessControl().authorize(principal, identifier, accessType);
  }

  @Override
  public @NonNull List<String> listNamespaces(final ThirdEyePrincipal principal) {
    return getAccessControl().listNamespaces(principal);
  }

  @VisibleForTesting
  public static class AlwaysAllowAuthorizer implements ThirdEyeAuthorizer {

    private final Map<String, List<String>> nameToNamespaces;

    public AlwaysAllowAuthorizer(final @Nullable Map<String, List<String>> nameToNamespaces) {
      if (nameToNamespaces != null) {
        for (final String e : nameToNamespaces.keySet()) {
          if (e.contains("null")) {
            throw new IllegalArgumentException("Illegal namespace name null (the string). It is forbidden to avoid confusion with the null (not set) value.");
          }
        }
      }
      this.nameToNamespaces = nameToNamespaces;
    }

    @Override
    public boolean authorize(final ThirdEyePrincipal principal, final ResourceIdentifier identifier,
        final AccessType accessType) {
      return listNamespaces(principal).contains(identifier.getNamespace());
    }

    @Override
    public @NonNull List<String> listNamespaces(final ThirdEyePrincipal principal) {
      if (nameToNamespaces != null) {
        if (nameToNamespaces.containsKey(principal.getName())) {
          return nameToNamespaces.get(principal.getName());
        }
      }
      return LIST_OF_NULL;
    }
  }

  private static class AlwaysDenyAuthorizer implements ThirdEyeAuthorizer {

    @Override
    public boolean authorize(final ThirdEyePrincipal principal, final ResourceIdentifier identifier,
        final AccessType accessType) {
      return false;
    }

    @Override
    public @NonNull List<String> listNamespaces(final ThirdEyePrincipal principal) {
      return List.of();
    }
  }
}
