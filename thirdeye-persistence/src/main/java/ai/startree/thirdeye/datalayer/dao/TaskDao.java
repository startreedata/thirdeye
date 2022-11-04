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

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datalayer.DatabaseService;
import ai.startree.thirdeye.datalayer.DatabaseTransactionService;
import ai.startree.thirdeye.datalayer.entity.TaskEntity;
import ai.startree.thirdeye.datalayer.mapper.TaskEntityMapper;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TaskDao {

  private static final Logger LOG = LoggerFactory.getLogger(TaskDao.class);
  private static final boolean IS_DEBUG = LOG.isDebugEnabled();
  private static final ObjectMapper OBJECT_MAPPER = ThirdEyeSerialization.getObjectMapper();

  private final DatabaseService databaseService;
  private final DatabaseTransactionService transactionService;

  @Inject
  public TaskDao(final DatabaseService databaseService,
      final DatabaseTransactionService transactionService) {
    this.databaseService = databaseService;
    this.transactionService = transactionService;
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
    TaskDTO dto = OBJECT_MAPPER.readValue(entity.getJsonVal(), TaskDTO.class);
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
    return OBJECT_MAPPER.writeValueAsString(dto);
  }

  public Long put(final TaskDTO pojo) {
    if(pojo.getId() != null) {
      return null;
    }
    try {
      final TaskEntity entity = toEntity(pojo);
      return transactionService.executeTransaction(
          (connection) -> databaseService.save(entity, connection),
          null);
    } catch (JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
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
      return transactionService.executeTransaction(
          (connection) -> databaseService.update(entity, predicate, connection),
          null);
    } catch (JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return 0;
    }
  }

  public List<TaskDTO> getAll() {
    try {
      final List<TaskEntity> entities = transactionService.executeTransaction(
          (connection) -> databaseService.findAll(null,
              null, null, TaskEntity.class, connection), Collections.emptyList());
      return toDto(entities);
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  public List<TaskDTO> list(final long limit, final long offset) {
    try {
      final List<TaskEntity> entities = transactionService.executeTransaction(
          (connection) -> databaseService.findAll(null,
              limit, offset, TaskEntity.class, connection), Collections.emptyList());
      return toDto(entities);
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  public TaskDTO get(final Long id) {
    try {
      final TaskEntity entity = transactionService.executeTransaction(
          (connection) -> databaseService.find(id, TaskEntity.class, connection),
          null);
      if (entity == null) {
        return null;
      }
      return toDto(entity);
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
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
    return get(daoFilter.getPredicate());
  }

  public List<TaskDTO> get(final Map<String, Object> filterParams) {
    final Predicate[] childPredicates = new Predicate[filterParams.size()];
    int index = 0;
    for (final Entry<String, Object> entry : filterParams.entrySet()) {
      childPredicates[index] = Predicate.EQ(entry.getKey(), entry.getValue());
      index = index + 1;
    }
    return get(Predicate.AND(childPredicates));
  }

  public List<TaskDTO> get(final Predicate predicate) {
    try {
      final List<TaskEntity> entities = transactionService.executeTransaction(
          (connection) -> databaseService.findAll(
              predicate, null, null, TaskEntity.class, connection),
          Collections.emptyList());
      return toDto(entities);
    } catch (final JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  public long count() {
    try {
      return transactionService.executeTransaction(
          (connection) -> databaseService.count(null, TaskEntity.class, connection),
          0L);
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
      return 0;
    }
  }

  public long count(final Predicate predicate) {
    try {
      return transactionService.executeTransaction(
          (connection) -> databaseService.count(predicate, TaskEntity.class, connection),
          0L);
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
      return 0;
    }
  }

  /**
   * @param parameterizedSQL second part of the sql (omit select from table section)
   */
  public List<TaskDTO> executeParameterizedSQL(final String parameterizedSQL,
      final Map<String, Object> parameterMap) {
    try {
      final List<TaskEntity> entities = transactionService.executeTransaction(
          (connection) -> databaseService.runSQL(
              parameterizedSQL,
              parameterMap,
              TaskEntity.class,
              connection), Collections.emptyList());
      return toDto(entities);
    } catch (JsonProcessingException | SQLException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    }
  }

  /**
   * Dump all entities of type entityClass to logger
   * This utility is useful to dump the entire table. However, it gets executed in code regularly in
   * debug mode.
   */
  @SuppressWarnings("unused")
  private void dumpTable() {
    if (IS_DEBUG) {
      try {
        final List<TaskEntity> entities = transactionService.executeTransaction(
            (connection) -> databaseService.findAll(
                null, null, null, TaskEntity.class, connection),
            Collections.emptyList());
        for (final TaskEntity entity : entities) {
          LOG.debug("{}", entity);
        }
      } catch (SQLException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  public int delete(final Long id) {
    return delete(List.of(id));
  }

  public int delete(final List<Long> idsToDelete) {
    return deleteByPredicate(Predicate.IN("id", idsToDelete.toArray()));
  }

  public int deleteByPredicate(final Predicate predicate) {
    try {
      return transactionService.executeTransaction(
          (connection) -> databaseService.delete(predicate, TaskEntity.class, connection),
          0);
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
      return 0;
    }
  }
}
