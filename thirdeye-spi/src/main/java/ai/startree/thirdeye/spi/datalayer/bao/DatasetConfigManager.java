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

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface DatasetConfigManager extends AbstractManager<DatasetConfigDTO> {

  /**
   * Find a dataset in a given namespace. If namespace is null, look in the undefined namespace.
   * If the dataset is not found in the provided namespace, look in the undefined namespace, and
   * if a matching dataset is found in the undefined namespace, log a warning and return it.
   * Returns null if the dataset is not found in the namespace and the unset namespace.
   *
   * Used to maintain backward compatibility with existing instances.
   * To have a strict search that does not fallback to the undefined namespace, use
   * {@link AbstractManager#findUniqueByNameAndNamespace}.
   */
  @Nullable DatasetConfigDTO findByDatasetAndNamespaceOrUnsetNamespace(final String dataset, final String namespace);

  List<DatasetConfigDTO> findActive();
}
