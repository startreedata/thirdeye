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

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datalayer.entity.AbstractEntity;
import ai.startree.thirdeye.datalayer.entity.AbstractIndexEntity;
import ai.startree.thirdeye.datalayer.entity.GenericJsonEntity;
import ai.startree.thirdeye.datalayer.entity.RcaInvestigationIndex;
import ai.startree.thirdeye.datalayer.mapper.RcaInvestigationIndexMapper;
import ai.startree.thirdeye.datalayer.util.GenericResultSetMapper;
import ai.startree.thirdeye.datalayer.util.SqlQueryBuilder;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GenericPojoDao {

  private static final Logger LOG = LoggerFactory.getLogger(GenericPojoDao.class);

  private static final boolean IS_DEBUG = LOG.isDebugEnabled();
  private static final int MAX_BATCH_SIZE = 1000;
  private static final ModelMapper MODEL_MAPPER = new ModelMapper();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    // add custom mapping from DTO to index
    MODEL_MAPPER.createTypeMap(RcaInvestigationDTO.class, RcaInvestigationIndex.class)
        .addMappings(new RcaInvestigationIndexMapper());
  }

  private final Counter dbReadCallCounter;
  private final Counter dbWriteCallCounter;
  private final Histogram dbReadDuration;
  private final Histogram dbWriteDuration;
  private final Counter dbReadByteCounter;
  private final Counter dbWriteByteCounter;
  private final Counter dbExceptionCounter;
  private final Counter dbCallCounter;

  private final DataSource dataSource;
  private final SqlQueryBuilder sqlQueryBuilder;
  private final GenericResultSetMapper genericResultSetMapper;

  @Inject
  public GenericPojoDao(final DataSource dataSource,
      final SqlQueryBuilder sqlQueryBuilder,
      final GenericResultSetMapper genericResultSetMapper,
      final MetricRegistry metricRegistry) {
    this.dataSource = dataSource;
    this.sqlQueryBuilder = sqlQueryBuilder;
    this.genericResultSetMapper = genericResultSetMapper;

    dbReadCallCounter = metricRegistry.counter("dbReadCallCounter");
    dbWriteByteCounter = metricRegistry.counter("dbWriteByteCounter");
    dbWriteDuration = metricRegistry.histogram("dbWriteDuration");
    dbWriteCallCounter = metricRegistry.counter("dbWriteCallCounter");
    dbReadDuration = metricRegistry.histogram("dbReadDuration");
    dbReadByteCounter = metricRegistry.counter("dbReadByteCounter");
    dbExceptionCounter = metricRegistry.counter("dbExceptionCounter");
    dbCallCounter = metricRegistry.counter("dbCallCounter");

    checkState(SubEntities.BEAN_INDEX_MAP.size() == SubEntities.BEAN_TYPE_MAP.size(),
        "Entity Metadata is inconsistent!");
  }

  public Set<Class<? extends AbstractDTO>> getAllBeanClasses() {
    return SubEntities.BEAN_INDEX_MAP.keySet();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public List<String> getIndexedColumns(final Class beanClass) {
    final Class<? extends AbstractIndexEntity> indexEntityClass = SubEntities.BEAN_INDEX_MAP.get(beanClass);
    final Set<Field> allFields = ReflectionUtils.getAllFields(indexEntityClass);
    final List<String> indexedColumnNames = new ArrayList<>();
    for (final Field field : allFields) {
      indexedColumnNames.add(field.getName());
    }
    return indexedColumnNames;
  }

  /**
   * Use at your own risk!!! Ensure to close the connection after using it or it can cause a leak.
   */
  public Connection getConnection()
      throws SQLException {
    // ensure to close the connection
    return dataSource.getConnection();
  }

  public <E extends AbstractDTO> Long put(final E pojo) {
    final long tStart = System.nanoTime();
    try {
      //insert into its base table
      //get the generated id
      //update indexes
      return runTask(connection -> {
        final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(pojo.getClass());
        final GenericJsonEntity genericJsonEntity = new GenericJsonEntity();
        genericJsonEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        genericJsonEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        genericJsonEntity.setVersion(1);
        genericJsonEntity.setType(SubEntities.getType(pojo.getClass()));
        final String jsonVal = toJsonString(pojo);
        genericJsonEntity.setJsonVal(jsonVal);
        dbWriteByteCounter.inc(jsonVal.length());

        try (final PreparedStatement baseTableInsertStmt = sqlQueryBuilder
            .createInsertStatement(connection, genericJsonEntity)) {
          final int affectedRows = baseTableInsertStmt.executeUpdate();
          if (affectedRows == 1) {
            try (final ResultSet generatedKeys = baseTableInsertStmt.getGeneratedKeys()) {
              if (generatedKeys.next()) {
                pojo.setId(generatedKeys.getLong(1));
              }
            }
            if (indexClass != null) {
              final AbstractIndexEntity abstractIndexEntity = indexClass.newInstance();
              MODEL_MAPPER.map(pojo, abstractIndexEntity);
              abstractIndexEntity.setBaseId(pojo.getId());
              abstractIndexEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
              abstractIndexEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
              abstractIndexEntity.setVersion(1);
              final int numRowsCreated;
              try (final PreparedStatement indexTableInsertStatement = sqlQueryBuilder
                  .createInsertStatement(connection, abstractIndexEntity)) {
                numRowsCreated = indexTableInsertStatement.executeUpdate();
              }
              if (numRowsCreated == 1) {
                return pojo.getId();
              }
            } else {
              return pojo.getId();
            }
          }
        }
        return null;
      }, null);
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
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

    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        if (CollectionUtils.isEmpty(pojos)) {
          return 0;
        }

        int updateCounter = 0;
        int minIdx = 0;
        int maxIdx = MAX_BATCH_SIZE;
        final boolean isAutoCommit = connection.getAutoCommit();
        // Ensure that transaction mode is enabled
        connection.setAutoCommit(false);
        while (minIdx < pojos.size()) {
          final List<E> subList = pojos.subList(minIdx, Math.min(maxIdx, pojos.size()));
          try {
            for (final E pojo : subList) {
              Preconditions.checkNotNull(pojo.getId());
              addUpdateToConnection(pojo, Predicate.EQ("id", pojo.getId()), connection);
            }
            // Trigger commit() to ensure this batch of deletion is executed
            connection.commit();
            updateCounter += subList.size();
          } catch (final Exception e) {
            // Error recovery: rollback previous changes.
            connection.rollback();
            // Unable to do batch because of exception; fall back to single row deletion mode.
            for (final E pojo : subList) {
              try {
                final int updateRow = addUpdateToConnection(pojo,
                    Predicate.EQ("id", pojo.getId()),
                    connection);
                connection.commit();
                updateCounter += updateRow;
              } catch (final Exception e1) {
                connection.rollback();
                LOG.error("Exception while executing query task; skipping entity (id={})",
                    pojo.getId(),
                    e);
              }
            }
          }
          minIdx = maxIdx;
          maxIdx = maxIdx + MAX_BATCH_SIZE;
        }
        // Restore the original state of connection's auto commit
        connection.setAutoCommit(isAutoCommit);
        return updateCounter;
      }, 0);
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractDTO> int update(final E pojo) {
    if (pojo.getId() == null) {
      throw new IllegalArgumentException(String.format("Need an ID to update the DB entity: %s",
          pojo));
    }
    return update(pojo, Predicate.EQ("id", pojo.getId()));
  }

  public <E extends AbstractDTO> int update(final E pojo, final Predicate predicate) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> addUpdateToConnection(pojo, predicate, connection), 0);
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
  }

  private <E extends AbstractDTO> int addUpdateToConnection(final E pojo,
      final Predicate predicate,
      final Connection connection)
      throws Exception {
    //update base table
    final String jsonVal = toJsonString(pojo);

    final GenericJsonEntity genericJsonEntity = new GenericJsonEntity();
    genericJsonEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
    genericJsonEntity.setJsonVal(jsonVal);
    genericJsonEntity.setId(pojo.getId());
    genericJsonEntity.setVersion(pojo.getVersion());
    final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(pojo.getClass());
    final Set<String> fieldsToUpdate = Sets.newHashSet("jsonVal", "updateTime", "version");
    final int affectedRows;
    try (final PreparedStatement baseTableInsertStmt = sqlQueryBuilder
        .createUpdateStatement(connection, genericJsonEntity, fieldsToUpdate, predicate)) {
      affectedRows = baseTableInsertStmt.executeUpdate();
    }

    //update indexes
    if (affectedRows == 1) {
      dbWriteByteCounter.inc(jsonVal.length());
      if (indexClass != null) {
        final AbstractIndexEntity abstractIndexEntity = indexClass.newInstance();
        MODEL_MAPPER.map(pojo, abstractIndexEntity);
        abstractIndexEntity.setBaseId(pojo.getId());
        abstractIndexEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        //updates all columns in the index table by default
        try (final PreparedStatement indexTableInsertStatement = sqlQueryBuilder
            .createUpdateStatementForIndexTable(connection, abstractIndexEntity)) {
          final int numRowsUpdated = indexTableInsertStatement.executeUpdate();
          LOG.debug("numRowsUpdated: {}", numRowsUpdated);
          return numRowsUpdated;
        }
      }
    }

    return affectedRows;
  }

  public <E extends AbstractDTO> List<E> getAll(final Class<E> beanClass) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        final Predicate predicate = Predicate.EQ("type", SubEntities.getType(beanClass));
        final List<GenericJsonEntity> entities;
        try (final PreparedStatement selectStatement = sqlQueryBuilder
            .createFindByParamsStatement(connection, GenericJsonEntity.class, predicate)) {
          try (final ResultSet resultSet = selectStatement.executeQuery()) {
            entities = genericResultSetMapper.mapAll(resultSet, GenericJsonEntity.class);
          }
        }
        final List<E> ret = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
          for (final GenericJsonEntity entity : entities) {
            dbReadByteCounter.inc(entity.getJsonVal().length());

            final E e = getBean(entity, beanClass);
            e.setId(entity.getId());
            e.setUpdateTime(entity.getUpdateTime());
            ret.add(e);
          }
        }
        return ret;
      }, Collections.emptyList());
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  private <E> E getBean(final GenericJsonEntity entity, final Class<E> beanClass)
      throws JsonProcessingException {
    return OBJECT_MAPPER.readValue(entity.getJsonVal(), beanClass);
  }

  public <E extends AbstractDTO> List<E> list(final Class<E> beanClass, final long limit, final long offset) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        final List<GenericJsonEntity> entities;
        final Predicate predicate = Predicate.EQ("type", SubEntities.getType(beanClass));
        try (final PreparedStatement selectStatement = sqlQueryBuilder
            .createfindByParamsStatementWithLimit(connection,
                GenericJsonEntity.class,
                predicate,
                limit,
                offset)) {
          try (final ResultSet resultSet = selectStatement.executeQuery()) {
            entities = genericResultSetMapper.mapAll(resultSet, GenericJsonEntity.class);
          }
        }
        final List<E> result = new ArrayList<>();
        if (entities != null) {
          for (final GenericJsonEntity entity : entities) {
            dbReadByteCounter.inc(entity.getJsonVal().length());
            final E e = getBean(entity, beanClass);
            e.setId(entity.getId());
            e.setUpdateTime(entity.getUpdateTime());
            result.add(e);
          }
        }
        return result;
      }, Collections.emptyList());
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractDTO> long count(final Class<E> beanClass) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(beanClass);
        try (final PreparedStatement selectStatement = sqlQueryBuilder
            .createCountStatement(connection, indexClass)) {
          try (final ResultSet resultSet = selectStatement.executeQuery()) {
            if (resultSet.next()) {
              return resultSet.getInt(1);
            } else {
              throw new IllegalStateException("can't parse count query response");
            }
          }
        }
      }, -1);
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractDTO> long count(final Predicate predicate, final Class<E> beanClass) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(beanClass);
        try (final PreparedStatement selectStatement = sqlQueryBuilder
            .createCountWhereStatement(connection, new DaoFilter().setPredicate(predicate), indexClass)) {
          try (final ResultSet resultSet = selectStatement.executeQuery()) {
            if (resultSet.next()) {
              return resultSet.getInt(1);
            } else {
              throw new IllegalStateException("can't parse count with WHERE query response");
            }
          }
        }
      }, -1);
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractDTO> E get(final Long id, final Class<E> pojoClass) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        final GenericJsonEntity genericJsonEntity;
        try (final PreparedStatement selectStatement = sqlQueryBuilder.createFindByParamsStatement(
            connection,
            GenericJsonEntity.class,
            Predicate.AND(
                Predicate.EQ("id", id),
                Predicate.EQ("type", SubEntities.getType(pojoClass)))
        )
        ) {
          try (final ResultSet resultSet = selectStatement.executeQuery()) {
            genericJsonEntity = genericResultSetMapper.mapSingle(resultSet,
                GenericJsonEntity.class);
          }
        }
        if (genericJsonEntity == null) {
          return null;
        }
        dbReadByteCounter.inc(genericJsonEntity.getJsonVal().length());

        final E e = getBean(genericJsonEntity, pojoClass);
        e.setId(genericJsonEntity.getId());
        e.setVersion(genericJsonEntity.getVersion());
        e.setCreateTime(genericJsonEntity.getCreateTime());
        e.setUpdateTime(genericJsonEntity.getUpdateTime());
        return e;
      }, null);
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public Object getRaw(final Long id) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {

        final GenericJsonEntity genericJsonEntity;
        try (final PreparedStatement selectStatement = sqlQueryBuilder
            .createFindByIdStatement(connection, GenericJsonEntity.class, id)) {
          try (final ResultSet resultSet = selectStatement.executeQuery()) {
            genericJsonEntity = genericResultSetMapper.mapSingle(resultSet,
                GenericJsonEntity.class);
          }
        }
        Object e = null;
        if (genericJsonEntity != null) {
          dbReadByteCounter.inc(genericJsonEntity.getJsonVal().length());
          e = getBean(genericJsonEntity, Object.class);
        }
        return e;
      }, null);
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractDTO> List<E> get(final List<Long> idList, final Class<E> pojoClass) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        final List<GenericJsonEntity> genericJsonEntities;
        try (final PreparedStatement selectStatement = sqlQueryBuilder
            .createFindByIdStatement(connection, GenericJsonEntity.class, idList)) {
          try (final ResultSet resultSet = selectStatement.executeQuery()) {
            genericJsonEntities = genericResultSetMapper.mapAll(resultSet,
                GenericJsonEntity.class);
          }
        }
        final List<E> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(genericJsonEntities)) {
          for (final GenericJsonEntity genericJsonEntity : genericJsonEntities) {
            dbReadByteCounter.inc(genericJsonEntity.getJsonVal().length());

            final E e = getBean(genericJsonEntity, pojoClass);
            e.setId(genericJsonEntity.getId());
            e.setVersion(genericJsonEntity.getVersion());
            e.setUpdateTime(genericJsonEntity.getUpdateTime());
            result.add(e);
          }
        }
        return result;
      }, Collections.emptyList());
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
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

    final long tStart = System.nanoTime();
    try {
      //apply the predicates and fetch the primary key ids
      //look up the id and convert them to bean
      return runTask(connection -> {
        //fetch the entities
        final List<E> results = new ArrayList<>();
        try (final PreparedStatement selectStatement = sqlQueryBuilder
            .createFindByIdStatement(connection, GenericJsonEntity.class, ids)) {
          final List<GenericJsonEntity> entities;
          try (final ResultSet resultSet = selectStatement.executeQuery()) {
            entities = genericResultSetMapper.mapAll(resultSet, GenericJsonEntity.class);
          }
          if (CollectionUtils.isNotEmpty(entities)) {
            for (final GenericJsonEntity entity : entities) {
              final String json = entity.getJsonVal();
              dbReadByteCounter.inc(json.length());

              final E bean = (E) getBean(entity, beanClass);
              bean.setId(entity.getId())
                  .setVersion(entity.getVersion())
                  .setUpdateTime(entity.getUpdateTime());
              results.add(bean);
            }
          }
        }
        return results;
      }, Collections.emptyList());
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  /**
   * @param parameterizedSQL second part of the sql (omit select from table section)
   */
  public <E extends AbstractDTO> List<E> executeParameterizedSQL(final String parameterizedSQL,
      final Map<String, Object> parameterMap, final Class<E> pojoClass) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(pojoClass);
        final List<? extends AbstractIndexEntity> indexEntities;
        try (final PreparedStatement findMatchingIdsStatement = sqlQueryBuilder
            .createStatementFromSQL(connection,
                parameterizedSQL,
                parameterMap,
                indexClass)) {
          try (final ResultSet rs = findMatchingIdsStatement.executeQuery()) {
            indexEntities = genericResultSetMapper.mapAll(rs, indexClass);
          }
        }
        final List<Long> idsToFind = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(indexEntities)) {
          for (final AbstractIndexEntity entity : indexEntities) {
            idsToFind.add(entity.getBaseId());
          }
        }
        final List<E> ret = new ArrayList<>();
        //fetch the entities
        if (!idsToFind.isEmpty()) {
          final List<GenericJsonEntity> entities;
          try (final PreparedStatement selectStatement = sqlQueryBuilder
              .createFindByIdStatement(connection, GenericJsonEntity.class, idsToFind)) {
            try (final ResultSet resultSet = selectStatement.executeQuery()) {
              entities = genericResultSetMapper.mapAll(resultSet, GenericJsonEntity.class);
            }
          }
          if (CollectionUtils.isNotEmpty(entities)) {
            for (final GenericJsonEntity entity : entities) {
              dbReadByteCounter.inc(entity.getJsonVal().length());

              final E bean = getBean(entity, pojoClass);
              bean.setId(entity.getId());
              bean.setVersion(entity.getVersion());
              ret.add(bean);
            }
          }
        }
        return ret;
      }, Collections.emptyList());
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
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
    final long tStart = System.nanoTime();
    try {
      //apply the predicates and fetch the primary key ids
      //look up the id and convert them to bean
      return runTask(connection -> {
        //fetch the entities
        final List<E> ret = new ArrayList<>();
        if (!idsToFind.isEmpty()) {
          final List<GenericJsonEntity> entities;
          try (final PreparedStatement selectStatement = sqlQueryBuilder
              .createFindByIdStatement(connection, GenericJsonEntity.class, idsToFind)) {
            try (final ResultSet resultSet = selectStatement.executeQuery()) {
              entities = genericResultSetMapper.mapAll(resultSet, GenericJsonEntity.class);
            }
            if (CollectionUtils.isNotEmpty(entities)) {
              for (final GenericJsonEntity entity : entities) {
                dbReadByteCounter.inc(entity.getJsonVal().length());

                final E bean = getBean(entity, pojoClass);
                bean.setId(entity.getId());
                bean.setVersion(entity.getVersion());
                bean.setUpdateTime(entity.getUpdateTime());
                ret.add(bean);
              }
            }
          }
        }
        return ret;
      }, Collections.emptyList());
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractDTO> List<Long> getIdsByPredicate(final Predicate predicate,
      final Class<E> pojoClass) {
    return filterIds(new DaoFilter().setPredicate(predicate).setBeanClass(pojoClass));
  }

  public List<Long> filterIds(final DaoFilter daoFilter) {
    final long tStart = System.nanoTime();
    try {
      //apply the predicates and fetch the primary key ids
      return runTask(connection -> {
        final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(daoFilter.getBeanClass());
        //find the matching ids
        final List<? extends AbstractIndexEntity> indexEntities;
        try (final PreparedStatement findByParamsStatement = sqlQueryBuilder
            .createStatement(connection, daoFilter, indexClass)) {
          try (final ResultSet rs = findByParamsStatement.executeQuery()) {
            indexEntities = genericResultSetMapper.mapAll(rs, indexClass);
          }
        }
        final List<Long> idsToReturn = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(indexEntities)) {
          for (final AbstractIndexEntity entity : indexEntities) {
            idsToReturn.add(entity.getBaseId());
          }
        }
        return idsToReturn;
      }, Collections.emptyList());
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  /**
   * Dump all entities of type entityClass to logger
   * This utility is useful to dump the entire table. However, it gets executed in code regularly in
   * debug mode.
   *
   * @param connection SQL connection
   * @param entityClass The entity class.
   * @throws Exception exceptions encountered during row fetches
   */
  @SuppressWarnings("unused")
  private void dumpTable(final Connection connection, final Class<? extends AbstractEntity> entityClass)
      throws Exception {
    final long tStart = System.nanoTime();
    try {
      if (IS_DEBUG) {
        try (final PreparedStatement findAllStatement = sqlQueryBuilder.createFindAllStatement(connection,
            entityClass)) {
          try (final ResultSet resultSet = findAllStatement.executeQuery()) {
            final List<? extends AbstractEntity> entities = genericResultSetMapper.mapAll(resultSet,
                entityClass);
            for (final AbstractEntity entity : entities) {
              LOG.debug("{}", entity);
            }
          }
        }
      }
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractDTO> int delete(final Long id, final Class<E> pojoClass) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        final Class<? extends AbstractIndexEntity> indexClass = SubEntities.BEAN_INDEX_MAP.get(pojoClass);
        final Map<String, Object> filters = new HashMap<>();
        filters.put("id", id);
        try (final PreparedStatement deleteStatement = sqlQueryBuilder
            .createDeleteByIdStatement(connection, GenericJsonEntity.class, filters)) {
          deleteStatement.executeUpdate();
        }
        filters.clear();
        filters.put("baseId", id);

        try (final PreparedStatement deleteIndexStatement = sqlQueryBuilder
            .createDeleteByIdStatement(connection, indexClass, filters)) {
          return deleteIndexStatement.executeUpdate();
        }
      }, 0);
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractDTO> int delete(final List<Long> idsToDelete,
      final Class<E> pojoClass) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        if (CollectionUtils.isEmpty(idsToDelete)) {
          return 0;
        }

        final boolean isAutoCommit = connection.getAutoCommit();
        // Ensure that transaction mode is enabled
        connection.setAutoCommit(false);
        final Class<? extends AbstractIndexEntity> indexEntityClass = SubEntities.BEAN_INDEX_MAP.get(
            pojoClass);
        int updateCounter = 0;
        int minIdx = 0;
        int maxIdx = MAX_BATCH_SIZE;
        while (minIdx < idsToDelete.size()) {
          final List<Long> subList = idsToDelete.subList(minIdx, Math.min(maxIdx, idsToDelete.size()));
          try {
            final int updatedBaseRow = addBatchDeletionToConnection(subList,
                indexEntityClass,
                connection);
            // Trigger commit() to ensure this batch of deletion is executed
            connection.commit();
            updateCounter += updatedBaseRow;
          } catch (final Exception e) {
            // Error recovery: rollback previous changes.
            connection.rollback();
            // Unable to do batch because of exception; fall back to single row deletion mode.
            for (final Long pojoId : subList) {
              try {
                final int updatedBaseRow =
                    addBatchDeletionToConnection(Collections.singletonList(pojoId),
                        indexEntityClass,
                        connection);
                connection.commit();
                updateCounter += updatedBaseRow;
              } catch (final Exception e1) {
                connection.rollback();
                LOG.error("Exception while executing query task; skipping entity (id={})",
                    pojoId,
                    e);
              }
            }
          }
          minIdx = Math.min(maxIdx, idsToDelete.size());
          maxIdx += MAX_BATCH_SIZE;
        }
        // Restore the original state of connection's auto commit
        connection.setAutoCommit(isAutoCommit);
        return updateCounter;
      }, 0);
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
  }

  /**
   * Delete the given ids from base table and index table.
   *
   * @param idsToDelete the IDs to be deleted
   * @param indexEntityClass the index entity of the entities in the ID list; this method
   *     assumes that these entities
   *     to be deleted belong to the same index entity
   * @param connection the connection to the database.
   * @return the number of base rows that are deleted.
   * @throws Exception any exception from DB.
   */
  private int addBatchDeletionToConnection(final List<Long> idsToDelete,
      final Class<? extends AbstractIndexEntity> indexEntityClass, final Connection connection)
      throws Exception {
    try (final PreparedStatement statement = sqlQueryBuilder
        .createDeleteStatement(connection, indexEntityClass, idsToDelete, true)) {
      statement.executeUpdate();
    }
    try (final PreparedStatement baseTableDeleteStatement = sqlQueryBuilder
        .createDeleteStatement(connection, GenericJsonEntity.class, idsToDelete, false)) {
      return baseTableDeleteStatement.executeUpdate();
    }
  }

  public <E extends AbstractDTO> int deleteByPredicate(final Predicate predicate,
      final Class<E> pojoClass) {
    final List<Long> idsToDelete = getIdsByPredicate(predicate, pojoClass);
    return delete(idsToDelete, pojoClass);
  }

  <T> T runTask(final QueryTask<T> task, final T defaultReturnValue) {
    dbCallCounter.inc();

    Connection connection = null;
    try {
      connection = getConnection();
      // Enable transaction
      connection.setAutoCommit(false);
      final T t = task.handle(connection);
      // Commit this transaction
      connection.commit();
      return t;
    } catch (final Exception e) {
      LOG.error("Exception while executing query task", e);
      dbExceptionCounter.inc();

      // Rollback transaction in case json table is updated but index table isn't due to any errors (duplicate key, etc.)
      if (connection != null) {
        try {
          connection.rollback();
        } catch (final SQLException e1) {
          LOG.error("Failed to rollback SQL execution", e);
        }
      }
      return defaultReturnValue;
    } finally {
      // Always close connection before leaving
      if (connection != null) {
        try {
          connection.close();
        } catch (final SQLException e) {
          LOG.error("Failed to close connection", e);
        }
      }
    }
  }

  private interface QueryTask<T> {

    T handle(Connection connection) throws Exception;
  }
}
