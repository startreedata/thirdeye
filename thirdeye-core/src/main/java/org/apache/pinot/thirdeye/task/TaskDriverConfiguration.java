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

import java.time.Duration;

public class TaskDriverConfiguration {

  private Long id;
  private boolean enabled = false;
  private Duration noTaskDelay = Duration.ofSeconds(15);
  private Duration taskFailureDelay = Duration.ofSeconds(30);
  private Duration randomDelayCap = Duration.ofSeconds(15);
  private Duration maxTaskRunTime = Duration.ofHours(6);

  private int taskFetchSizeCap = 50;
  private int maxParallelTasks = 5;

  public Long getId() {
    return id;
  }

  public TaskDriverConfiguration setId(final Long id) {
    this.id = id;
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
}
