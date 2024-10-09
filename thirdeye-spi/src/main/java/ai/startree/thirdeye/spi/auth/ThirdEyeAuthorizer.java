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
import ai.startree.thirdeye.spi.api.DataSourceApi;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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

  // if the implementation does not support multiple namespace, please return a list with the value null Collections.singletonList(null)
  @NonNull List<String> listNamespaces(final ThirdEyePrincipal principal);

  /**
   * Generates a recommended datasource configuration to use in the ThirdEye instance.
   * Does not create the datasource.
   *
   * Example: an authorizer plugin could:
   * 1. set up a service account to connect to Pinot
   * 2. return a valid datasource configuration that uses the service account just created.
   *
   * @param principal the principal to generate datasource for
   * @return DataSourceApi -> Returns null if there is no recommended datasource configuration
   */
  default @Nullable DataSourceApi generateDatasourceConnection(final ThirdEyePrincipal principal) {
    return null;
  }

  interface ThirdEyeAuthorizerFactory extends
      PluginServiceFactory<ThirdEyeAuthorizer, Map<String, Object>> {}
}
