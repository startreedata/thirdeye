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
package ai.startree.thirdeye.datalayer.dao;

import static ai.startree.thirdeye.datalayer.dao.SubEntities.BEAN_INDEX_MAP;
import static ai.startree.thirdeye.datalayer.mapper.DtoIndexMapper.toAbstractIndexEntity;
import static ai.startree.thirdeye.datalayer.mapper.GenericJsonEntityDtoMapper.toDto;
import static ai.startree.thirdeye.datalayer.mapper.GenericJsonEntityDtoMapper.toGenericJsonEntity;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NEGATIVE_LIMIT_VALUE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_NEGATIVE_OFFSET_VALUE;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OFFSET_WITHOUT_LIMIT;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datalayer.DatabaseClient;
import ai.startree.thirdeye.datalayer.DatabaseOrm;
import ai.startree.thirdeye.datalayer.entity.AbstractIndexEntity;
import ai.startree.thirdeye.datalayer.entity.GenericJsonEntity;
import ai.startree.thirdeye.datalayer.entity.SubEntityType;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

;

@Singleton
public class GenericPojoDao {

  private static final Logger LOG = LoggerFactory.getLogger(GenericPojoDao.class);

  private final DatabaseOrm databaseOrm;
  private final DatabaseClient databaseClient;

  @Inject
  public GenericPojoDao(final DatabaseOrm databaseOrm,
      final DatabaseClient databaseClient) {
    this.databaseOrm = databaseOrm;
    this.databaseClient = databaseClient;

    checkState(BEAN_INDEX_MAP.size() == SubEntities.BEAN_TYPE_MAP.size(),
        "Entity Metadata is inconsistent!");
  }

  private static void validate(final DaoFilter filter) {
    optional(filter.getLimit()).ifPresent(limit -> {
      Preconditions.checkArgument(limit >= 0, ERR_NEGATIVE_LIMIT_VALUE.getMessage());
    });
    optional(filter.getOffset()).ifPresent(offset -> {
      Preconditions.checkArgument(filter.getLimit() != null, ERR_OFFSET_WITHOUT_LIMIT.getMessage());
      Preconditions.checkArgument(offset >= 0, ERR_NEGATIVE_OFFSET_VALUE.getMessage());
    });
  }

