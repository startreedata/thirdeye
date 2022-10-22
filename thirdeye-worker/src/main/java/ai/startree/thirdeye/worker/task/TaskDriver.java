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
package ai.startree.thirdeye.worker.task;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TaskDriver {

  private static final Logger LOG = LoggerFactory.getLogger(TaskDriver.class);

  private final TaskManager taskManager;
  private final TaskContext taskContext;
  private final TaskDriverConfiguration config;
  private final Long workerId;
  private final TaskDriverThreadPoolManager taskDriverThreadPoolManager;

  @Inject
  public TaskDriver(final TaskManager taskManager,
      final TaskRunnerFactory taskRunnerFactory,
      final TaskDriverThreadPoolManager taskDriverThreadPoolManager,
      final MetricRegistry metricRegistry,
      final TaskDriverConfiguration taskDriverConfiguration) {
    this.taskManager = taskManager;
    this.taskDriverThreadPoolManager = taskDriverThreadPoolManager;
    config = taskDriverConfiguration;
    workerId = fetchWorkerId(config);

    taskContext = new TaskContext()
        .setTaskManager(taskManager)
        .setTaskDriverThreadPoolManager(taskDriverThreadPoolManager)
        .setTaskRunnerFactory(taskRunnerFactory)
        .setMetricRegistry(metricRegistry)
        .setConfig(taskDriverConfiguration)
        .setWorkerId(workerId);
  }

  private Long fetchWorkerId(final TaskDriverConfiguration config) {
    if (config.isRandomWorkerIdEnabled()) {
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
    return workerId;
  }

  public void start() {
    handleLeftoverTasks();
    runTasksInParallel();
  }

  private void runTasksInParallel() {
    for (int i = 0; i < config.getMaxParallelTasks(); i++) {
      taskDriverThreadPoolManager.getTaskWatcherExecutorService()
          .submit(new TaskDriverRunnable(taskContext));
    }
  }

  /**
   * Mark all assigned tasks with RUNNING as FAILED
   */
  private void handleLeftoverTasks() {
    final List<TaskDTO> leftoverTasks = taskManager
        .findByStatusAndWorkerId(workerId, TaskStatus.RUNNING);
    if (!leftoverTasks.isEmpty()) {
      LOG.info("Found {} RUNNING tasks with worker id {} at start", leftoverTasks.size(), workerId);
      for (final TaskDTO task : leftoverTasks) {
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
    taskDriverThreadPoolManager.shutdown();
  }
}
