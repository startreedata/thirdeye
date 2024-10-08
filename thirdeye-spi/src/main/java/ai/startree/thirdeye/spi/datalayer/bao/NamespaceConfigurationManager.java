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

  /**
   * There is no create method. This find operation takes care of creating a default configuration
   * if necessary.
   * There is no getById because the namespace should be a unique identifier for this entity.
   */
  @NonNull
  NamespaceConfigurationDTO findByNamespace(final String namespace);

  @NonNull
  NamespaceConfigurationDTO update(final NamespaceConfigurationDTO updatedNamespaceConfiguration);

  @NonNull
  NamespaceConfigurationDTO resetByNamespace(final String namespace);

  /**
   * For internal stats use only.
   */
  List<NamespaceConfigurationDTO> findAll();

  int delete(final NamespaceConfigurationDTO entity);

  /**
   * For internal use only.
   */
  int deleteById(final Long id);

  List<NamespaceConfigurationDTO> filter(final DaoFilter daoFilter);
}

