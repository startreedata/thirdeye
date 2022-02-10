/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TaskManagerImpl extends AbstractManagerImpl<TaskDTO> implements TaskManager {

  private static final String FIND_BY_STATUS_ORDER_BY_CREATE_TIME_ASC =
      " WHERE status = :status order by startTime asc limit 10";

  private static final String FIND_BY_STATUS_ORDER_BY_CREATE_TIME_DESC =
      " WHERE status = :status order by startTime desc limit 10";

  private static final String FIND_BY_NAME_ORDER_BY_CREATE_TIME_ASC =
      " WHERE name = :name order by createTime asc limit ";

  private static final String FIND_BY_NAME_ORDER_BY_CREATE_TIME_DESC =
      " WHERE name = :name order by createTime desc limit ";

  private static final Logger LOG = LoggerFactory.getLogger(TaskManagerImpl.class);

  @Inject
  public TaskManagerImpl(final GenericPojoDao genericPojoDao) {
    super(TaskDTO.class, genericPojoDao);
  }

  @Override
  public Long save(final TaskDTO entity) {
    if (entity.getId() != null) {
      //TODO: throw exception and force the caller to call update instead
      update(entity);
      return entity.getId();
    }
    final Long id = genericPojoDao.put(entity);
    entity.setId(id);
    return id;
  }

  @Override
  public List<TaskDTO> findByJobIdStatusNotIn(final Long jobId, final TaskStatus status) {
    final Predicate jobIdPredicate = Predicate.EQ("jobId", jobId);
    final Predicate statusPredicate = Predicate.NEQ("status", status.toString());
    final Predicate predicate = Predicate.AND(statusPredicate, jobIdPredicate);
    return findByPredicate(predicate);
  }

  @Override
  public List<TaskDTO> findByNameOrderByCreateTime(
      final String name, final int fetchSize, final boolean asc) {
    final Map<String, Object> parameterMap = new HashMap<>();
    parameterMap.put("name", name);
    final String queryClause = (asc)
        ? FIND_BY_NAME_ORDER_BY_CREATE_TIME_ASC + fetchSize
        : FIND_BY_NAME_ORDER_BY_CREATE_TIME_DESC + fetchSize;
    return genericPojoDao.executeParameterizedSQL(queryClause, parameterMap, TaskDTO.class);
  }

  @Override
  public List<TaskDTO> findByStatusOrderByCreateTime(final TaskStatus status, final int fetchSize,
      final boolean asc) {
    final Map<String, Object> parameterMap = new HashMap<>();
    parameterMap.put("status", status.toString());
    final String queryClause =
        (asc) ? FIND_BY_STATUS_ORDER_BY_CREATE_TIME_ASC : FIND_BY_STATUS_ORDER_BY_CREATE_TIME_DESC;
    return genericPojoDao.executeParameterizedSQL(queryClause, parameterMap, TaskDTO.class);
  }

  @Override
  public boolean updateStatusAndWorkerId(final Long workerId, final Long id,
      final Set<TaskStatus> permittedOldStatus,
      final int expectedVersion) {
    final TaskDTO task = findById(id);
    if (permittedOldStatus.contains(task.getStatus())) {
      task.setStatus(TaskStatus.RUNNING);
      task.setWorkerId(workerId);
      task.setStartTime(System.currentTimeMillis());
      //increment the version
      task.setVersion(expectedVersion + 1);
      final Predicate predicate = Predicate.AND(
          Predicate.EQ("id", id),
          Predicate.EQ("version", expectedVersion));
      final int update = update(task, predicate);
      return update == 1;
    } else {
      return false;
    }
  }

  @Override
  public void updateStatusAndTaskEndTime(final Long id, final TaskStatus oldStatus,
      final TaskStatus newStatus,
      final Long taskEndTime, final String message) {
    final TaskDTO task = findById(id);
    if (task.getStatus().equals(oldStatus)) {
      task.setStatus(newStatus);
      task.setEndTime(taskEndTime);
      task.setMessage(message);
      save(task);
    }
  }

  @Override
  public void updateTaskStartTime(final Long id, final Long taskStartTime) {
    final TaskDTO task = findById(id);
    task.setStartTime(taskStartTime);
    save(task);
  }

  @Override
  @Transactional
  public int deleteRecordsOlderThanDaysWithStatus(final int days, final TaskStatus status) {
    final DateTime expireDate = new DateTime().minusDays(days);
    final Timestamp expireTimestamp = new Timestamp(expireDate.getMillis());

    final Predicate timestampPredicate = Predicate.LT("createTime", expireTimestamp);
    final Predicate statusPredicate = Predicate.EQ("status", status.toString());
    return deleteByPredicate(Predicate.AND(statusPredicate, timestampPredicate));
  }

  @Override
  @Transactional
  public List<TaskDTO> findByStatusNotIn(final TaskStatus status) {
    final Predicate statusPredicate = Predicate.NEQ("status", status.toString());
    return findByPredicate(statusPredicate);
  }

  @Override
  public List<TaskDTO> findByStatusWithinDays(final TaskStatus status, final int days) {
    final DateTime activeDate = new DateTime().minusDays(days);
    final Timestamp activeTimestamp = new Timestamp(activeDate.getMillis());
    final Predicate statusPredicate = Predicate.EQ("status", status.toString());
    final Predicate timestampPredicate = Predicate.GE("createTime", activeTimestamp);
    return findByPredicate(Predicate.AND(statusPredicate, timestampPredicate));
  }

  @Override
  public List<TaskDTO> findByStatusesAndTypeWithinDays(final List<TaskStatus> statuses,
      final TaskType type, final int days) {
    final DateTime activeDate = new DateTime().minusDays(days);
    final Timestamp activeTimestamp = new Timestamp(activeDate.getMillis());
    final Predicate statusPredicate = Predicate
        .IN("status", statuses.stream().map(Enum::toString).toArray());
    final Predicate typePredicate = Predicate.EQ("type", type.toString());
    final Predicate timestampPredicate = Predicate.GE("createTime", activeTimestamp);
    return findByPredicate(Predicate.AND(statusPredicate, typePredicate, timestampPredicate));
  }

  @Override
  public List<TaskDTO> findTimeoutTasksWithinDays(final int days, final long maxTaskTime) {
    final DateTime activeDate = new DateTime().minusDays(days);
    final Timestamp activeTimestamp = new Timestamp(activeDate.getMillis());
    final DateTime timeoutDate = new DateTime().minus(maxTaskTime);
    final Timestamp timeoutTimestamp = new Timestamp(timeoutDate.getMillis());
    final Predicate statusPredicate = Predicate.EQ("status", TaskStatus.RUNNING.toString());
    final Predicate daysTimestampPredicate = Predicate.GE("createTime", activeTimestamp);
    final Predicate timeoutTimestampPredicate = Predicate.LT("updateTime", timeoutTimestamp);
    return findByPredicate(
        Predicate.AND(statusPredicate, daysTimestampPredicate, timeoutTimestampPredicate));
  }

  @Override
  public List<TaskDTO> findByStatusAndWorkerId(final Long workerId, final TaskStatus status) {
    final Predicate statusPredicate = Predicate.EQ("status", status.toString());
    final Predicate workerIdPredicate = Predicate.EQ("workerId", workerId);
    return findByPredicate(Predicate.AND(statusPredicate, workerIdPredicate));
  }
}
