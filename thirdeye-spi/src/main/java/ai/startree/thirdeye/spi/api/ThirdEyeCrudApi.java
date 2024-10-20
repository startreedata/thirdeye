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
package ai.startree.thirdeye.spi.api;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface ThirdEyeCrudApi<T extends ThirdEyeCrudApi<T>> extends ThirdEyeApi {

  Long getId();

  T setId(Long id);

  AuthorizationConfigurationApi getAuth();

  T setAuth(final AuthorizationConfigurationApi auth);

  default @Nullable String namespace() {
    return getAuth() != null ? getAuth().getNamespace() : null;
  }
}
