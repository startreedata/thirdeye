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

import ai.startree.thirdeye.spi.PluginServiceFactory;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ThirdEyeAuthorizer {

  /**
   * Authorizes the given principal to access the given resource with the given access type.
   *
   * @param principal the principal to authorize
   * @param identifier the resource to authorize access to
   * @param accessType the type of access to authorize
   * @return true if the principal is authorized to access the resource with the given access type
   */
  boolean authorize(
      ThirdEyePrincipal principal,
      ResourceIdentifier identifier,
      AccessType accessType
  );

  // if the implementation does not support multiple namespace, please return null (the null namespace is supported for the moment) 
  default @NonNull List<String> listNamespaces(final ThirdEyePrincipal principal) {
    throw new UnsupportedOperationException("listNamespaces is not implemented. Cannot enforce namespace in all entities.");
  }

  interface ThirdEyeAuthorizerFactory extends
      PluginServiceFactory<ThirdEyeAuthorizer, Map<String, Object>> {}
}
