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

package org.apache.pinot.thirdeye.anomaly.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.pinot.thirdeye.anomaly.ThirdEyeWorkerConfiguration;
import org.apache.pinot.thirdeye.anomaly.task.TaskConstants.TaskStatus;
import org.apache.pinot.thirdeye.anomaly.task.TaskConstants.TaskType;
import org.apache.pinot.thirdeye.anomaly.utils.AnomalyUtils;
import org.apache.pinot.thirdeye.anomaly.utils.ThirdeyeMetricsUtil;
import org.apache.pinot.thirdeye.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.datalayer.dto.TaskDTO;
import org.apache.pinot.thirdeye.datasource.DAORegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Singleton
public class TaskDriver {

  private static final Logger LOG = LoggerFactory.getLogger(TaskDriver.class);
  private static final Random RANDOM = new Random();

  private final ExecutorService taskExecutorService;
  private final ExecutorService taskWatcherExecutorService;

  private final TaskManager taskManager;
  private final TaskContext taskContext;
  private final Set<TaskStatus> allowedOldTaskStatus = new HashSet<>();
  private final TaskDriverConfiguration config;
  private final long workerId;

  private volatile boolean shutdown = false;

  @Inject
  public TaskDriver(final ThirdEyeWorkerConfiguration workerConfiguration,
      final TaskManager taskManager,
      final DAORegistry daoRegistry) {
    this.taskManager = taskManager;
    config = workerConfiguration.getTaskDriverConfiguration();
    workerId = workerConfiguration.getId();

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

    taskContext = new TaskContext()
        .setThirdEyeWorkerConfiguration(workerConfiguration)
        .setDaoRegistry(daoRegistry);

    allowedOldTaskStatus.add(TaskStatus.FAILED);
    allowedOldTaskStatus.add(TaskStatus.WAITING);
  }

  public void start() {
    handleLeftoverTasks();
    runTasksInParallel();
  }

  private void runTasksInParallel() {
    for (int i = 0; i < config.getMaxParallelTasks(); i++) {
      taskWatcherExecutorService.submit(this::runTask);
    }
  }

  private void runTask() {
    while (!shutdown) {
      // select a task to execute, and update it to RUNNING
      final TaskDTO taskDTO = waitForTask();
      if (taskDTO == null) {
        continue;
      }

      // a task has acquired and we must finish executing it before termination
      runAcquiredTask(taskDTO);
    }
    LOG.info("Thread safely quiting");
  }

  private void runAcquiredTask(final TaskDTO taskDTO) {
    MDC.put("job.name", taskDTO.getJobName());
    LOG.info("Executing task {} {}", taskDTO.getId(), taskDTO.getTaskInfo());

    final long tStart = System.nanoTime();
    ThirdeyeMetricsUtil.taskCounter.inc();

    Future<List<TaskResult>> future = null;
    try {
      future = runTaskAsync(taskDTO);
      // wait for the future to complete
      future.get(config.getMaxTaskRunTimeMillis(), TimeUnit.MILLISECONDS);

      LOG.info("DONE Executing task {}", taskDTO.getId());
      // update status to COMPLETED
      updateStatusAndTaskEndTime(taskDTO.getId(),
          TaskStatus.COMPLETED,
          "");

      ThirdeyeMetricsUtil.taskSuccessCounter.inc();
    } catch (TimeoutException e) {
      handleTimeout(taskDTO, future, e);
    } catch (Exception e) {
      handleException(taskDTO, e);
    } finally {
      long elapsedTime = System.nanoTime() - tStart;
      LOG.info("Task {} took {} nano seconds", taskDTO.getId(), elapsedTime);
      MDC.clear();
      ThirdeyeMetricsUtil.taskDurationCounter.inc(elapsedTime);
    }
  }

  private Future<List<TaskResult>> runTaskAsync(final TaskDTO taskDTO) throws IOException {
    final TaskType taskType = taskDTO.getTaskType();
    final TaskRunner taskRunner = TaskRunnerFactory.getTaskRunnerFromTaskType(taskType);
    final TaskInfo taskInfo = TaskInfoFactory
        .getTaskInfoFromTaskType(taskType, taskDTO.getTaskInfo());

    // execute the selected task asynchronously
    return taskExecutorService.submit(() -> taskRunner.execute(taskInfo, taskContext));
  }