  public Set<Class<? extends AbstractDTO>> getAllBeanClasses() {
    return BEAN_INDEX_MAP.keySet();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public List<String> getIndexedColumns(final Class beanClass) {
    final Class<? extends AbstractIndexEntity> indexEntityClass = BEAN_INDEX_MAP.get(beanClass);
    final Set<Field> allFields = ReflectionUtils.getAllFields(indexEntityClass);
    final List<String> indexedColumnNames = new ArrayList<>();
    for (final Field field : allFields) {
      indexedColumnNames.add(field.getName());
    }
    return indexedColumnNames;
  }

  // return the id of the created entity. return null in case of error
  public <E extends AbstractDTO> @Nullable Long create(final E pojo) {
    requireNonNull(pojo, "entity is null");
    checkArgument(pojo.getId() == null, "id must be null for create flow.");

    /* Populate createTime before DB insert if not present already */
    if (pojo.getCreateTime() == null) {
      pojo.setCreateTime(new Timestamp(System.currentTimeMillis()));
    }
    try {
      return databaseClient.executeTransaction((connection) -> {
        final GenericJsonEntity e = toGenericJsonEntity(pojo);
        final Class<? extends AbstractIndexEntity> indexClass = BEAN_INDEX_MAP.get(pojo.getClass());
        final Long generatedKey = databaseOrm.save(e, connection);
        pojo.setId(generatedKey);
        if (indexClass != null) {
          final AbstractIndexEntity abstractIndexEntity = toAbstractIndexEntity(
              pojo,
              indexClass,
              e.getJsonVal());
          abstractIndexEntity.setVersion(1);
          abstractIndexEntity.setCreateTime(pojo.getCreateTime());
          return databaseOrm.save(abstractIndexEntity, connection);
        } else {
          return pojo.getId();
        }
      });
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL redesign - maybe all operations in this calls should throw exceptions instead of catching them? what's the use case of returning null ? the operation failed, and then some custom logic is required to find the null - see https://github.com/startreedata/thirdeye/blob/51c7d9decf707176153822b1a90c6162f072066a/thirdeye-server/src/main/java/ai/startree/thirdeye/service/CrudService.java#L110
      return null;
    }
  }

  public <E extends AbstractDTO> int update(final E pojo) {
    return update(List.of(pojo));
  }

  /**
   * Update the list of pojos in transaction mode. Every transaction contains MAX_BATCH_SIZE of
   * entries. By default,
   * this method update the entries in one transaction. If any entries cause an exception, this
   * method will
   * update the entries one-by-one (i.e., in separated transactions) and skip the one that causes
   * exceptions.
   *
   * @param pojos the pojo to be updated, whose ID cannot be null; otherwise, it will be
   *     ignored.
   * @return the number of rows that are affected.
   */
  public <E extends AbstractDTO> int update(final List<E> pojos) {
    if (CollectionUtils.isEmpty(pojos)) {
      return 0;
    }
    int updateCounter = 0;
    for (final E pojo : pojos) {
      try {
        updateCounter += update(pojo, null);
      } catch (final Exception e) {
        LOG.error("Could not update entity : {}", pojo, e);
      }
    }
    return updateCounter;
  }

  // return 1 if the update is successful. else return 0.
  public <E extends AbstractDTO> int update(final E pojo, final Predicate predicate) {
    checkNotNull(pojo.getId(), "An id is required to update the entity: %s", pojo);

    /* Update updateTime before DB update. Restore if update fails */
    final Timestamp lastUpdateTime = pojo.getUpdateTime();
    pojo.setUpdateTime(new Timestamp(System.currentTimeMillis()));

    try {
      final GenericJsonEntity genericJsonEntity = toGenericJsonEntity(pojo);
      final Class<? extends AbstractIndexEntity> indexClass = BEAN_INDEX_MAP.get(pojo.getClass());
      return databaseClient.executeTransaction((connection) -> {
        Integer ret = databaseOrm.update(genericJsonEntity, predicate, connection);
        //update indexes
        if (ret == 1) {
          if (indexClass != null) {
            final AbstractIndexEntity abstractIndexEntity = toAbstractIndexEntity(pojo,
                indexClass,
                genericJsonEntity.getJsonVal());
            //updates all columns in the index table by default
            ret = databaseOrm.update(abstractIndexEntity, null, connection);
          }
        }
        if (ret > 1) {
          throw new ThirdEyeException(ThirdEyeStatus.ERR_UNKNOWN, "Too many rows updated");
        }
        return ret;
      });
    } catch (final Exception e) {
      pojo.setUpdateTime(lastUpdateTime);
      LOG.error("Could not update entity : {}", pojo, e);
      return 0;
    }
  }

  // return a list of entity. if the operation fails, return an empty list 
  public <E extends AbstractDTO> List<E> list(final Class<E> beanClass, final long limit,
      final long offset) {
    try {
      final Predicate predicate = Predicate.EQ("type", SubEntities.getType(beanClass));
      final List<GenericJsonEntity> entities = databaseClient.executeTransaction(
          (connection) -> databaseOrm.findAll(predicate,
              limit,
              offset,
              GenericJsonEntity.class,
              connection));
      final List<E> result = new ArrayList<>();
      if (entities != null) {
        for (final GenericJsonEntity entity : entities) {
          final E e = toDto(entity, beanClass);
          result.add(e);
        }
      }
      return result;
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL - throw exception instead of returning an empty list ?
      return emptyList();
    }
  }

  public <E extends AbstractDTO> long count(final Class<E> beanClass) {
    return count(null, beanClass);
  }

  // return the count of entity matching the predicate. If the operation fails, return -1. 
  public <E extends AbstractDTO> long count(final @Nullable Predicate predicate, final Class<E> beanClass) {
    final Class<? extends AbstractIndexEntity> indexClass = BEAN_INDEX_MAP.get(beanClass);
    try {
      return databaseClient.executeTransaction(
          (connection) -> databaseOrm.count(predicate, indexClass, connection));
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      return -1L;
    }
  }

  // return the entity with a given id. If the entity is not found, is not of the input pojoClass type, or the operation fails, return null 
  public <E extends AbstractDTO> E get(final Long id, final Class<E> pojoClass) {
    try {
      final GenericJsonEntity genericJsonEntity = databaseClient.executeTransaction(
          (connection) -> databaseOrm.find(id, GenericJsonEntity.class, connection));
      if (genericJsonEntity == null) {
        return null;
      }
      final String type = SubEntities.getType(pojoClass);
      /* Object with id just not match type. Hence, return null */
      if (!type.equals(genericJsonEntity.getType())) {
        return null;
      }
      return toDto(genericJsonEntity, pojoClass);
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ? 
      return null;
    }
  }

  // return the entity with a given id. If not found or the operation fails, return null
  public AbstractDTO getRaw(final Long id) {
    try {
      final GenericJsonEntity genericJsonEntity = databaseClient.executeTransaction(
          (connection) -> databaseOrm.find(id, GenericJsonEntity.class, connection));
      if (genericJsonEntity != null) {
        return toDto(genericJsonEntity,
            SubEntities.BEAN_TYPE_MAP.asMultimap().inverse().get(
                SubEntityType.valueOf(genericJsonEntity.getType())).asList().getFirst());
      } else {
        return null;
      }
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return null;
    }
  }

  public <E extends AbstractDTO> List<E> get(final List<Long> idList, final Class<E> pojoClass) {
    try {
      return fetchEntities(pojoClass, Predicate.IN("id", idList.toArray()));
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return emptyList();
    }
  }

  public <E extends AbstractDTO> List<E> getAll(final Class<E> pojoClass) {
    try {
      return fetchEntities(pojoClass, Predicate.EQ("type", SubEntities.getType(pojoClass)));
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return emptyList();
    }
  }

  /**
   * Use this method when you want to fetchEntities out a subset of the entities based on predicates,
   * limits, offsets, etc.
   * If you wish to get all the entities then please use {@link #getAll} as the fetchEntities method does a
   * two-step operation to get the entities whereas {@link #getAll} gets the entities in a single
   * operation.
   *
   * @param daoFilter required filters to fetchEntities the result.
   */
  @SuppressWarnings("unchecked")
  public <E extends AbstractDTO> List<E> get(final DaoFilter daoFilter) {
    final List<Long> ids = fetchIds(daoFilter);
    if (ids.isEmpty()) {
      return emptyList();
    }
    final Class<? extends AbstractDTO> beanClass = daoFilter.getBeanClass();
    return (List<E>) get(ids, beanClass);
  }

  private <E extends AbstractDTO> List<E> fetchEntities(final Class<E> pojoClass,
      final Predicate predicate)
      throws Exception {
    final List<GenericJsonEntity> entities = databaseClient.executeTransaction(
        (connection) -> databaseOrm.findAll(predicate,
            null,
            null,
            GenericJsonEntity.class,
            connection));
    final List<E> results = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(entities)) {
      for (final GenericJsonEntity entity : entities) {
        final E e = toDto(entity, pojoClass);
        results.add(e);
      }
    }
    return results;
  }

