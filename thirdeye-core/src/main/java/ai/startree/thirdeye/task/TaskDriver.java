/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.detection.anomaly.utils.AnomalyUtils;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TaskDriver {

  private static final Logger LOG = LoggerFactory.getLogger(TaskDriver.class);

  private final ExecutorService taskExecutorService;
  private final ExecutorService taskWatcherExecutorService;
  private final ScheduledExecutorService heartbeatExecutorService;

  private final TaskManager taskManager;
  private final TaskContext taskContext;
  private final TaskDriverConfiguration config;
  private final Long workerId;
  private final AtomicBoolean shutdown = new AtomicBoolean(false);
  private final TaskRunnerFactory taskRunnerFactory;
  private final MetricRegistry metricRegistry;

  @Inject
  public TaskDriver(final ThirdEyeServerConfiguration thirdEyeServerConfiguration,
      final TaskManager taskManager,
      final TaskRunnerFactory taskRunnerFactory,
      final MetricRegistry metricRegistry) {
    this.taskManager = taskManager;
    this.metricRegistry = metricRegistry;
    config = thirdEyeServerConfiguration.getTaskDriverConfiguration();
    workerId = fetchWorkerId(config);

    taskExecutorService = Executors.newFixedThreadPool(
        config.getMaxParallelTasks(),
        new ThreadFactoryBuilder()
            .setNameFormat("task-executor-%d")
            .build());

    taskWatcherExecutorService = Executors.newFixedThreadPool(
        config.getMaxParallelTasks(),
        new ThreadFactoryBuilder()
            .setNameFormat("task-watcher-%d")
            .setDaemon(true)
            .build());

    heartbeatExecutorService = Executors.newScheduledThreadPool(config.getMaxParallelTasks(),
        new ThreadFactoryBuilder()
            .setNameFormat("task-heartbeat-%d")
            .build());

    taskContext = new TaskContext().setThirdEyeWorkerConfiguration(thirdEyeServerConfiguration);

    this.taskRunnerFactory = taskRunnerFactory;
  }

  private Long fetchWorkerId(final TaskDriverConfiguration config) {
    if(config.isRandomWorkerIdEnabled()) {
      checkArgument(isNull(config.getId()),
          "worker id should be null when randomWorkerIdEnabled is true");
      return Math.abs(new Random().nextLong());
    } else {
      requireNonNull(config.getId(),
          "worker id must be provided and unique for every worker");
      checkArgument(config.getId() >= 0,
          "worker id is expected to be a non negative integer");
      return config.getId();
    }
  }

  public Long getWorkerId() {
    return this.workerId;
  }

  public void start() {
    handleLeftoverTasks();
    runTasksInParallel();
  }

  private void runTasksInParallel() {
    for (int i = 0; i < config.getMaxParallelTasks(); i++) {
      taskWatcherExecutorService.submit(runTask());
    }
  }

  private TaskDriverRunnable runTask() {
    return new TaskDriverRunnable(
        taskManager,
        taskContext,
        shutdown,
        taskExecutorService,
        heartbeatExecutorService,
        config,
        workerId,
        taskRunnerFactory,
        metricRegistry
    );
  }

  /**
   * Mark all assigned tasks with RUNNING as FAILED
   */
  private void handleLeftoverTasks() {
    List<TaskDTO> leftoverTasks = taskManager
        .findByStatusAndWorkerId(workerId, TaskStatus.RUNNING);
    if (!leftoverTasks.isEmpty()) {
      LOG.info("Found {} RUNNING tasks with worker id {} at start", leftoverTasks.size(), workerId);
      for (TaskDTO task : leftoverTasks) {
        LOG.info("Update task {} from RUNNING to FAILED", task.getId());
        taskManager.updateStatusAndTaskEndTime(task.getId(),
            TaskStatus.RUNNING,
            TaskStatus.FAILED,
            System.currentTimeMillis(),
            "FAILED status updated by the worker at start");
      }
    }
  }

  public void shutdown() {
    shutdown.set(true);
    AnomalyUtils.safelyShutdownExecutionService(taskExecutorService, this.getClass());
    AnomalyUtils.safelyShutdownExecutionService(taskWatcherExecutorService, this.getClass());
    AnomalyUtils.safelyShutdownExecutionService(heartbeatExecutorService, this.getClass());
  }
}
