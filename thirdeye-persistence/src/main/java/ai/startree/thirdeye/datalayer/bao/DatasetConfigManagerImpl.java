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
package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AbstractManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DatasetConfigManagerImpl extends AbstractManagerImpl<DatasetConfigDTO> implements
    DatasetConfigManager {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetConfigManagerImpl.class);

  @Inject
  public DatasetConfigManagerImpl(GenericPojoDao genericPojoDao) {
    super(DatasetConfigDTO.class, genericPojoDao);
  }

  @Override
  public DatasetConfigDTO findUniqueByNameAndNamespace(final @NonNull String name,
      final @Nullable String namespace) {
    final List<DatasetConfigDTO> list = findByPredicate(
        Predicate.AND(
            Predicate.EQ("dataset", name),
            Predicate.OR(
                Predicate.EQ("namespace", namespace),
                // existing entities are not migrated automatically so they can have their namespace column to null in the index table, even if they do belong to a namespace 
                //  todo cyril authz - prepare migration scripts - or some logic to ensure all entities are eventually migrated
                Predicate.EQ("namespace", null)
            )
        )
    )
        // we still need to perform in-app filtering until all entities namespace are migrated in db - see above
        .stream().filter(e -> Objects.equals(e.namespace(), namespace)).toList();
    if (list.isEmpty()) {
      return null;
    } else if (list.size() == 1) {
      return list.iterator().next();
    } else {
      throw new IllegalStateException(String.format(
          "Found multiple entities with name %s and namespace %s. This should not happen. Please reach out to support.",
          name, namespace));
    }
  }

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
  @Override
  public @Nullable DatasetConfigDTO findByNameAndNamespaceOrUnsetNamespace(final String dataset,
      final @Nullable String namespace) {
    DatasetConfigDTO datasetConfigDTO = findUniqueByNameAndNamespace(dataset, namespace);
    if (datasetConfigDTO == null && namespace != null) {
      // attempt to fallback to the undefined namespace - TODO cyril authz this behaviour will be removed or conditioned on requireNamespace=false
      datasetConfigDTO = findUniqueByNameAndNamespace(dataset, null);
      if (datasetConfigDTO != null) {
        LOG.warn(
            "Could not find dataset {} in namespace {}, but found a dataset with this name with an unset namespace. " 
                + "Using this dataset. This behaviour will change. Please migrate your dataset to a namespace.",
            dataset, namespace);
      }
    }
    return datasetConfigDTO;
  }

  @Override
  public List<DatasetConfigDTO> findActive() {
    Predicate activePredicate = Predicate.EQ("active", true);
    return findByPredicate(activePredicate);
  }
}
