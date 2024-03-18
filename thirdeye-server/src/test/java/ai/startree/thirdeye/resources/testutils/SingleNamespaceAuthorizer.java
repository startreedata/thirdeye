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
package ai.startree.thirdeye.resources.testutils;

import ai.startree.thirdeye.spi.auth.AccessType;
import ai.startree.thirdeye.spi.auth.ResourceIdentifier;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SingleNamespaceAuthorizer implements ThirdEyeAuthorizer {

  private final String namespace;
  private final AccessType accessType;

  private SingleNamespaceAuthorizer(final String namespace,
      @Nullable final AccessType accessType) {
    this.namespace = namespace;
    this.accessType = accessType;
  }

  public static SingleNamespaceAuthorizer of(final String namespace) {
    return new SingleNamespaceAuthorizer(namespace, null);
  }

  public static SingleNamespaceAuthorizer of(final String namespace,
      @NonNull final AccessType accessType) {
    return new SingleNamespaceAuthorizer(namespace, accessType);
  }

  @Override
  public boolean authorize(final ThirdEyePrincipal principal,
      final ResourceIdentifier identifier,
      final AccessType accessType) {
    return identifier.getNamespace().equals(namespace) && (this.accessType == null
        || accessType.equals(this.accessType));
  }

  @Override
  public @NonNull List<String> listNamespaces(final ThirdEyePrincipal principal) {
    return List.of();
  }
}