  private void handleTimeout(final TaskDTO taskDTO, final Future<List<TaskResult>> future,
      final TimeoutException e) {
    ThirdeyeMetricsUtil.taskExceptionCounter.inc();
    LOG.error("Timeout on executing task", e);
    if (future != null) {
      future.cancel(true);
      LOG.info("Executor thread gets cancelled successfully: {}", future.isCancelled());
    }

    updateStatusAndTaskEndTime(taskDTO.getId(),
        TaskStatus.TIMEOUT,
        e.getMessage());
  }

  private void handleException(final TaskDTO taskDTO, final Exception e) {
    ThirdeyeMetricsUtil.taskExceptionCounter.inc();
    LOG.error("Exception in electing and executing task", e);

    // update task status failed
    updateStatusAndTaskEndTime(taskDTO.getId(),
        TaskStatus.FAILED,
        String.format("%s\n%s", ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e)));
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
    shutdown = true;
    AnomalyUtils.safelyShutdownExecutionService(taskExecutorService, this.getClass());
    AnomalyUtils.safelyShutdownExecutionService(taskWatcherExecutorService, this.getClass());
  }

  /**
   * Returns a TaskDTO if a task is successfully acquired; returns null if system is shutting down.
   *
   * @return null if system is shutting down.
   */
  private TaskDTO waitForTask() {
    while (!shutdown) {
      final List<TaskDTO> anomalyTasks = findTasks();

      final boolean tasksFound = CollectionUtils.isNotEmpty(anomalyTasks);
      if (tasksFound) {
        final TaskDTO taskDTO = acquireTask(anomalyTasks);
        if (taskDTO != null) {
          return taskDTO;
        }
      }
      sleep(!tasksFound);
    }
    return null;
  }

  private TaskDTO acquireTask(final List<TaskDTO> anomalyTasks) {
    // shuffle candidate tasks to avoid synchronized patterns across threads (and hosts)
    Collections.shuffle(anomalyTasks);

    for (final TaskDTO taskDTO : anomalyTasks) {
      try {
        // Don't acquire a new task if shutting down.
        if (!shutdown) {
          boolean success = taskManager.updateStatusAndWorkerId(workerId,
              taskDTO.getId(),
              allowedOldTaskStatus,
              taskDTO.getVersion());
          if (success) {
            return taskDTO;
          }
        }
      } catch (Exception e) {
        LOG.warn("Got exception when acquiring task. (Worker Id: {})", workerId, e);
      }
    }
    return null;
  }

  private List<TaskDTO> findTasks() {
    try {
      // randomize fetching head and tail to reduce synchronized patterns across threads (and hosts)
      boolean orderAscending = System.currentTimeMillis() % 2 == 0;

      // find by task type to separate online task from a normal task
      return taskManager.findByStatusAndTypeNotInOrderByCreateTime(TaskStatus.WAITING,
          TaskType.DETECTION,
          config.getTaskFetchSizeCap(),
          orderAscending);
    } catch (Exception e) {
      LOG.error("Exception found in fetching new tasks", e);
    }
    return null;
  }

  private void sleep(final boolean hasFetchError) {
    final long sleepTime = hasFetchError
        ? config.getTaskFailureDelayInMillis()
        : config.getNoTaskDelayInMillis() + RANDOM
            .nextInt(config.getRandomDelayCapInMillis());
    // sleep for few seconds if not tasks found - avoid cpu thrashing
    // also add some extra random number of milli seconds to allow threads to start at different times
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      if (!shutdown) {
        LOG.warn(e.getMessage(), e);
      }
    }
  }

  private void updateStatusAndTaskEndTime(long taskId,
      TaskStatus newStatus,
      String message) {
    try {
      taskManager.updateStatusAndTaskEndTime(taskId,
          TaskStatus.RUNNING,
          newStatus,
          System.currentTimeMillis(),
          message);
      LOG.info("Updated status to {}", newStatus);
    } catch (Exception e) {
      LOG.error(String.format(
          "Exception: updating task status. Request: taskId: %d, newStatus: %s, msg: %s",
          taskId,
          newStatus,
          message), e);
    }
  }
}
