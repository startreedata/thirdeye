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
package ai.startree.thirdeye.service;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.spi.api.WorkspaceApi;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class NamespaceService {

  // kept to respect architecture test - maybe the listNamespaces 
  private final AuthorizationManager authorizationManager;
  private final ThirdEyeAuthorizer thirdEyeAuthorizer;

  @Inject
  public NamespaceService(
      final AuthorizationManager authorizationManager,
      final ThirdEyeAuthorizer thirdEyeAuthorizer) {
    this.authorizationManager = authorizationManager;
    this.thirdEyeAuthorizer = thirdEyeAuthorizer;
  }
  
  // workspace is the public facing name - namespace is the internal word. They mean the same thing.
  public List<WorkspaceApi> listNamespaces(final ThirdEyeServerPrincipal principal) {
    final List<String> namespaces = thirdEyeAuthorizer.listNamespaces(principal);
    //todo authz throw 403 if the list is empty? 
    return namespaces.stream().map(e -> new WorkspaceApi().setId(e)).toList();
  }
}
