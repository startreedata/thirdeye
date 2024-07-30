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
package ai.startree.thirdeye.plugins.oauth;

import ai.startree.thirdeye.spi.auth.AuthenticationType;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ThirdEyePrincipal implements ai.startree.thirdeye.spi.auth.ThirdEyePrincipal {

  private final String name;
  private @Nullable String namespace;

  public ThirdEyePrincipal(final String name, final @Nullable String namespace) {
    this.name = name;
    this.namespace = namespace;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getAuthToken() {
    return null;
  }

  @Override
  public AuthenticationType getAuthenticationType() {
    return null;
  }

  @Override
  public @Nullable String getNamespace() {
    return namespace;
  }
}
