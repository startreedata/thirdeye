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
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface AbstractManager<E extends AbstractDTO> {

  Long save(E entity);

  int update(E entity);

  int update(List<E> entities);

  E findById(Long id);

  // returns null if the entity is not found 
  // intended for entities that enforce uniqueness of name, namespace 
  @Nullable E findUniqueByNameAndNamespace(final @NonNull String name, final @Nullable String namespace);

  List<E> findByIds(List<Long> id);

  int delete(E entity);

  int deleteById(Long id);

  int deleteByIds(List<Long> ids);

  int deleteByPredicate(Predicate predicate);

  int deleteRecordsOlderThanDays(int days);

  List<E> findAll();

  List<E> findByPredicate(Predicate predicate);

  List<E> filter(DaoFilter daoFilter);

  int update(E entity, Predicate predicate);

  /**
   * Count how many entities are there in the table
   *
   * @return the number of total entities
   */
  default long count() {
    throw new UnsupportedOperationException();
  }

  /**
   * Count how many entities are there in the table matching the predicate
   *
   * @return the number of total entities matching the predicate
   */
  default long count(final Predicate predicate) {
    throw new UnsupportedOperationException();
  }

  /**
   * Register metrics that perform regular calls to the persistence database.
   * These metrics should not be registered by all components of ThirdEye. 
   * This would result in a linear scaling of database queries when components are scaled linearly. 
   * It is recommended to only have the scheduler component register such metrics.
   */
  void registerDatabaseMetrics();
}
