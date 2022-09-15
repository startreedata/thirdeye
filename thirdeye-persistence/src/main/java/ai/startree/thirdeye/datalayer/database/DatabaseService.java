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
package ai.startree.thirdeye.datalayer.database;

import static ai.startree.thirdeye.datalayer.mapper.DtoIndexMapper.toAbstractIndexEntity;

import ai.startree.thirdeye.datalayer.entity.AbstractEntity;
import ai.startree.thirdeye.datalayer.entity.AbstractIndexEntity;
import ai.startree.thirdeye.datalayer.entity.GenericJsonEntity;
import ai.startree.thirdeye.datalayer.util.GenericResultSetMapper;
import ai.startree.thirdeye.datalayer.util.SqlQueryBuilder;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DatabaseService {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseService.class);

  private final SqlQueryBuilder sqlQueryBuilder;
  private final DataSource dataSource;
  private final GenericResultSetMapper genericResultSetMapper;
  private final Counter dbReadCallCounter;
  private final Counter dbWriteCallCounter;
  private final Histogram dbReadDuration;
  private final Histogram dbWriteDuration;
  private final Counter dbExceptionCounter;
  private final Counter dbCallCounter;

  @Inject
  public DatabaseService(final SqlQueryBuilder sqlQueryBuilder,
      final DataSource dataSource,
      final GenericResultSetMapper genericResultSetMapper,
      final MetricRegistry metricRegistry) {
    this.sqlQueryBuilder = sqlQueryBuilder;
    this.dataSource = dataSource;
    this.genericResultSetMapper = genericResultSetMapper;

    dbExceptionCounter = metricRegistry.counter("dbExceptionCounter");
    dbCallCounter = metricRegistry.counter("dbCallCounter");
    dbReadCallCounter = metricRegistry.counter("dbReadCallCounter");
    dbWriteDuration = metricRegistry.histogram("dbWriteDuration");
    dbWriteCallCounter = metricRegistry.counter("dbWriteCallCounter");
    dbReadDuration = metricRegistry.histogram("dbReadDuration");
  }


  public <E extends AbstractEntity> E find(final Long id, final Class<E> clazz) {
    return findAll(Predicate.EQ(getIdColumnName(clazz), id), clazz).stream().findFirst().orElse(null);
  }

  public <E extends AbstractEntity> List<E> findAll(final Class<E> clazz) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        try (final PreparedStatement selectStatement = sqlQueryBuilder
            .createFindAllStatement(connection, clazz)) {
          try (final ResultSet resultSet = selectStatement.executeQuery()) {
            return genericResultSetMapper.mapAll(resultSet, clazz);
          }
        }
      }, Collections.emptyList());
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractEntity> List<E> findAll(final Predicate predicate, final Class<E> clazz) {
    return findAll(predicate, null, null, clazz);
  }

  public <E extends AbstractEntity> List<E> findAll(final Predicate predicate, final Long limit, final Long offset, final Class<E> clazz) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        try (final PreparedStatement selectStatement = sqlQueryBuilder
            .createfindByParamsStatementWithLimit(connection,
                clazz,
                predicate,
                limit,
                offset)) {
          try (final ResultSet resultSet = selectStatement.executeQuery()) {
            return genericResultSetMapper.mapAll(resultSet, clazz);
          }
        }
      }, Collections.emptyList());
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractEntity> Long save(final E entity) {
    try {
      return save(entity, null);
    } catch (Exception e) {
      return null;
    }
  }
  public <E extends AbstractEntity> Long save(final E entity, final Connection managedConnection)
      throws Exception {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        try (final PreparedStatement baseTableInsertStmt = sqlQueryBuilder
            .createInsertStatement(connection, entity)) {
          final int affectedRows = baseTableInsertStmt.executeUpdate();
          if (affectedRows == 1) {
            try (final ResultSet generatedKeys = baseTableInsertStmt.getGeneratedKeys()) {
              if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
              } else {
                return entity.getId();
              }
            }
          }
        }
        return null;
      }, null, managedConnection);
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractEntity> Integer update(final E entity) {
    return update(entity, null);
  }

  public <E extends AbstractEntity> Integer update(final E entity, final Predicate predicate) {
    try {
      return update(entity, predicate, null);
    } catch (Exception e) {
      return 0;
    }
  }

  public <E extends AbstractEntity> Integer update(final E entity, final Predicate predicate, final Connection managedConnection)
      throws Exception {
    final E dbEntity = (E) find(entity.getId(), entity.getClass());
    final Predicate finalPredicate;
    final String idCol = getIdColumnName(entity.getClass());
    if(predicate == null) {
      finalPredicate = Predicate.EQ(idCol, entity.getId());
    } else {
      finalPredicate = Predicate.AND(predicate, Predicate.EQ(idCol, entity.getId()));
    }
    if(dbEntity != null) {
      final long tStart = System.nanoTime();
      entity.setCreateTime(dbEntity.getCreateTime());
      try {
        return runTask(connection -> {
          try (final PreparedStatement baseTableInsertStmt = sqlQueryBuilder
              .createUpdateStatement(connection, entity, null, finalPredicate)) {
            return baseTableInsertStmt.executeUpdate();
          }
        }, 0, managedConnection);
      } finally {
        dbWriteCallCounter.inc();
        dbWriteDuration.update(System.nanoTime() - tStart);
      }
    }
    return 0;
  }

  private <E extends AbstractEntity> String getIdColumnName(final Class<E> clazz) {
    return AbstractIndexEntity.class.isAssignableFrom(clazz) ? "baseId" : "id";
  }

  // TODO shounak
  // replace the below method by delete(Predicate predicate, ...)
  // after supporting a delete by predicate SqlQueryBuilder method
  public Integer deleteByBaseId(final List<Long> idsToDelete, final Class<? extends AbstractIndexEntity> entityClass, final Connection managedConnection)
      throws Exception {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        if (CollectionUtils.isEmpty(idsToDelete)) {
          return 0;
        }
        try (final PreparedStatement baseTableDeleteStatement = sqlQueryBuilder
            .createDeleteStatement(connection, entityClass, idsToDelete, true)) {
          return baseTableDeleteStatement.executeUpdate();
        }
      }, 0, managedConnection);
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
  }

  public Integer delete(final List<Long> idsToDelete, final Class<? extends AbstractEntity> entityClass) {
    try {
      return delete(idsToDelete, entityClass, null);
    } catch (Exception e) {
      return 0;
    }
  }

  public Integer delete(final List<Long> idsToDelete, final Class<? extends AbstractEntity> entityClass, final Connection managedConnection)
      throws Exception {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        if (CollectionUtils.isEmpty(idsToDelete)) {
          return 0;
        }
        try (final PreparedStatement baseTableDeleteStatement = sqlQueryBuilder
            .createDeleteStatement(connection, entityClass, idsToDelete, false)) {
          return baseTableDeleteStatement.executeUpdate();
        }
      }, 0, managedConnection);
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractIndexEntity> Long count(Class<E> clazz) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        try (final PreparedStatement selectStatement = sqlQueryBuilder
            .createCountStatement(connection, clazz)) {
          try (final ResultSet resultSet = selectStatement.executeQuery()) {
            if (resultSet.next()) {
              return resultSet.getLong(1);
            }
          }
        }
        return -1L;
      }, -1L);
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractIndexEntity> Long count(Predicate predicate, Class<E> clazz) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        try (final PreparedStatement selectStatement = sqlQueryBuilder
            .createCountWhereStatement(connection,
                new DaoFilter().setPredicate(predicate),
                clazz)) {
          try (final ResultSet resultSet = selectStatement.executeQuery()) {
            if (resultSet.next()) {
              return resultSet.getLong(1);
            }
          }
        }
        return -1L;
      }, -1L);
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractIndexEntity> List<E> runSQL(
      final String parameterizedSQL,
      final Map<String, Object> parameterMap,
      final Class<E> indexClass) {
    final long tStart = System.nanoTime();
    try {
      return runTask(connection -> {
        try (final PreparedStatement findMatchingIdsStatement = sqlQueryBuilder
            .createStatementFromSQL(connection,
                parameterizedSQL,
                parameterMap,
                indexClass)) {
          try (final ResultSet rs = findMatchingIdsStatement.executeQuery()) {
            return genericResultSetMapper.mapAll(rs, indexClass);
          }
        }
      }, Collections.emptyList());
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  /**
   * Use at your own risk!!! Ensure to close the connection after using it or it can cause a leak.
   */
  private Connection getConnection()
      throws SQLException {
    // ensure to close the connection
    return dataSource.getConnection();
  }


  <T> T runTask(final QueryTask<T> task, final T defaultReturnValue, final Connection connection)
      throws Exception {
    if(connection != null) {
      dbCallCounter.inc();
      return task.handle(connection);
    } else {
      return runTask(task, defaultReturnValue);
    }
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

  protected interface QueryTask<T> {

    T handle(Connection connection) throws Exception;
  }

  /**
   * TODO shounak
   * The below section is very specific to GenericPojoDao.
   * Should be eventually removed
   */

  public <E extends AbstractDTO> Long save(final E entity, GenericJsonEntity genericJsonEntity, Class<? extends AbstractIndexEntity> indexClass) {
    return runTask(connection -> {
      final Long generatedKey = save(genericJsonEntity, connection);
      entity.setId(generatedKey);
      if (indexClass != null) {
        final AbstractIndexEntity abstractIndexEntity = toAbstractIndexEntity(
            entity,
            indexClass,
            genericJsonEntity.getJsonVal());
        abstractIndexEntity.setVersion(1);
        abstractIndexEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        return save(abstractIndexEntity, connection);
      } else {
        return entity.getId();
      }
    }, null);
  }

  public <E extends AbstractDTO> Integer update(final E entity, GenericJsonEntity genericJsonEntity, Class<? extends AbstractIndexEntity> indexClass, Predicate predicate) {
    return runTask(connection -> {
      Integer ret = update(genericJsonEntity, predicate, connection);
      //update indexes
      if (ret == 1) {
        if (indexClass != null) {
          final AbstractIndexEntity abstractIndexEntity = toAbstractIndexEntity(entity,
              indexClass,
              genericJsonEntity.getJsonVal());
          //updates all columns in the index table by default
          ret = update(abstractIndexEntity, null, connection);
        }
      }
      if(ret > 1) {
        throw new ThirdEyeException(ThirdEyeStatus.ERR_UNKNOWN, "Too many rows updated");
      }
      return ret;
    }, 0);
  }

  public Integer deleteByIds(final List<Long> idsToDelete, Class<? extends AbstractIndexEntity> indexClass) {
    return runTask(connection -> {
      // delete entry from base table
      delete(idsToDelete, GenericJsonEntity.class, connection);
      // delete entry from index table
      return deleteByBaseId(idsToDelete, indexClass, connection);
    }, 0);
  }

}