  private List<Long> fetchIds(final DaoFilter daoFilter) {
    //apply the predicates and fetch the primary key ids
    final Class<? extends AbstractIndexEntity> indexClass = BEAN_INDEX_MAP.get(
        daoFilter.getBeanClass());
    validate(daoFilter);
    try {
      //find the matching ids
      final List<? extends AbstractIndexEntity> indexEntities = databaseClient.executeTransaction(
          (connection) -> databaseOrm.findAll(daoFilter.getPredicate(),
              daoFilter.getLimit(),
              daoFilter.getOffset(),
              indexClass,
              connection));
      final List<Long> idsToReturn = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(indexEntities)) {
        for (final AbstractIndexEntity entity : indexEntities) {
          idsToReturn.add(entity.getBaseId());
        }
      }
      return idsToReturn;
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return emptyList();
    }
  }

  // delete the entity with the given id. Returns 1 if the deletion is successful, else return 0.
  public <E extends AbstractDTO> int delete(final Long id, final Class<E> pojoClass) {
    return delete(List.of(id), pojoClass);
  }

  // delete the entity with the given id. Returns 1 if the deletion is successful, else return 0.
  public <E extends AbstractDTO> int delete(final List<Long> idsToDelete,
      final Class<E> pojoClass) {
    final Class<? extends AbstractIndexEntity> indexEntityClass = BEAN_INDEX_MAP.get(pojoClass);
    try {
      return databaseClient.executeTransaction((connection) -> {
        // delete entry from base table
        databaseOrm.delete(
            Predicate.IN(databaseOrm.getIdColumnName(GenericJsonEntity.class),
                idsToDelete.toArray()),
            GenericJsonEntity.class,
            connection);
        // delete entry from index table
        return databaseOrm.delete(
            Predicate.IN(databaseOrm.getIdColumnName(indexEntityClass), idsToDelete.toArray()),
            indexEntityClass,
            connection);
      });
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return 0;
    }
  }

  public <E extends AbstractDTO> int deleteByPredicate(final Predicate predicate,
      final Class<E> pojoClass) {
    final List<Long> idsToDelete = fetchIds(
        new DaoFilter().setPredicate(predicate).setBeanClass(pojoClass));
    return delete(idsToDelete, pojoClass);
  }

  /**
   * Use this method when you want to fetch entities out a subset of the entities based on predicates,
   * limits, offsets, etc.
   *
   * This method is an optimized version of get(final DaoFilter daoFilter)
   * where we make a single combined query of the form
   *
   * select generic_json_entity.* from generic_json_entity
   * JOIN (select base_id from index_class where CONDITIONS)
   * subquery ON generic_json_entity.id = subquery.base_id
   *
   * @param daoFilter required filters to fetch the result.
   */
  public <E extends AbstractDTO> List<E> getV2(final DaoFilter daoFilter) {
    final Class<? extends AbstractIndexEntity> indexClass = BEAN_INDEX_MAP.get(
        daoFilter.getBeanClass());
    validate(daoFilter);

    final String matchingIdsQuery = databaseOrm.generateMatchingIdsQuery(
        daoFilter.getPredicate(),
        daoFilter.getLimit(),
        daoFilter.getOffset(),
        indexClass);
    final String parameterizedSQL = String.format("""
            JOIN (
                %s
            ) subquery ON generic_json_entity.%s = subquery.%s
            """,
        matchingIdsQuery,
        databaseOrm.getIdColumnSQLName(GenericJsonEntity.class),
        databaseOrm.getIdColumnSQLName(indexClass));

    try {
      final List<GenericJsonEntity> entities = databaseClient.executeTransaction(
          (connection) -> databaseOrm.runSQL(
              parameterizedSQL,
              Collections.emptyMap(),
              GenericJsonEntity.class,
              connection));
      final List<E> results = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(entities)) {
        for (final GenericJsonEntity entity : entities) {
          final E e = toDto(entity, (Class<E>) daoFilter.getBeanClass());
          results.add(e);
        }
      }
      return results;
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO ANSHUL design - surface exception ?
      return Collections.emptyList();
    }
  }
}
