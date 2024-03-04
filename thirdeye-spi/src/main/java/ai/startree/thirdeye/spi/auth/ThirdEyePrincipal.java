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
import java.security.Principal;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ThirdEyePrincipal extends Principal {

  @Override
  String getName();

  String getAuthToken();

  AuthenticationType getAuthenticationType();

  /**
   * null means the principal belongs to no namespace. (least-privilege model)
   * It should not have access to anything
   * See {@link ThirdEyePrincipal#isFullyNamespaced()} for backward compatibility.
   */
  @Nullable String getActiveNamespace();

  /**
   * Empty list means the principal belongs to no namespace.
   * In most cases this function should return 1 or more namespaces.
   */
  @NonNull ImmutableList<String> listNamespaces();

  /**
   * For backward compatibility with old environments and {@link ThirdEyeAuthorizer} that
   * don't implement default/active namespace.
   * Context: entities are fetched from the DB then authorization is checked
   * with {@link ThirdEyeAuthorizer#authorize}. For performance and security
   * purpose, only entities from the current namespace are fetched from the DB.
   * For backward compatibility, we need to be able to disable the filter at the DB level:
   * - a ThirdEyeAuthenticator does not manage namespaces
   * - entities in the databases are not migrated and have no namespace yet
   *
   * When isFullyNamespaced returns false, the filter at the db level is not performed.
   * In any case the final authorization is performed {@link ThirdEyeAuthorizer#authorize}
   * 
   * New implementations should return true;
   */
  boolean isFullyNamespaced(); 
}
