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
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.NamespaceConfigurationApi;
import ai.startree.thirdeye.spi.datalayer.bao.NamespaceConfigurationManager;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import com.google.common.collect.ImmutableMap;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NamespaceConfigurationService extends CrudService<NamespaceConfigurationApi,
    NamespaceConfigurationDTO> {

  @Inject
  public NamespaceConfigurationService(
      final NamespaceConfigurationManager namespaceConfigurationManager,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, namespaceConfigurationManager, ImmutableMap.of());
  }

  @Override
  protected NamespaceConfigurationDTO toDto(final NamespaceConfigurationApi api) {
    return ApiBeanMapper.toNamespaceConfigurationDTO(api);
  }

  @Override
  protected NamespaceConfigurationApi toApi(final NamespaceConfigurationDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}
