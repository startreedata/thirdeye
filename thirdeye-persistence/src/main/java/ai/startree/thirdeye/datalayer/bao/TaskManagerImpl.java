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
package ai.startree.thirdeye.datalayer.bao;

import static ai.startree.thirdeye.spi.Constants.METRICS_CACHE_TIMEOUT;
import static ai.startree.thirdeye.spi.Constants.TASK_EXPIRY_DURATION;
import static ai.startree.thirdeye.spi.Constants.TASK_MAX_DELETES_PER_CLEANUP;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Suppliers.memoizeWithExpiration;

import ai.startree.thirdeye.datalayer.dao.TaskDao;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AuthorizationConfigurationDTO;
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
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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

  @Override
  public TaskDTO createTaskDto(final TaskInfo taskInfo, final TaskType taskType, final
  AuthorizationConfigurationDTO auth)
      throws JsonProcessingException {
    final String taskInfoJson = OBJECT_MAPPER.writeValueAsString(taskInfo);

    final TaskDTO task = new TaskDTO()
        .setTaskType(taskType)
        .setJobName(taskType.toString() + "_" + taskInfo.getRefId())
        .setStatus(TaskStatus.WAITING)
        .setTaskInfo(taskInfoJson)
        .setRefId(taskInfo.getRefId());
    task.setAuth(auth);
    save(task);
    return task;
  }

  @Override
  public boolean isAlreadyRunning(final String taskName) {
    final List<TaskDTO> scheduledTasks = findByPredicate(Predicate.AND(
        Predicate.EQ("name", taskName),
        Predicate.OR(
            Predicate.EQ("status", TaskStatus.RUNNING.toString()),
            Predicate.EQ("status", TaskStatus.WAITING.toString())
        ))
    );
    return !scheduledTasks.isEmpty();
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

  // TODO CYRIL NOTE - RETRY IS NOT IMPLEMENTED BUT IT SHOULD BE EASY BY ACCEPTING STATUS = FAILED IN THE 2 METHODS BELOW AND PUTTING A LIMIT ON THE VALUE OF VERSION
  @Override
  public TaskDTO findNextTaskToRun() {
    final String queryClause = """
        WHERE status = 'WAITING'
        AND ref_id not in (select ref_id from task_entity where status = 'RUNNING')
        ORDER BY create_time ASC LIMIT 1
        """;
    final List<TaskDTO> dtos = dao.executeParameterizedSQL(queryClause, Collections.emptyMap());
    if (dtos.isEmpty()) {
      return null;
    }
    return dtos.get(0);
  }

  /**
   * This method has side effects on the task DTO, even if the acquisition attempt fails.
   * Re-fetch the taskDto if you need to ensure consistency with the persistence layer.
   */
  @Override
  public boolean acquireTaskToRun(final TaskDTO task, final long workerId) {
    task.setStatus(TaskStatus.RUNNING);
    task.setWorkerId(workerId);
    task.setStartTime(System.currentTimeMillis());
    final int currentVersion = task.getVersion();
    task.setVersion(currentVersion + 1);
    final Predicate predicate = Predicate.AND(
        Predicate.EQ("version", currentVersion),
        Predicate.EQ("status", TaskStatus.WAITING.toString())
    );
    return dao.update(task, predicate) == 1;
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
  public List<TaskDTO> findByStatusAndWorkerId(final Long workerId, final TaskStatus status) {
    final Predicate statusPredicate = Predicate.EQ("status", status.toString());
    final Predicate workerIdPredicate = Predicate.EQ("workerId", workerId);
    return findByPredicate(Predicate.AND(statusPredicate, workerIdPredicate));
  }

  public void purge(@Nullable final Duration expiryDurationOptional,
      @Nullable final Integer limitOptional) {
    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    final Duration expiryDuration = optional(expiryDurationOptional).orElse(TASK_EXPIRY_DURATION);
    final long twoMonthsBack = System.currentTimeMillis() - expiryDuration.toMillis();
    final String formattedDate = df.format(new Date(twoMonthsBack));

    final int limit = optional(limitOptional).orElse(TASK_MAX_DELETES_PER_CLEANUP);

    final long startTime = System.nanoTime();
    final List<TaskDTO> tasksToBeDeleted = filter(new DaoFilter()
        .setPredicate(Predicate.LT("createTime", formattedDate))
        .setLimit((long) limit)
    );

    /* Delete each task */
    tasksToBeDeleted.forEach(this::delete);

    final double totalTime = (System.nanoTime() - startTime) / 1e9;

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

  public long countBy(final TaskStatus status, final TaskType type) {
    return count(Predicate.AND(
        Predicate.EQ("status", status.toString()),
        Predicate.EQ("type", type)));
  }

  private void registerMetrics() {
    // deprecated - use thirdeye_tasks
    metricRegistry.register("taskCountTotal",
        new CachedGauge<Long>(METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES) {
          @Override
          protected Long loadValue() {
            return count();
          }
        });

    // deprecated - use thirdeye_task_latency
    metricRegistry.register("detectionTaskLatencyInMillis",
        new CachedGauge<Long>(METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES) {
          @Override
          protected Long loadValue() {
            return getTaskLatency(TaskType.DETECTION);
          }
        });

    // deprecated - use thirdeye_task_latency
    metricRegistry.register("notificationTaskLatencyInMillis",
        new CachedGauge<Long>(METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES) {
          @Override
          protected Long loadValue() {
            return getTaskLatency(TaskType.NOTIFICATION);
          }
        });

    for (final TaskStatus status : TaskStatus.values()) {
      registerStatusMetric(status);
    }

    for (final TaskType type : TaskType.values()) {
      Gauge.builder("thirdeye_task_latency",
              memoizeWithExpiration(() -> getTaskLatency(type), 1, TimeUnit.MINUTES))
          .register(Metrics.globalRegistry);
      for (final TaskStatus status : TaskStatus.values()) {
        Gauge.builder("thirdeye_tasks",
                memoizeWithExpiration(() -> countBy(status, type), 1, TimeUnit.MINUTES))
            .tag("status", status.toString())
            .tags("type", type.toString())
            .register(Metrics.globalRegistry);
      }
    }
  }

  // FIXME CYRIL - this should have as less cache as possible and as precise as possible - so it should be a direct query to the index table
  private long getTaskLatency(final TaskType type) {
    // fetch pending tasks from DB of the given type
    final List<TaskStatus> pendingStatus = List.of(TaskStatus.WAITING, TaskStatus.RUNNING);
    final DaoFilter filter = new DaoFilter()
        .setPredicate(Predicate.AND(
            Predicate.IN("status", pendingStatus.toArray()),
            Predicate.EQ("type", type)
        ));
    final long currentTime = System.currentTimeMillis();
    return filter(filter).stream()
        // Calculate latency as max value of (current time) - (task creation time) from the filtered tasks
        .map(task -> currentTime - task.getCreateTime().getTime())
        .max(Long::compare).orElse(0L);
  }

  private void registerStatusMetric(final TaskStatus status) {
    // deprecated - use thirdeye_tasks
    metricRegistry.register(String.format("taskCount_%s", status),
        new CachedGauge<Long>(METRICS_CACHE_TIMEOUT.toMinutes(), TimeUnit.MINUTES) {
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
