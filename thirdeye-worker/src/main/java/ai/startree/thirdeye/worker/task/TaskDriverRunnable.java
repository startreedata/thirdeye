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

import static ai.startree.thirdeye.spi.Constants.METRICS_TIMER_PERCENTILES;
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
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer.Sample;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  @Deprecated //  use thirdeye_task_run
  private final Counter taskExceptionCounter;
  @Deprecated //  use thirdeye_task_run
  private final Counter taskSuccessCounter;
  @Deprecated //  use thirdeye_task_run
  private final Counter taskCounter;
  @Deprecated //  use thirdeye_task_wait
  private final Counter taskFetchHitCounter;
  @Deprecated //  use thirdeye_task_runner_idle
  private final Counter taskFetchMissCounter;
  @Deprecated // use thirdeye_task_runner_idle
  private final Counter workerIdleTimeInSeconds;
  @Deprecated //  use thirdeye_task_run
  private final Timer taskRunningTimer;
  @Deprecated // use thirdeye_task_wait
  private final Timer taskWaitingTimer;
  private final TaskDriverThreadPoolManager taskDriverThreadPoolManager;
  private final io.micrometer.core.instrument.Timer taskRunTimerOfSuccess;
  private final io.micrometer.core.instrument.Timer taskRunTimerOfException;
  private final io.micrometer.core.instrument.Timer taskWaitTimer;
  private final io.micrometer.core.instrument.Timer taskRunnerWaitIdleTimer;

  public TaskDriverRunnable(final TaskContext taskContext) {
    this.taskContext = taskContext;
    taskDriverThreadPoolManager = taskContext.getTaskDriverThreadPoolManager();

    this.taskManager = taskContext.getTaskManager();
    this.config = taskContext.getConfig();
    this.workerId = taskContext.getWorkerId();
    this.taskRunnerFactory = taskContext.getTaskRunnerFactory();

    final MetricRegistry metricRegistry = taskContext.getMetricRegistry();
    // deprecated - use thirdeye_task_run
    taskExceptionCounter = metricRegistry.counter("taskExceptionCounter");
    taskSuccessCounter = metricRegistry.counter("taskSuccessCounter");
    taskCounter = metricRegistry.counter("taskCounter");
    taskRunningTimer = metricRegistry.timer("taskRunningTimer");

    final String description = "Start: a taskDTO is passed for execution. End: the task has run or failed. Tag exception=true means an exception was thrown by the method call.";
    this.taskRunTimerOfSuccess = io.micrometer.core.instrument.Timer.builder("thirdeye_task_run")
        .description(description)
        .publishPercentiles(METRICS_TIMER_PERCENTILES)
        .tag("exception", "false")
        .register(Metrics.globalRegistry);
    this.taskRunTimerOfException = io.micrometer.core.instrument.Timer.builder("thirdeye_task_run")
        .description(description)
        .publishPercentiles(METRICS_TIMER_PERCENTILES)
        .tag("exception", "true")
        .register(Metrics.globalRegistry);

    // deprecated - use thirdeye_task_wait
    taskFetchHitCounter = metricRegistry.counter("taskFetchHitCounter");
    taskWaitingTimer = metricRegistry.timer("taskWaitingTimer");
    this.taskWaitTimer = io.micrometer.core.instrument.Timer.builder("thirdeye_task_wait")
        .description(
            "Start: a task is created in the persistence layer. End: the task is picked by a task runner for execution.")
        .register(Metrics.globalRegistry);

    // deprecated - use thirdeye_task_runner_idle
    taskFetchMissCounter = metricRegistry.counter("taskFetchMissCounter");
    workerIdleTimeInSeconds = metricRegistry.counter("workerIdleTimeInSeconds");
    this.taskRunnerWaitIdleTimer = io.micrometer.core.instrument.Timer.builder(
            "thirdeye_task_runner_idle")
        .description(
            "Start: start thread sleep because no tasks were found. End: end of sleep. Mostly used for the sum and the count.")
        .publishPercentiles(METRICS_TIMER_PERCENTILES)
        .register(Metrics.globalRegistry);
  }

  public void run() {
    while (!isShutdown()) {
      // select a task to execute, and update it to RUNNING
      final TaskDTO taskDTO = waitForTask();
      if (taskDTO == null) {
        continue;
      }

      // a task has acquired and we must finish executing it before termination
      taskRunningTimer.time(() -> runTask(taskDTO));
    }
    LOG.info(String.format("TaskDriverRunnable safely quitting. name: %s",
        Thread.currentThread().getName()));
  }

  private boolean isShutdown() {
    return taskDriverThreadPoolManager.isShutdown();
  }

  private void runTask(final TaskDTO taskDTO) {
    LOG.info("Task {} {}: executing {}", taskDTO.getId(), taskDTO.getJobName(),
        taskDTO.getTaskInfo());

    final Sample sample = io.micrometer.core.instrument.Timer.start(Metrics.globalRegistry);
    final long tStart = System.nanoTime();
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
      future.get(config.getMaxTaskRunTime().toMillis(), TimeUnit.MILLISECONDS);
      updateTaskStatus(taskDTO.getId(), TaskStatus.COMPLETED, "");
      LOG.info("Task {} {}: COMPLETED", taskDTO.getId(), taskDTO.getJobName());
      taskSuccessCounter.inc();
      sample.stop(taskRunTimerOfSuccess);
    } catch (TimeoutException e) {
      future.cancel(true);
      taskExceptionCounter.inc();
      sample.stop(taskRunTimerOfException);
      LOG.error("Task {} {}: TIMEOUT after a period of {}", taskDTO.getId(), taskDTO.getJobName(),
          config.getMaxTaskRunTime(), e);
      updateTaskStatus(taskDTO.getId(), TaskStatus.TIMEOUT, e.getMessage());
    } catch (Exception e) {
      taskExceptionCounter.inc();
      sample.stop(taskRunTimerOfException);
      LOG.error("Task {} {}: FAILED with exception.", taskDTO.getId(), taskDTO.getJobName(), e);
      updateTaskStatus(taskDTO.getId(), TaskStatus.FAILED,
          String.format("%s\n%s", ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e)));
    } finally {
      long elapsedTime = (System.nanoTime() - tStart) / 1_000_000;
      LOG.info("Task {} {}: run took {}ms", taskDTO.getId(), taskDTO.getJobName(), elapsedTime);
      optional(heartbeat).ifPresent(f -> f.cancel(false));
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

  /**
   * Returns a TaskDTO if a task is successfully acquired; returns null if system is shutting down.
   *
   * @return null if system is shutting down.
   */
  private TaskDTO waitForTask() {
    while (!isShutdown()) {
      TaskDTO nextTask = null;
      try {
        nextTask = taskManager.findNextTaskToRun();
      } catch (Exception e) {
        LOG.error("Failed to fetch a new task to run", e);
      }
      boolean taskFound = nextTask != null;
      if (taskFound) {
        // FIXME CYRIL REWRITE THIS
        final TaskDTO taskDTO = acquireTask(List.of(nextTask));
        if (taskDTO != null) {
          taskFetchHitCounter.inc();
          return taskDTO;
        }
      }
      taskFetchMissCounter.inc();
      final long idleStart = System.nanoTime();
      // FIXME CYRIL REWRITE THIS
      taskRunnerWaitIdleTimer.record(() -> sleep(!taskFound));
      workerIdleTimeInSeconds.inc(
          TimeUnit.SECONDS.convert(System.nanoTime() - idleStart, TimeUnit.NANOSECONDS));
    }
    return null;
  }

  private TaskDTO acquireTask(final List<TaskDTO> anomalyTasks) {
    for (final TaskDTO taskDTO : anomalyTasks) {
      try {
        // Don't acquire a new task if shutting down.
        if (!isShutdown()) {
          boolean success = taskManager.updateStatusAndWorkerId(workerId,
              taskDTO.getId(),
              ALLOWED_OLD_TASK_STATUS,
              taskDTO.getVersion());
          if (success) {
            final long waitTime = System.currentTimeMillis() - taskDTO.getCreateTime().getTime();
            taskWaitTimer.record(waitTime, TimeUnit.MILLISECONDS);
            taskWaitingTimer.update(waitTime, TimeUnit.MILLISECONDS);
            return taskDTO;
          }
        }
      } catch (Exception e) {
        LOG.warn("Got exception when acquiring task. (Worker Id: {})", workerId, e);
      }
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
