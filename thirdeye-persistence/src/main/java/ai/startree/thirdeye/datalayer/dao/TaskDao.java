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

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datalayer.DatabaseClient;
import ai.startree.thirdeye.datalayer.DatabaseOrm;
import ai.startree.thirdeye.datalayer.entity.TaskEntity;
import ai.startree.thirdeye.datalayer.mapper.TaskEntityMapper;
import ai.startree.thirdeye.datalayer.util.GenericResultSetMapper;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TaskDao {

  // WARNING - this query is more tricky than it seems
  // the resolution of this query is heavily dependent on the engine (InnoDb) and the indexes
  // LIMIT 1 FOR UPDATE SKIP LOCKED may lock more than 1 (+ gap) row depending on the indexes
  // in the worst case, it locks every row it has to look at when performing the query (eg reading a row to resolve the WHERE or ORDER BY clause) 
  // and it locks every operation that would impact the indexes used in the query 
  // here it works because: 
  // in the WHERE clause, status and ref_id are indexed
  // in the ORDER BY clause: the id index (PRIMARY KEY AUTO_INCREMENT) helps and result in a single row read
  // note: ORDER BY create_time would not work and would lock all rows matching the WHERE clause
  // it is important to enforce in-order execution for tasks referencing the same entity - this condition may be relaxed later on
  // for the moment this means we need AND ref_id not in (select ref_id from task_entity where status = 'RUNNING') + ORDER BY id ASC
  private static final String SELECT_AND_LOCK_NEXT_TASK_QUERY = """
      select * from task_entity
      WHERE status = 'WAITING'
      AND ref_id not in (select ref_id from task_entity where status = 'RUNNING')
      ORDER BY id ASC
      LIMIT 1
      FOR UPDATE SKIP LOCKED
      """.replace("\n", " ");
  private static final Logger LOG = LoggerFactory.getLogger(TaskDao.class);

  private final DatabaseOrm databaseOrm;
  private final DatabaseClient databaseClient;
  private final GenericResultSetMapper genericResultSetMapper;

  @Inject
  public TaskDao(final DatabaseOrm databaseOrm,
      final DatabaseClient DatabaseClient,
      final GenericResultSetMapper genericResultSetMapper) {
    this.databaseOrm = databaseOrm;
    this.databaseClient = DatabaseClient;
    this.genericResultSetMapper = genericResultSetMapper;
  }

  private TaskEntity toEntity(final TaskDTO dto)
      throws JsonProcessingException {
    TaskEntity entity = TaskEntityMapper.INSTANCE.toTaskEntity(dto);
    final String jsonVal = toJsonString(dto);
    entity.setJsonVal(jsonVal);
    final Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());
    entity.setCreateTime(currentTime);
    entity.setUpdateTime(currentTime);
    entity.setVersion(dto.getVersion() == 0 ? 1 : dto.getVersion());
    return entity;
  }

  private TaskDTO toDto(final TaskEntity entity) throws JsonProcessingException {
    TaskDTO dto = Constants.TEMPLATABLE_OBJECT_MAPPER.readValue(entity.getJsonVal(), TaskDTO.class);
    dto.setId(entity.getId());
    dto.setCreateTime(entity.getCreateTime());
    dto.setUpdateTime(entity.getUpdateTime());
    dto.setVersion(entity.getVersion());
    return dto;
  }

  private List<TaskDTO> toDto(final List<TaskEntity> entities) throws JsonProcessingException {
    final List<TaskDTO> ret = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(entities)) {
      for (final TaskEntity entity : entities) {
        final TaskDTO dto = toDto(entity);
        ret.add(dto);
      }
    }
    return ret;
  }

  private String toJsonString(final TaskDTO dto) throws JsonProcessingException {
    return Constants.TEMPLATABLE_OBJECT_MAPPER.writeValueAsString(dto);
  }

  public Long put(final TaskDTO pojo) {
    if(pojo.getId() != null) {
      return null;
    }
    try {
      final TaskEntity entity = toEntity(pojo);
      return databaseClient.executeTransaction(
          (connection) -> databaseOrm.save(entity, connection));
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return null;
    }
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
  public int update(final List<TaskDTO> pojos) {
    if (CollectionUtils.isEmpty(pojos)) {
      return 0;
    }
    int updateCounter = 0;
    for (final TaskDTO pojo : pojos) {
      Preconditions.checkNotNull(pojo.getId());
      try {
        updateCounter += update(pojo);
      } catch (final Exception e) {
        LOG.error("Could not update entity : {}", pojo, e);
      }
    }
    return updateCounter;
  }

  public int update(final TaskDTO pojo) {
    if (pojo.getId() == null) {
      throw new IllegalArgumentException(String.format("Need an ID to update the Task entity: %s",
          pojo));
    }
    return update(pojo, null);
  }

  public int update(final TaskDTO pojo, final Predicate predicate) {
    try {
      final TaskEntity entity = toEntity(pojo);
      return databaseClient.executeTransaction(
          (connection) -> databaseOrm.update(entity, predicate, connection));
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return 0;
    }
  }

  public List<TaskDTO> list(final long limit, final long offset) {
    try {
      final List<TaskEntity> entities = databaseClient.executeTransaction(
          (connection) -> databaseOrm.findAll(null,
              limit, offset, TaskEntity.class, connection));
      return toDto(entities);
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return Collections.emptyList();
    }
  }

  public TaskDTO get(final Long id) {
    try {
      final TaskEntity entity = databaseClient.executeTransaction(
          (connection) -> databaseOrm.find(id, TaskEntity.class, connection));
      if (entity == null) {
        return null;
      }
      return toDto(entity);
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return null;
    }
  }


  public List<TaskDTO> get(final List<Long> idList) {
    return get(
        Predicate.IN("id", idList.toArray())
    );
  }

  public List<TaskDTO> filter(final DaoFilter daoFilter) {
    requireNonNull(daoFilter.getPredicate(),
        "If the predicate is null, you can just do "
            + "getAll() which doesn't need to fetch IDs first");
    return get(daoFilter.getPredicate(), daoFilter.getLimit(), daoFilter.getTransactionIsolationLevel());
  }

  public List<TaskDTO> get(final Predicate predicate) {
    return get(predicate, null);
  }
  
  public List<TaskDTO> get(final Predicate predicate, final @Nullable Long limit) {
    return get(predicate, limit, null);
  }

  public List<TaskDTO> get(final Predicate predicate, final @Nullable Long limit, final @Nullable Integer transactionIsolationLevel) {
    try {
      final List<TaskEntity> entities = databaseClient.executeTransaction(
          (connection) -> {
            if (transactionIsolationLevel != null) {
              connection.setTransactionIsolation(transactionIsolationLevel);
            }
            return databaseOrm.findAll(
                predicate, limit, null, TaskEntity.class, connection);
          });
      return toDto(entities);
    } catch (final Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return Collections.emptyList();
    }
  }

  public long count() {
    try {
      return databaseClient.executeTransaction(
          (connection) -> {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            return databaseOrm.count(null, TaskEntity.class, connection);
          });
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return 0;
    }
  }

  public long count(final Predicate predicate) {
    try {
      return databaseClient.executeTransaction(
          (connection) -> {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            return databaseOrm.count(predicate, TaskEntity.class, connection);
          });
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return 0;
    }
  }

  /**
   * @param parameterizedSQL second part of the sql (omit select from table section)
   */
  public List<TaskDTO> executeParameterizedSQL(final String parameterizedSQL,
      final Map<String, Object> parameterMap) {
    try {
      final List<TaskEntity> entities = databaseClient.executeTransaction(
          (connection) -> databaseOrm.runSQL(
              parameterizedSQL,
              parameterMap,
              TaskEntity.class,
              connection));
      return toDto(entities);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return Collections.emptyList();
    }
  }

  public TaskDTO acquireNextTaskToRun(final long workerId) throws Exception {
    return databaseClient.executeTransaction(
        connection -> {
          connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
          try (final Statement s = connection.createStatement();
              final ResultSet rs = s.executeQuery(SELECT_AND_LOCK_NEXT_TASK_QUERY)
          ) {
            final List<TaskEntity> res = genericResultSetMapper.mapAll(rs, TaskEntity.class);
            if (res.size() == 1) {
              final List<TaskDTO> dtos = toDto(res);
              final TaskDTO toUpdate = dtos.get(0);
              toUpdate.setStatus(TaskStatus.RUNNING);
              toUpdate.setWorkerId(workerId);
              toUpdate.setStartTime(System.currentTimeMillis());
              toUpdate.setVersion(toUpdate.getVersion() + 1);
              toUpdate.setLastActive(new Timestamp(System.currentTimeMillis()));
              final int success = databaseOrm.update(toEntity(toUpdate), null, connection);
              if (success == 1) {
                return toUpdate;
              } else {
                throw new RuntimeException("Failed to acquire task. Failed to update the task, even though it is locked by this SQL transaction. Please reach out to StarTree support. Task id: " + toUpdate.getId());
              }
            } else if (res.isEmpty()) {
              // no task to run
              return null;
            } else {
              throw new RuntimeException(
                  "Failed to acquire task. Query returned multiple rows. Only one row was expected. Please reach out to StarTree support.");
            }
          }
        });
  }

  public int delete(final Long id) {
    return delete(List.of(id));
  }

  public int delete(final List<Long> idsToDelete) {
    return deleteByPredicate(Predicate.IN("id", idsToDelete.toArray()));
  }

  public int deleteByPredicate(final Predicate predicate) {
    try {
      return databaseClient.executeTransaction(
          (connection) -> databaseOrm.delete(predicate, TaskEntity.class, connection));
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      // TODO CYRIL design - surface exception ?
      return 0;
    }
  }
}
