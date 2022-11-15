/*
 * Copyright 2022 StarTree Inc
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

import static ai.startree.thirdeye.datalayer.mapper.DtoIndexMapper.toAbstractIndexEntity;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datalayer.DatabaseService;
import ai.startree.thirdeye.datalayer.DatabaseTransactionService;
import ai.startree.thirdeye.datalayer.entity.AbstractEntity;
import ai.startree.thirdeye.datalayer.entity.AbstractIndexEntity;
import ai.startree.thirdeye.datalayer.entity.GenericJsonEntity;
import ai.startree.thirdeye.datalayer.entity.SubEntityType;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GenericPojoDao {

  private static final Logger LOG = LoggerFactory.getLogger(GenericPojoDao.class);
  private static final boolean IS_DEBUG = LOG.isDebugEnabled();
  private static final ObjectMapper OBJECT_MAPPER = ThirdEyeSerialization.getObjectMapper();

  private final DatabaseService databaseService;
  private final DatabaseTransactionService transactionService;

  @Inject
  public GenericPojoDao(final DatabaseService databaseService,
      final DatabaseTransactionService transactionService) {
    this.databaseService = databaseService;
    this.transactionService = transactionService;

    checkState(SubEntities.BEAN_INDEX_MAP.size() == SubEntities.BEAN_TYPE_MAP.size(),
        "Entity Metadata is inconsistent!");
  }

  public Set<Class<? extends AbstractDTO>> getAllBeanClasses() {
    return SubEntities.BEAN_INDEX_MAP.keySet();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public List<String> getIndexedColumns(final Class beanClass) {
    final Class<? extends AbstractIndexEntity> indexEntityClass = SubEntities.BEAN_INDEX_MAP.get(
        beanClass);
    final Set<Field> allFields = ReflectionUtils.getAllFields(indexEntityClass);
    final List<String> indexedColumnNames = new ArrayList<>();
    for (final Field field : allFields) {
      indexedColumnNames.add(field.getName());
    }
    return indexedColumnNames;
  }

  public <E extends AbstractDTO> Long put(final E pojo) {
    if(pojo.getId() != null) {
      return null;
    }
    try {
      return transactionService.executeTransaction((connection) -> {
        final GenericJsonEntity genericJsonEntity = toGenericJsonEntity(pojo);
        final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(pojo.getClass());
        final Long generatedKey = databaseService.save(genericJsonEntity, connection);
        pojo.setId(generatedKey);
        if (indexClass != null) {
          final AbstractIndexEntity abstractIndexEntity = toAbstractIndexEntity(
              pojo,
              indexClass,
              genericJsonEntity.getJsonVal());
          abstractIndexEntity.setVersion(1);
          abstractIndexEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
          return databaseService.save(abstractIndexEntity, connection);
        } else {
          return pojo.getId();
        }
      }, null);
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
      return null;
    }
  }

  private <E extends AbstractDTO> GenericJsonEntity toGenericJsonEntity(final E pojo)
      throws JsonProcessingException {
    GenericJsonEntity ret = new GenericJsonEntity();
    int version = pojo.getVersion() == 0 ? 1 : pojo.getVersion();
    ret.setId(pojo.getId());
    ret.setCreateTime(new Timestamp(System.currentTimeMillis()));
    ret.setUpdateTime(new Timestamp(System.currentTimeMillis()));
    ret.setVersion(version);
    ret.setType(SubEntities.getType(pojo.getClass()));
    final String jsonVal = toJsonString(pojo);
    ret.setJsonVal(jsonVal);
    return ret;
  }

  private <E extends AbstractDTO> String toJsonString(final E pojo) throws JsonProcessingException {
    return OBJECT_MAPPER.writeValueAsString(pojo);
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
      Preconditions.checkNotNull(pojo.getId());
      try {
        updateCounter += addUpdateToConnection(pojo, null);
      } catch (final Exception e) {
        LOG.error("Could not update entity : {}", pojo, e);
      }
    }
    return updateCounter;
  }

  public <E extends AbstractDTO> int update(final E pojo) {
    if (pojo.getId() == null) {
      throw new IllegalArgumentException(String.format("Need an ID to update the DB entity: %s",
          pojo));
    }
    return update(pojo, null);
  }

  public <E extends AbstractDTO> int update(final E pojo, final Predicate predicate) {
    try {
      return addUpdateToConnection(pojo, predicate);
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      return 0;
    }
  }

  private <E extends AbstractDTO> int addUpdateToConnection(final E pojo, final Predicate predicate) {
    try {
      final GenericJsonEntity genericJsonEntity = toGenericJsonEntity(pojo);
      final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(pojo.getClass());
      return transactionService.executeTransaction((connection) -> {
        Integer ret = databaseService.update(genericJsonEntity, predicate, connection);
        //update indexes
        if (ret == 1) {
          if (indexClass != null) {
            final AbstractIndexEntity abstractIndexEntity = toAbstractIndexEntity(pojo,
                indexClass,
                genericJsonEntity.getJsonVal());
            //updates all columns in the index table by default
            ret = databaseService.update(abstractIndexEntity, null, connection);
          }
        }
        if(ret > 1) {
          throw new ThirdEyeException(ThirdEyeStatus.ERR_UNKNOWN, "Too many rows updated");
        }
        return ret;
      }, 0);
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return 0;
    }
  }

  public <E extends AbstractDTO> List<E> getAll(final Class<E> beanClass) {
    try {
      final Predicate predicate = Predicate.EQ("type", SubEntities.getType(beanClass));
      final List<GenericJsonEntity> entities = transactionService.executeTransaction(
          (connection) -> databaseService.findAll(predicate,
              null,
              null,
              GenericJsonEntity.class,
              connection), Collections.emptyList());
      final List<E> ret = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(entities)) {
        for (final GenericJsonEntity entity : entities) {
          final E e = getBean(entity, beanClass);
          ret.add(e);
        }
      }
      return ret;
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  private <E extends AbstractDTO> E getBean(final GenericJsonEntity entity, final Class<E> beanClass)
      throws JsonProcessingException {
    E e = OBJECT_MAPPER.readValue(entity.getJsonVal(), beanClass);
    e.setId(entity.getId());
    e.setVersion(entity.getVersion());
    e.setCreateTime(entity.getCreateTime());
    e.setUpdateTime(entity.getUpdateTime());
    return e;
  }

  public <E extends AbstractDTO> List<E> list(final Class<E> beanClass, final long limit,
      final long offset) {
    try {
      final Predicate predicate = Predicate.EQ("type", SubEntities.getType(beanClass));
      final List<GenericJsonEntity> entities = transactionService.executeTransaction(
          (connection) -> databaseService.findAll(predicate,
              limit,
              offset,
              GenericJsonEntity.class,
              connection), Collections.emptyList());
      final List<E> result = new ArrayList<>();
      if (entities != null) {
        for (final GenericJsonEntity entity : entities) {
          final E e = getBean(entity, beanClass);
          result.add(e);
        }
      }
      return result;
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  public <E extends AbstractDTO> long count(final Class<E> beanClass) {
    final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(
          beanClass);
    try {
      return transactionService.executeTransaction(
          (connection) -> databaseService.count(null, indexClass, connection),
          -1L);
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
      return -1L;
    }
  }

  public <E extends AbstractDTO> long count(final Predicate predicate, final Class<E> beanClass) {
    final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(
          beanClass);
    try {
      return transactionService.executeTransaction(
          (connection) -> databaseService.count(predicate, indexClass, connection),
          -1L);
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
      return -1L;
    }
  }

  public <E extends AbstractDTO> E get(final Long id, final Class<E> pojoClass) {
    try {
      final GenericJsonEntity genericJsonEntity = transactionService.executeTransaction(
          (connection) -> databaseService.find(id, GenericJsonEntity.class, connection),
          null);
      if (genericJsonEntity == null) {
        return null;
      }
      final String type = SubEntities.getType(pojoClass);
      /* Object with id just not match type. Hence, return null */
      if (!type.equals(genericJsonEntity.getType())) {
        return null;
      }
      return getBean(genericJsonEntity, pojoClass);
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return null;
    }
  }

  public Object getRaw(final Long id) {
    try {
      final GenericJsonEntity genericJsonEntity = transactionService.executeTransaction(
          (connection) -> databaseService.find(id, GenericJsonEntity.class, connection),
          null);
      if (genericJsonEntity != null) {
        return getBean(genericJsonEntity, SubEntities.BEAN_TYPE_MAP.asMultimap().inverse().get(
            SubEntityType.valueOf(genericJsonEntity.getType())).asList().get(0));
      }
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
    }
    return null;
  }

  public <E extends AbstractDTO> List<E> get(final List<Long> idList, final Class<E> pojoClass) {
    try {
      final Predicate predicate = Predicate.IN("id", idList.toArray());
      final List<GenericJsonEntity> genericJsonEntities = transactionService.executeTransaction(
          (connection) -> databaseService.findAll(predicate,
              null,
              null,
              GenericJsonEntity.class,
              connection), Collections.emptyList());
      final List<E> result = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(genericJsonEntities)) {
        for (final GenericJsonEntity genericJsonEntity : genericJsonEntities) {
          final E e = getBean(genericJsonEntity, pojoClass);
          result.add(e);
        }
      }
      return result;
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  @SuppressWarnings("unchecked")
  public <E extends AbstractDTO> List<E> filter(final DaoFilter daoFilter) {
    requireNonNull(daoFilter.getPredicate(),
        "If the predicate is null, you can just do "
            + "getAll() which doesn't need to fetch IDs first");

    final Class<? extends AbstractDTO> beanClass = daoFilter.getBeanClass();
    final List<Long> ids = filterIds(daoFilter);
    if (ids.isEmpty()) {
      return Collections.emptyList();
    }
    return (List<E>) get(ids, beanClass);
  }

  /**
   * @param parameterizedSQL second part of the sql (omit select from table section)
   */
  public <E extends AbstractDTO> List<E> executeParameterizedSQL(final String parameterizedSQL,
      final Map<String, Object> parameterMap, final Class<E> pojoClass) {
    final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(
          pojoClass);
    try {
      final List<? extends AbstractIndexEntity> indexEntities = transactionService.executeTransaction(
          (connection) -> databaseService.runSQL(
              parameterizedSQL,
              parameterMap,
              indexClass,
              connection), Collections.emptyList());
      final List<Long> idsToFind = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(indexEntities)) {
        for (final AbstractIndexEntity entity : indexEntities) {
          idsToFind.add(entity.getBaseId());
        }
      }
      //fetch the entities
      if (!idsToFind.isEmpty()) {
        return get(idsToFind, pojoClass);
      }
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  public <E extends AbstractDTO> List<E> get(final Map<String, Object> filterParams,
      final Class<E> pojoClass) {
    final Predicate[] childPredicates = new Predicate[filterParams.size()];
    int index = 0;
    for (final Entry<String, Object> entry : filterParams.entrySet()) {
      childPredicates[index] = Predicate.EQ(entry.getKey(), entry.getValue());
      index = index + 1;
    }
    return get(Predicate.AND(childPredicates), pojoClass);
  }

  public <E extends AbstractDTO> List<E> get(final Predicate predicate, final Class<E> pojoClass) {
    final List<Long> idsToFind = getIdsByPredicate(predicate, pojoClass);
    return get(idsToFind, pojoClass);
  }

  public <E extends AbstractDTO> List<Long> getIdsByPredicate(final Predicate predicate,
      final Class<E> pojoClass) {
    return filterIds(new DaoFilter().setPredicate(predicate).setBeanClass(pojoClass));
  }

  public List<Long> filterIds(final DaoFilter daoFilter) {
    //apply the predicates and fetch the primary key ids
      final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(
          daoFilter.getBeanClass());
    try {
      //find the matching ids
      final List<? extends AbstractIndexEntity> indexEntities = transactionService.executeTransaction(
          (connection) -> databaseService.findAll(daoFilter.getPredicate(),
              null,
              null,
              indexClass,
              connection), Collections.emptyList());
      final List<Long> idsToReturn = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(indexEntities)) {
        for (final AbstractIndexEntity entity : indexEntities) {
          idsToReturn.add(entity.getBaseId());
        }
      }
      return idsToReturn;
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  /**
   * Dump all entities of type entityClass to logger
   * This utility is useful to dump the entire table. However, it gets executed in code regularly in
   * debug mode.
   *
   * @param entityClass The entity class.
   */
  @SuppressWarnings("unused")
  private void dumpTable(final Class<? extends AbstractEntity> entityClass) {
    if (IS_DEBUG) {
      try {
        final List<? extends AbstractEntity> entities = transactionService.executeTransaction(
            (connection) -> databaseService.findAll(null,
                null,
                null,
                entityClass,
                connection), Collections.emptyList());
        for (final AbstractEntity entity : entities) {
          LOG.debug("{}", entity);
        }
      } catch (SQLException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  public <E extends AbstractDTO> int delete(final Long id, final Class<E> pojoClass) {
    return delete(List.of(id), pojoClass);
  }

  public <E extends AbstractDTO> int delete(final List<Long> idsToDelete,
      final Class<E> pojoClass) {
    final Class<? extends AbstractIndexEntity> indexEntityClass = SubEntities.BEAN_INDEX_MAP.get(
        pojoClass);
    try {
      return transactionService.executeTransaction((connection) -> {
        // delete entry from base table
        databaseService.delete(
            Predicate.IN(databaseService.getIdColumnName(GenericJsonEntity.class), idsToDelete.toArray()),
            GenericJsonEntity.class,
            connection);
        // delete entry from index table
        return databaseService.delete(
            Predicate.IN(databaseService.getIdColumnName(indexEntityClass), idsToDelete.toArray()),
            indexEntityClass,
            connection);
      }, 0);
    } catch (SQLException e) {
      LOG.error(e.getMessage(),e);
      return 0;
    }
  }

  public <E extends AbstractDTO> int deleteByPredicate(final Predicate predicate,
      final Class<E> pojoClass) {
    final List<Long> idsToDelete = getIdsByPredicate(predicate, pojoClass);
    return delete(idsToDelete, pojoClass);
  }
}
