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

import static ai.startree.thirdeye.spi.util.ExecutorUtils.shutdownExecutionService;
import static java.util.Collections.emptyList;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class TaskDriverThreadPoolManager {

  private final ExecutorService taskExecutorService;
  private final ExecutorService taskWatcherExecutorService;
  private final ScheduledExecutorService heartbeatExecutorService;
  private final AtomicBoolean shutdown = new AtomicBoolean(false);

  @Inject
  public TaskDriverThreadPoolManager(final TaskDriverConfiguration config) {
    taskExecutorService = Executors.newFixedThreadPool(
        config.getMaxParallelTasks(),
        new ThreadFactoryBuilder()
            .setNameFormat("task-executor-%d")
            .build());
    new ExecutorServiceMetrics(taskExecutorService, "task-executor", emptyList()).bindTo(
        Metrics.globalRegistry);

    taskWatcherExecutorService = Executors.newFixedThreadPool(
        config.getMaxParallelTasks(),
        new ThreadFactoryBuilder()
            .setNameFormat("task-watcher-%d")
            .setDaemon(true)
            .build());
    new ExecutorServiceMetrics(taskWatcherExecutorService, "task-watcher", emptyList()).bindTo(
        Metrics.globalRegistry);

    heartbeatExecutorService = Executors.newScheduledThreadPool(config.getMaxParallelTasks(),
        new ThreadFactoryBuilder()
            .setNameFormat("task-heartbeat-%d")
            .build());
    new ExecutorServiceMetrics(heartbeatExecutorService, "task-heartbeat", emptyList()).bindTo(
        Metrics.globalRegistry);
  }

  public ExecutorService getTaskExecutorService() {
    return taskExecutorService;
  }

  public ExecutorService getTaskWatcherExecutorService() {
    return taskWatcherExecutorService;
  }

  public ScheduledExecutorService getHeartbeatExecutorService() {
    return heartbeatExecutorService;
  }

  public boolean isShutdown() {
    return shutdown.get();
  }

  public void shutdown() {
    shutdown.set(true);
    shutdownExecutionService(taskExecutorService);
    shutdownExecutionService(taskWatcherExecutorService);
    shutdownExecutionService(heartbeatExecutorService);
  }
}
