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
package ai.startree.thirdeye.worker.task;

import java.time.Duration;

public class TaskDriverConfiguration {

  private Long id;
  private boolean enabled = false;
  private boolean randomWorkerIdEnabled = false;
  private Duration noTaskDelay = Duration.ofSeconds(15);
  private Duration taskFailureDelay = Duration.ofSeconds(30);
  private Duration randomDelayCap = Duration.ofSeconds(15);
  private Duration maxTaskRunTime = Duration.ofHours(6);
  private Duration heartbeatInterval = Duration.ofSeconds(30);
  // The multiplies of heartbeatInterval allowed past lastActive before considering a task inactive
  private int activeThresholdMultiplier = 3;

  private int taskFetchSizeCap = 50;
  private int maxParallelTasks = 5;

  public Long getId() {
    return id;
  }

  public TaskDriverConfiguration setId(final Long id) {
    this.id = id;
    return this;
  }

  public boolean isRandomWorkerIdEnabled() {
    return randomWorkerIdEnabled;
  }

  public TaskDriverConfiguration setRandomWorkerIdEnabled(final boolean randomWorkerIdEnabled) {
    this.randomWorkerIdEnabled = randomWorkerIdEnabled;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public TaskDriverConfiguration setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public Duration getNoTaskDelay() {
    return noTaskDelay;
  }

  public TaskDriverConfiguration setNoTaskDelay(final Duration noTaskDelay) {
    this.noTaskDelay = noTaskDelay;
    return this;
  }

  public Duration getTaskFailureDelay() {
    return taskFailureDelay;
  }

  public TaskDriverConfiguration setTaskFailureDelay(final Duration taskFailureDelay) {
    this.taskFailureDelay = taskFailureDelay;
    return this;
  }

  public Duration getRandomDelayCap() {
    return randomDelayCap;
  }

  public TaskDriverConfiguration setRandomDelayCap(final Duration randomDelayCap) {
    this.randomDelayCap = randomDelayCap;
    return this;
  }

  public Duration getMaxTaskRunTime() {
    return maxTaskRunTime;
  }

  public TaskDriverConfiguration setMaxTaskRunTime(final Duration maxTaskRunTime) {
    this.maxTaskRunTime = maxTaskRunTime;
    return this;
  }

  public int getTaskFetchSizeCap() {
    return taskFetchSizeCap;
  }

  public TaskDriverConfiguration setTaskFetchSizeCap(final int taskFetchSizeCap) {
    this.taskFetchSizeCap = taskFetchSizeCap;
    return this;
  }

  public int getMaxParallelTasks() {
    return maxParallelTasks;
  }

  public TaskDriverConfiguration setMaxParallelTasks(final int maxParallelTasks) {
    this.maxParallelTasks = maxParallelTasks;
    return this;
  }

  public Duration getHeartbeatInterval() {
    return heartbeatInterval;
  }

  public TaskDriverConfiguration setHeartbeatInterval(final Duration heartbeatInterval) {
    this.heartbeatInterval = heartbeatInterval;
    return this;
  }

  public int getActiveThresholdMultiplier() {
    return activeThresholdMultiplier;
  }

  public TaskDriverConfiguration setActiveThresholdMultiplier(final int activeThresholdMultiplier) {
    this.activeThresholdMultiplier = activeThresholdMultiplier;
    return this;
  }
}
