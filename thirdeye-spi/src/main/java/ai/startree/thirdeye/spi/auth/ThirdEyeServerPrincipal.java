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
package ai.startree.thirdeye.spi.auth;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

// pojo implementation of ServerPrincipal - TODO CYRIL change the name
public class ThirdEyeServerPrincipal implements ThirdEyePrincipal {

  private final String name;
  private final String authToken;
  private final AuthenticationType authenticationType;
  private final String activeNamespace;
  private final ImmutableList<String> namespaces;
  private final boolean isFullyNamespaced;

  public ThirdEyeServerPrincipal(final String name,
      final String authToken,
      final AuthenticationType authenticationType, final String activeNamespace,
      final List<String> namespaces, final boolean isFullyNamespaced) {
    this.name = name;
    this.authToken = authToken;
    this.authenticationType = authenticationType;
    this.activeNamespace = activeNamespace;
    this.namespaces = ImmutableList.copyOf(namespaces);
    this.isFullyNamespaced = isFullyNamespaced;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getAuthToken() {
    return authToken;
  }

  @Override
  public AuthenticationType getAuthenticationType() {
    return authenticationType;
  }

  @Override
  public String getActiveNamespace() {
    return activeNamespace;
  }

  @Override
  public @NonNull ImmutableList<String> listNamespaces() {
    return namespaces;
  }

  @Override
  public boolean isFullyNamespaced() {
    return isFullyNamespaced;
  }
}
