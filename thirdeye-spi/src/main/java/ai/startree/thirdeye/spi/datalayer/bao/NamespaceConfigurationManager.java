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
package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.dto.NamespaceConfigurationDTO;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface NamespaceConfigurationManager {

  @NonNull NamespaceConfigurationDTO getNamespaceConfiguration(final String namespace);

  @NonNull NamespaceConfigurationDTO updateNamespaceConfiguration(
      NamespaceConfigurationDTO updatedNamespaceConfiguration);

  @NonNull NamespaceConfigurationDTO resetNamespaceConfiguration(final String namespace);

  Long save(final NamespaceConfigurationDTO entity);

  List<NamespaceConfigurationDTO> findAll();

  NamespaceConfigurationDTO findById(final Long id);

  int update(final NamespaceConfigurationDTO entity);

  int delete(final NamespaceConfigurationDTO entity);

  int deleteById(final Long id);

  List<NamespaceConfigurationDTO> filter(final DaoFilter daoFilter);
}

