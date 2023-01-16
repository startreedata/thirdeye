/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.datalayer;

import ai.startree.thirdeye.datalayer.entity.AbstractEntity;
import ai.startree.thirdeye.datalayer.entity.AbstractIndexEntity;
import ai.startree.thirdeye.datalayer.util.GenericResultSetMapper;
import ai.startree.thirdeye.datalayer.util.SqlQueryBuilder;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@Singleton
public class DatabaseService {

  private final SqlQueryBuilder sqlQueryBuilder;
  private final GenericResultSetMapper genericResultSetMapper;
  private final Counter dbReadCallCounter;
  private final Counter dbWriteCallCounter;
  private final Histogram dbReadDuration;
  private final Histogram dbWriteDuration;

  @Inject
  public DatabaseService(final SqlQueryBuilder sqlQueryBuilder,
      final GenericResultSetMapper genericResultSetMapper,
      final MetricRegistry metricRegistry) {
    this.sqlQueryBuilder = sqlQueryBuilder;
    this.genericResultSetMapper = genericResultSetMapper;

    dbReadCallCounter = metricRegistry.counter("dbReadCallCounter");
    dbWriteDuration = metricRegistry.histogram("dbWriteDuration");
    dbWriteCallCounter = metricRegistry.counter("dbWriteCallCounter");
    dbReadDuration = metricRegistry.histogram("dbReadDuration");
  }


  public <E extends AbstractEntity> E find(final Long id, final Class<E> clazz, final Connection connection)
      throws Exception {
    return findAll(Predicate.EQ(getIdColumnName(clazz), id), null, null, clazz, connection)
        .stream().findFirst().orElse(null);
  }

  public <E extends AbstractEntity> List<E> findAll(final Predicate predicate, final Long limit, final Long offset, final Class<E> clazz, final Connection connection)
      throws Exception {
    final long tStart = System.nanoTime();
    try {
      try (final PreparedStatement selectStatement = sqlQueryBuilder
          .createFindByParamsStatementWithLimit(connection,
              clazz,
              predicate,
              limit,
              offset)) {
        try (final ResultSet resultSet = selectStatement.executeQuery()) {
          return genericResultSetMapper.mapAll(resultSet, clazz);
        }
      }
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractEntity> Long save(final E entity, final Connection connection)
      throws Exception {
    final long tStart = System.nanoTime();
    try {
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
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
  }


  public <E extends AbstractEntity> Integer update(final E entity, final Predicate predicate, final Connection connection)
      throws Exception {
    final E dbEntity = (E) find(entity.getId(), entity.getClass(), connection);
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
        try (final PreparedStatement baseTableInsertStmt = sqlQueryBuilder
            .createUpdateStatement(connection, entity, null, finalPredicate)) {
          return baseTableInsertStmt.executeUpdate();
        }
      } finally {
        dbWriteCallCounter.inc();
        dbWriteDuration.update(System.nanoTime() - tStart);
      }
    }
    return 0;
  }

  public <E extends AbstractEntity> String getIdColumnName(final Class<E> clazz) {
    return AbstractIndexEntity.class.isAssignableFrom(clazz) ? "baseId" : "id";
  }

  public Integer delete(final Predicate predicate,
      final Class<? extends AbstractEntity> entityClass, final Connection connection)
      throws Exception {
    final long tStart = System.nanoTime();
    try {
      try (final PreparedStatement baseTableDeleteStatement = sqlQueryBuilder
          .createDeleteStatement(connection, entityClass, predicate)) {
        return baseTableDeleteStatement.executeUpdate();
      }
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractEntity> Long count(final Predicate predicate, final Class<E> clazz, final Connection connection)
      throws Exception {
    final long tStart = System.nanoTime();
    try {
      try (final PreparedStatement selectStatement = sqlQueryBuilder
          .createCountStatement(connection,
              predicate,
              clazz)) {
        try (final ResultSet resultSet = selectStatement.executeQuery()) {
          if (resultSet.next()) {
            return resultSet.getLong(1);
          }
        }
      }
      return -1L;
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractEntity> List<E> runSQL(
      final String parameterizedSQL,
      final Map<String, Object> parameterMap,
      final Class<E> clazz,
      final Connection connection) throws Exception {
    final long tStart = System.nanoTime();
    try {
        try (final PreparedStatement findMatchingIdsStatement = sqlQueryBuilder
            .createStatementFromSQL(connection,
                parameterizedSQL,
                parameterMap,
                clazz)) {
          try (final ResultSet rs = findMatchingIdsStatement.executeQuery()) {
            return genericResultSetMapper.mapAll(rs, clazz);
          }
        }
    } finally {
      dbReadCallCounter.inc();
      dbReadDuration.update(System.nanoTime() - tStart);
    }
  }

}
