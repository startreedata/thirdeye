package ai.startree.thirdeye.datalayer.database;

import ai.startree.thirdeye.datalayer.entity.AbstractEntity;
import ai.startree.thirdeye.datalayer.entity.AbstractIndexEntity;
import ai.startree.thirdeye.datalayer.entity.GenericJsonEntity;
import ai.startree.thirdeye.datalayer.util.GenericResultSetMapper;
import ai.startree.thirdeye.datalayer.util.SqlQueryBuilder;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;

@Singleton
public class DatabaseService {

  private final SqlQueryBuilder sqlQueryBuilder;
  private final DatabaseTaskService taskService;
  private final GenericResultSetMapper genericResultSetMapper;


  private final Counter dbReadCallCounter;
  private final Counter dbWriteCallCounter;
  private final Histogram dbReadDuration;
  private final Histogram dbWriteDuration;

  @Inject
  public DatabaseService(final SqlQueryBuilder sqlQueryBuilder,
      final DatabaseTaskService taskService,
      final GenericResultSetMapper genericResultSetMapper,
      final MetricRegistry metricRegistry) {
    this.sqlQueryBuilder = sqlQueryBuilder;
    this.taskService = taskService;
    this.genericResultSetMapper = genericResultSetMapper;

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
      return taskService.runTask(connection -> {
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
      return taskService.runTask(connection -> {
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
    final long tStart = System.nanoTime();
    try {
      return taskService.runTask(connection -> {
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
        return 0L;
      }, 0L);
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractEntity> Integer update(final E entity) {
    return update(entity, null);
  }

  public <E extends AbstractEntity> Integer update(final E entity, final Predicate predicate) {
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
        return taskService.runTask(connection -> {
          try (final PreparedStatement baseTableInsertStmt = sqlQueryBuilder
              .createUpdateStatement(connection, entity, null, finalPredicate)) {
            return baseTableInsertStmt.executeUpdate();
          }
        }, 0);
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
  public Integer deleteByBaseId(final List<Long> idsToDelete, final Class<? extends AbstractIndexEntity> entityClass) {
    final long tStart = System.nanoTime();
    try {
      return taskService.runTask(connection -> {
        if (CollectionUtils.isEmpty(idsToDelete)) {
          return 0;
        }
        try (final PreparedStatement baseTableDeleteStatement = sqlQueryBuilder
            .createDeleteStatement(connection, entityClass, idsToDelete, true)) {
          return baseTableDeleteStatement.executeUpdate();
        }
      }, 0);
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
  }

  public Integer delete(final List<Long> idsToDelete, final Class<? extends AbstractEntity> entityClass) {
    final long tStart = System.nanoTime();
    try {
      return taskService.runTask(connection -> {
        if (CollectionUtils.isEmpty(idsToDelete)) {
          return 0;
        }
        try (final PreparedStatement baseTableDeleteStatement = sqlQueryBuilder
            .createDeleteStatement(connection, entityClass, idsToDelete, false)) {
          return baseTableDeleteStatement.executeUpdate();
        }
      }, 0);
    } finally {
      dbWriteCallCounter.inc();
      dbWriteDuration.update(System.nanoTime() - tStart);
    }
  }

  public <E extends AbstractIndexEntity> Long count(Class<E> clazz) {
    final long tStart = System.nanoTime();
    try {
      return taskService.runTask(connection -> {
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
      return taskService.runTask(connection -> {
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
      return taskService.runTask(connection -> {
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

}
