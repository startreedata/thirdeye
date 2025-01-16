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

import static ai.startree.thirdeye.ResourceUtils.badRequest;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.NamespaceConfigurationApi;
import ai.startree.thirdeye.spi.datalayer.bao.NamespaceConfigurationManager;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public class NamespaceConfigurationService {

  private final NamespaceConfigurationManager namespaceConfigurationManager;
  private final AuthorizationManager authorizationManager;

  @Inject
  public NamespaceConfigurationService(
      final NamespaceConfigurationManager namespaceConfigurationManager,
      final AuthorizationManager authorizationManager) {
    this.namespaceConfigurationManager = namespaceConfigurationManager;
    this.authorizationManager = authorizationManager;
  }

  public NamespaceConfigurationApi getNamespaceConfiguration(
      final ThirdEyeServerPrincipal principal
  ) {
    final String namespace = authorizationManager.currentNamespace(principal);
    return toApi(namespaceConfigurationManager.getNamespaceConfiguration(namespace));
  }

  public NamespaceConfigurationApi updateNamespaceConfiguration(
      final ThirdEyeServerPrincipal principal,
      final NamespaceConfigurationApi updatedNamespaceConfiguration
  ) {
    final String namespace = authorizationManager.currentNamespace(principal);
    final @NonNull NamespaceConfigurationDTO existing = namespaceConfigurationManager
        .getNamespaceConfiguration(namespace);
    final @NonNull NamespaceConfigurationDTO updated = toDto(updatedNamespaceConfiguration);

    validateUpdate(toApi(existing), updatedNamespaceConfiguration);
    authorizationManager.ensureNamespace(principal, existing);
    authorizationManager.ensureNamespace(principal, updated);
    return toApi(namespaceConfigurationManager.updateNamespaceConfiguration(
        toDto(updatedNamespaceConfiguration)));
  }

  public NamespaceConfigurationApi resetNamespaceConfiguration(
      final ThirdEyeServerPrincipal principal
  ) {
    final String namespace = authorizationManager.currentNamespace(principal);
    return toApi(namespaceConfigurationManager.resetNamespaceConfiguration(namespace));
  }

  protected NamespaceConfigurationDTO toDto(final NamespaceConfigurationApi api) {
    return ApiBeanMapper.toNamespaceConfigurationDTO(api);
  }

  protected NamespaceConfigurationApi toApi(final NamespaceConfigurationDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  protected void validateUpdate(NamespaceConfigurationApi existing,
      NamespaceConfigurationApi updated) {
    if (!Objects.equals(existing.getId(), updated.getId()) ||
        !Objects.equals(existing.getAuth().getNamespace(), updated.getAuth().getNamespace()) ||
        !Objects.equals(existing.getQuotasConfiguration(), updated.getQuotasConfiguration())) {
      throw badRequest(
          ThirdEyeStatus.ERR_NAMESPACE_CONFIGURATION_VALIDATION_FAILED,
          existing.namespace(),
          "Updating Id, auth, or quotasConfiguration is not allowed for Namespace Configuration");
    }
  }
}
