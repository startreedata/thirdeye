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
package ai.startree.thirdeye.datalayer.bao;

import static ai.startree.thirdeye.spi.Constants.TASK_EXPIRY_DURATION;
import static ai.startree.thirdeye.spi.Constants.TASK_MAX_DELETES_PER_CLEANUP;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.datalayer.dao.TaskDao;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TaskManagerImpl implements TaskManager {

  private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final TaskDao dao;

  private static final String FIND_BY_STATUS_ORDER_BY_CREATE_TIME_ASC =
      " WHERE status = :status order by startTime asc limit ";

  private static final String FIND_BY_STATUS_ORDER_BY_CREATE_TIME_DESC =
      " WHERE status = :status order by startTime desc limit ";

  private static final String FIND_BY_NAME_ORDER_BY_CREATE_TIME_ASC =
      " WHERE name = :name order by createTime asc limit ";

  private static final String FIND_BY_NAME_ORDER_BY_CREATE_TIME_DESC =
      " WHERE name = :name order by createTime desc limit ";

  private static Timestamp taskLatencyWindowStart = new Timestamp(System.currentTimeMillis());

  private static final Logger LOG = LoggerFactory.getLogger(TaskManagerImpl.class);

  private final Meter orphanTasksCount;
  private final MetricRegistry metricRegistry;

  @Inject
  public TaskManagerImpl(final TaskDao dao,
      final MetricRegistry metricRegistry) {
    this.dao = dao;
    orphanTasksCount = metricRegistry.meter("orphanTasksCount");
    this.metricRegistry = metricRegistry;
    registerMetrics();
  }

  public TaskDTO createTaskDto(final long alertId, final TaskInfo taskInfo, final TaskType taskType)
      throws JsonProcessingException {
    final String taskInfoJson;
    taskInfoJson = OBJECT_MAPPER.writeValueAsString(taskInfo);

    TaskDTO task = new TaskDTO()
        .setTaskType(taskType)
        .setJobName(taskType.toString() + "_" + alertId)
        .setStatus(TaskStatus.WAITING)
        .setTaskInfo(taskInfoJson);
    save(task);
    return task;
  }

  @Override
  public Long save(final TaskDTO entity) {
    if (entity.getId() != null) {
      //TODO: throw exception and force the caller to call update instead
      update(entity);
      return entity.getId();
    }
    final Long id = dao.put(entity);
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
    return dao.executeParameterizedSQL(queryClause, parameterMap);
  }

  @Override
  public List<TaskDTO> findByStatusOrderByCreateTime(final TaskStatus status, final int fetchSize,
      final boolean asc) {
    final Map<String, Object> parameterMap = new HashMap<>();
    parameterMap.put("status", status.toString());
    final String queryClause = (asc)
        ? FIND_BY_STATUS_ORDER_BY_CREATE_TIME_ASC + fetchSize
        : FIND_BY_STATUS_ORDER_BY_CREATE_TIME_DESC + fetchSize;
    return dao.executeParameterizedSQL(queryClause, parameterMap);
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
      final Predicate predicate = Predicate.EQ("version", expectedVersion);
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
  public void updateLastActive(final Long id) {
    final TaskDTO task = findById(id);
    task.setLastActive(new Timestamp(System.currentTimeMillis()));
    save(task);
  }

  @Override
  @Transactional
  public int deleteRecordsOlderThanDaysWithStatus(final int days, final TaskStatus status) {
    final DateTime expireDate = new DateTime(DateTimeZone.UTC).minusDays(days);
    final Timestamp expireTimestamp = new Timestamp(expireDate.getMillis());

    final Predicate timestampPredicate = Predicate.LT("createTime", expireTimestamp);
    final Predicate statusPredicate = Predicate.EQ("status", status.toString());
    return deleteByPredicate(Predicate.AND(statusPredicate, timestampPredicate));
  }

  @Override
  public List<TaskDTO> findByStatusWithinDays(final TaskStatus status, final int days) {
    final DateTime activeDate = new DateTime(DateTimeZone.UTC).minusDays(days);
    final Timestamp activeTimestamp = new Timestamp(activeDate.getMillis());
    final Predicate statusPredicate = Predicate.EQ("status", status.toString());
    final Predicate timestampPredicate = Predicate.GE("createTime", activeTimestamp);
    return findByPredicate(Predicate.AND(statusPredicate, timestampPredicate));
  }

  @Override
  public List<TaskDTO> findByStatusesAndTypeWithinDays(final List<TaskStatus> statuses,
      final TaskType type, final int days) {
    final DateTime activeDate = new DateTime(DateTimeZone.UTC).minusDays(days);
    final Timestamp activeTimestamp = new Timestamp(activeDate.getMillis());
    final Predicate statusPredicate = Predicate
        .IN("status", statuses.stream().map(Enum::toString).toArray());
    final Predicate typePredicate = Predicate.EQ("type", type.toString());
    final Predicate timestampPredicate = Predicate.GE("createTime", activeTimestamp);
    return findByPredicate(Predicate.AND(statusPredicate, typePredicate, timestampPredicate));
  }

  @Override
  public List<TaskDTO> findTimeoutTasksWithinDays(final int days, final long maxTaskTime) {
    final DateTime activeDate = new DateTime(DateTimeZone.UTC).minusDays(days);
    final Timestamp activeTimestamp = new Timestamp(activeDate.getMillis());
    final DateTime timeoutDate = new DateTime(DateTimeZone.UTC).minus(maxTaskTime);
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

  public void purge(@Nullable Duration expiryDurationOptional, @Nullable Integer limitOptional) {
    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    final Duration expiryDuration = optional(expiryDurationOptional).orElse(TASK_EXPIRY_DURATION);
    final long twoMonthsBack = System.currentTimeMillis() - expiryDuration.toMillis();
    final String formattedDate = df.format(new Date(twoMonthsBack));

    final int limit = optional(limitOptional).orElse(TASK_MAX_DELETES_PER_CLEANUP);

    final long startTime = System.nanoTime();
    final List<TaskDTO> tasksToBeDeleted = filter(new DaoFilter()
        .setPredicate(Predicate.LT("createTime", formattedDate))
        .setLimit(limit)
    );

    /* Delete each task */
    tasksToBeDeleted.forEach(this::delete);

    double totalTime = (System.nanoTime() - startTime) / 1e9;

    LOG.info(String.format("Task cleanup complete. removed %d tasks. (time taken: %.2fs)",
        tasksToBeDeleted.size(),
        totalTime));
  }

  @Override
  public void orphanTaskCleanUp(final Timestamp activeThreshold) {
    final long current = System.currentTimeMillis();
    findByPredicate(
        Predicate.AND(
            Predicate.EQ("status", TaskStatus.RUNNING.toString()),
            Predicate.LT("lastActive", activeThreshold)
        )
    ).forEach(task -> {
          updateStatusAndTaskEndTime(
              task.getId(),
              TaskStatus.RUNNING,
              TaskStatus.FAILED,
              current,
              String.format("Orphan Task. Worker id : %s", task.getWorkerId())
          );
          orphanTasksCount.mark();
        }
    );
  }

  public long countByStatus(final TaskStatus status) {
    return count(Predicate.EQ("status", status.toString()));
  }

  private void registerMetrics() {
    metricRegistry.register("taskCountTotal", new CachedGauge<Long>(1, TimeUnit.MINUTES) {
      @Override
      protected Long loadValue() {
        return count();
      }
    });

    metricRegistry.register("taskLatencyInMillis", new CachedGauge<Long>(15, TimeUnit.SECONDS) {
      @Override
      protected Long loadValue() {
        final List<TaskStatus> pendingStatus = List.of(TaskStatus.WAITING, TaskStatus.RUNNING);
        final Timestamp taskLatencyWindowEnd = new Timestamp(System.currentTimeMillis());
        final DaoFilter filter = new DaoFilter()
            .setPredicate(Predicate.OR(
                // tasks which are still active in the lifecycle
                Predicate.IN("status", pendingStatus.toArray()),
                // tasks which completed(updated with the final status) lifecycle in the window
                Predicate.AND(
                    Predicate.GE("updateTime", taskLatencyWindowStart),
                    Predicate.LT("updateTime", taskLatencyWindowEnd)
                )
            ));
        final List<TaskDTO> tasks = filter(filter);
        if(tasks.isEmpty()) {
          return 0L;
        }
        final long maxLatency = tasks.stream().map(task -> {
          if(pendingStatus.contains(task.getStatus())) {
            // Time spent by active tasks in the system. From creation till now
            return (taskLatencyWindowEnd.getTime() - task.getCreateTime().getTime());
          } else {
            // Time spent by fulfilled tasks. From creation time to last updated time
            return (task.getUpdateTime().getTime() - task.getCreateTime().getTime());
          }
        }).max(Long::compare).get();
        // move the latency calculation window
        taskLatencyWindowStart = taskLatencyWindowEnd;
        return maxLatency;
      }
    });

    for (TaskStatus status : TaskStatus.values()) {
      registerStatusMetric(status);
    }
  }

  private void registerStatusMetric(final TaskStatus status) {
    metricRegistry.register(String.format("taskCount_%s", status.toString()),
        new CachedGauge<Long>(1, TimeUnit.MINUTES) {
          @Override
          protected Long loadValue() {
            return countByStatus(status);
          }
        });
  }

  @Override
  public int update(final TaskDTO entity, final Predicate predicate) {
    return dao.update(entity, predicate);
  }

  @Override
  public int update(final TaskDTO entity) {
    return dao.update(entity);
  }

  // Test is located at TestAlertConfigManager.testBatchUpdate()
  @Override
  public int update(final List<TaskDTO> entities) {
    return dao.update(entities);
  }

  @Override
  public TaskDTO findById(final Long id) {
    return dao.get(id);
  }

  @Override
  public List<TaskDTO> findByIds(final List<Long> ids) {
    return dao.get(ids);
  }

  @Override
  public List<TaskDTO> findByName(final String name) {
    return findByPredicate(Predicate.EQ("name", name));
  }

  @Override
  public int delete(final TaskDTO entity) {
    return dao.delete(entity.getId());
  }

  @Override
  public int deleteById(final Long id) {
    return dao.delete(id);
  }

  @Override
  public int deleteByIds(final List<Long> ids) {
    return dao.delete(ids);
  }

  @Override
  public int deleteByPredicate(final Predicate predicate) {
    return dao.deleteByPredicate(predicate);
  }

  @Override
  @Transactional
  public int deleteRecordsOlderThanDays(final int days) {
    final DateTime expireDate = new DateTime(DateTimeZone.UTC).minusDays(days);
    final Timestamp expireTimestamp = new Timestamp(expireDate.getMillis());
    final Predicate timestampPredicate = Predicate.LT("createTime", expireTimestamp);
    return deleteByPredicate(timestampPredicate);
  }

  @Override
  public List<TaskDTO> findAll() {
    return dao.getAll();
  }

  @Override
  public List<TaskDTO> findByParams(final Map<String, Object> filters) {
    return dao.get(filters);
  }

  @Override
  public List<TaskDTO> findByPredicate(final Predicate predicate) {
    return dao.get(predicate);
  }

  @Override
  public List<TaskDTO> filter(final DaoFilter daoFilter) {
    return dao.filter(daoFilter);
  }

  @Override
  public long count() {
    return dao.count();
  }

  @Override
  public long count(final Predicate predicate) {
    return dao.count(predicate);
  }
}
