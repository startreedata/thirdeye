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

public class AuthorizationConfiguration {
  
  // long term this should always be true - need to migrate current user to this new namespace design first
  private boolean requireNamespace = false;

  public boolean isRequireNamespace() {
    return requireNamespace;
  }

  public AuthorizationConfiguration setRequireNamespace(final boolean requireNamespace) {
    this.requireNamespace = requireNamespace;
    return this;
  }
}
