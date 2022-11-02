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

import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import com.codahale.metrics.MetricRegistry;

public class TaskContext {

  private TaskDriverConfiguration config;
  private long workerId;

  private TaskDriverThreadPoolManager taskDriverThreadPoolManager;
  private TaskManager taskManager;
  private TaskRunnerFactory taskRunnerFactory;
  private MetricRegistry metricRegistry;

  public TaskDriverConfiguration getConfig() {
    return config;
  }

  public TaskContext setConfig(final TaskDriverConfiguration config) {
    this.config = config;
    return this;
  }

  public long getWorkerId() {
    return workerId;
  }

  public TaskContext setWorkerId(final long workerId) {
    this.workerId = workerId;
    return this;
  }

  public TaskDriverThreadPoolManager getTaskDriverThreadPoolManager() {
    return taskDriverThreadPoolManager;
  }

  public TaskContext setTaskDriverThreadPoolManager(
      final TaskDriverThreadPoolManager taskDriverThreadPoolManager) {
    this.taskDriverThreadPoolManager = taskDriverThreadPoolManager;
    return this;
  }

  public TaskManager getTaskManager() {
    return taskManager;
  }

  public TaskContext setTaskManager(final TaskManager taskManager) {
    this.taskManager = taskManager;
    return this;
  }

  public TaskRunnerFactory getTaskRunnerFactory() {
    return taskRunnerFactory;
  }

  public TaskContext setTaskRunnerFactory(
      final TaskRunnerFactory taskRunnerFactory) {
    this.taskRunnerFactory = taskRunnerFactory;
    return this;
  }

  public MetricRegistry getMetricRegistry() {
    return metricRegistry;
  }

  public TaskContext setMetricRegistry(final MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
    return this;
  }
}
