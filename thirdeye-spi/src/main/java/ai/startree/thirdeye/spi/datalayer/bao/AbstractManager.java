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
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface AbstractManager<E extends AbstractDTO> {

  Long save(E entity);

  int update(E entity);

  int update(List<E> entities);

  E findById(Long id);

  /**
   * Find entity by name.
   * The name is intended to be unique, but it is not enforced by the database for all entities.
   * Therefore, this method returns a list of entities.
   *
   * @param name
   * @return
   */
  @Deprecated // use findUniqueByNameAndNamespace instead
  List<E> findByName(String name);
  
  // returns null if the entity is not found or not unique // TODO CYRIL differentiate behaviour when not unique 
  default @Nullable E findUniqueByNameAndNamespace(final @NonNull String name, final @Nullable String namespace) {
    final List<E> list =  findByName(name)
        // TODO CYRIL authz - filter in the sql read, not in app - remove findByName and don't provide a default
        .stream().filter(d -> Objects.equals(d.namespace(), namespace)).toList();
    if (list.size() == 1) {
      return list.iterator().next();
    }
    return null;
  }

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
}
