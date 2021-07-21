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

package org.apache.pinot.thirdeye.task;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.detection.anomaly.utils.AnomalyUtils;
import org.apache.pinot.thirdeye.spi.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskDTO;
import org.apache.pinot.thirdeye.spi.task.TaskConstants.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TaskDriver {

  private static final Logger LOG = LoggerFactory.getLogger(TaskDriver.class);

  private final ExecutorService taskExecutorService;
  private final ExecutorService taskWatcherExecutorService;

  private final TaskManager taskManager;
  private final TaskContext taskContext;
  private final TaskDriverConfiguration config;
  private final Long workerId;
  private final AtomicBoolean shutdown = new AtomicBoolean(false);
  private final TaskRunnerFactory taskRunnerFactory;
  private final MetricRegistry metricRegistry;

  @Inject
  public TaskDriver(final ThirdEyeCoordinatorConfiguration thirdEyeCoordinatorConfiguration,
      final TaskManager taskManager,
      final TaskRunnerFactory taskRunnerFactory,
      final MetricRegistry metricRegistry) {
    this.taskManager = taskManager;
    this.metricRegistry = metricRegistry;
    config = thirdEyeCoordinatorConfiguration.getTaskDriverConfiguration();
    workerId = requireNonNull(config.getId(),
        "worker id must be provided and unique for every worker");
    checkArgument(workerId >= 0,
        "worker id is expected to be a non negative integer");

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

    taskContext = new TaskContext().setThirdEyeWorkerConfiguration(thirdEyeCoordinatorConfiguration);

    this.taskRunnerFactory = taskRunnerFactory;
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
  }
}
