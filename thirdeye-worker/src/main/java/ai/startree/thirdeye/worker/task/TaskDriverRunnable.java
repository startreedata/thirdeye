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
package ai.startree.thirdeye.worker.task;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Singleton
public class TaskDriverRunnable implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(TaskDriverRunnable.class);
  private static final Random RANDOM = new Random();
  private static final Set<TaskStatus> ALLOWED_OLD_TASK_STATUS = ImmutableSet.of(
      TaskStatus.FAILED,
      TaskStatus.WAITING
  );

  private final TaskManager taskManager;
  private final TaskContext taskContext;
  private final TaskDriverConfiguration config;
  private final long workerId;
  private final TaskRunnerFactory taskRunnerFactory;

  // migration to micrometer. we start by verifying that old and new counters behave the same
  private final Counter taskExceptionCounter;
  private final Counter taskSuccessCounter;

  private final Counter taskCounter;
  private final Counter taskFetchHitCounter;
  private final Counter taskFetchMissCounter;
  private final Counter workerIdleTimeInSeconds;
  private final Timer taskRunningTimer;
  private final Timer taskWaitingTimer;
  private final TaskDriverThreadPoolManager taskDriverThreadPoolManager;

  public TaskDriverRunnable(final TaskContext taskContext) {
    this.taskContext = taskContext;
    taskDriverThreadPoolManager = taskContext.getTaskDriverThreadPoolManager();

    this.taskManager = taskContext.getTaskManager();
    this.config = taskContext.getConfig();
    this.workerId = taskContext.getWorkerId();
    this.taskRunnerFactory = taskContext.getTaskRunnerFactory();

    final MetricRegistry metricRegistry = taskContext.getMetricRegistry();
    taskExceptionCounter = metricRegistry.counter("taskExceptionCounter");
    taskSuccessCounter = metricRegistry.counter("taskSuccessCounter");
    taskCounter = metricRegistry.counter("taskCounter");
    
    taskRunningTimer = metricRegistry.timer("taskRunningTimer");
    taskFetchHitCounter = metricRegistry.counter("taskFetchHitCounter");
    taskFetchMissCounter = metricRegistry.counter("taskFetchMissCounter");
    
    workerIdleTimeInSeconds = metricRegistry.counter("workerIdleTimeInSeconds");
    taskWaitingTimer = metricRegistry.timer("taskWaitingTimer");
  }

  public void run() {
    while (!isShutdown()) {
      // select a task to execute, and update it to RUNNING
      final TaskDTO taskDTO = waitForTask();
      if (taskDTO == null) {
        continue;
      }

      // a task has acquired and we must finish executing it before termination
      taskRunningTimer.time(() -> runAcquiredTask(taskDTO));
    }
    LOG.info(String.format("TaskDriverRunnable safely quitting. name: %s",
        Thread.currentThread().getName()));
  }

  private boolean isShutdown() {
    return taskDriverThreadPoolManager.isShutdown();
  }

  private void runAcquiredTask(final TaskDTO taskDTO) {
    LOG.info("Executing task {} {}", taskDTO.getId(), taskDTO.getTaskInfo());

    final long tStart = System.currentTimeMillis();
    taskCounter.inc();

    Future heartbeat = null;
    if (config.isRandomWorkerIdEnabled()) {
      heartbeat = taskDriverThreadPoolManager.getHeartbeatExecutorService()
          .scheduleAtFixedRate(() -> taskExecutionHeartbeat(taskDTO),
              0,
              config.getHeartbeatInterval().toMillis(),
              TimeUnit.MILLISECONDS);
    }

    Future<List<TaskResult>> future = null;
    try {
      future = runTaskAsync(taskDTO);
      // wait for the future to complete
      future.get(config.getMaxTaskRunTime().toMillis(), TimeUnit.MILLISECONDS);

      LOG.info("DONE Executing task {}", taskDTO.getId());
      // update status to COMPLETED
      updateTaskStatus(taskDTO.getId(),
          TaskStatus.COMPLETED,
          "");

      taskSuccessCounter.inc();
    } catch (TimeoutException e) {
      handleTimeout(taskDTO, future, e);
    } catch (Exception e) {
      handleException(taskDTO, e);
    } finally {
      long elapsedTime = System.currentTimeMillis() - tStart;
      LOG.info("Task {} took {}ms", taskDTO.getId(), elapsedTime);
      optional(heartbeat).ifPresent(pulse -> pulse.cancel(false));
    }
  }

  private void taskExecutionHeartbeat(final TaskDTO taskDTO) {
    taskManager.updateLastActive(taskDTO.getId());
  }

  private Future<List<TaskResult>> runTaskAsync(final TaskDTO taskDTO) throws IOException {
    final TaskType taskType = taskDTO.getTaskType();
    final TaskInfo taskInfo = TaskInfoFactory.get(taskType, taskDTO.getTaskInfo());
    final TaskRunner taskRunner = taskRunnerFactory.get(taskType);

    // execute the selected task asynchronously
    return taskDriverThreadPoolManager.getTaskExecutorService()
        .submit(() -> taskRunner.execute(taskInfo, taskContext));
  }

  private void handleTimeout(final TaskDTO taskDTO, final Future<List<TaskResult>> future,
      final TimeoutException e) {
    taskExceptionCounter.inc();
    LOG.error("Timeout on executing task", e);
    if (future != null) {
      future.cancel(true);
      LOG.info("Executor thread gets cancelled successfully: {}", future.isCancelled());
    }

    updateTaskStatus(taskDTO.getId(),
        TaskStatus.TIMEOUT,
        e.getMessage());
  }

  private void handleException(final TaskDTO task, final Exception e) {
    taskExceptionCounter.inc();
    LOG.error(String.format("Exception in electing and executing task(id: %d, name: %s)",
        task.getId(),
        task.getJobName()), e);

    // update task status failed
    updateTaskStatus(task.getId(),
        TaskStatus.FAILED,
        String.format("%s\n%s", ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e)));
  }

  /**
   * Returns a TaskDTO if a task is successfully acquired; returns null if system is shutting down.
   *
   * @return null if system is shutting down.
   */
  private TaskDTO waitForTask() {
    while (!isShutdown()) {
      final List<TaskDTO> anomalyTasks = findTasks();

      final boolean tasksFound = CollectionUtils.isNotEmpty(anomalyTasks);
      if (tasksFound) {
        final TaskDTO taskDTO = acquireTask(anomalyTasks);
        if (taskDTO != null) {
          taskFetchHitCounter.inc();
          return taskDTO;
        }
      }
      taskFetchMissCounter.inc();
      final long idleStart = System.nanoTime();
      sleep(!tasksFound);
      workerIdleTimeInSeconds.inc(TimeUnit.SECONDS.convert(System.nanoTime() - idleStart, TimeUnit.NANOSECONDS));
    }
    return null;
  }

  private TaskDTO acquireTask(final List<TaskDTO> anomalyTasks) {
    // shuffle candidate tasks to avoid synchronized patterns across threads (and hosts)
    Collections.shuffle(anomalyTasks);

    for (final TaskDTO taskDTO : anomalyTasks) {
      try {
        // Don't acquire a new task if shutting down.
        if (!isShutdown()) {
          boolean success = taskManager.updateStatusAndWorkerId(workerId,
              taskDTO.getId(),
              ALLOWED_OLD_TASK_STATUS,
              taskDTO.getVersion());
          if (success) {
            taskWaitingTimer.update(
                System.currentTimeMillis() - taskDTO.getCreateTime().getTime(),
                TimeUnit.MILLISECONDS);
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
      return taskManager.findByStatusOrderByCreateTime(TaskStatus.WAITING,
          config.getTaskFetchSizeCap(),
          orderAscending);
    } catch (Exception e) {
      LOG.error("Exception found in fetching new tasks", e);
    }
    return null;
  }

  private void sleep(final boolean hasFetchError) {
    final long sleepTime = hasFetchError
        ? config.getTaskFailureDelay().toMillis()
        : config.getNoTaskDelay().toMillis() + RANDOM
            .nextInt((int) config.getRandomDelayCap().toMillis());
    // sleep for few seconds if not tasks found - avoid cpu thrashing
    // also add some extra random number of milliseconds to allow threads to start at different times
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      if (!isShutdown()) {
        LOG.warn(e.getMessage(), e);
      }
    }
  }

  private void updateTaskStatus(long taskId,
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
