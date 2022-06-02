/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task;

import java.time.Duration;
import java.util.Random;

public class TaskDriverConfiguration {

  private Long id;
  private boolean enabled = false;
  private boolean betaEnabled = false;
  private Duration noTaskDelay = Duration.ofSeconds(15);
  private Duration taskFailureDelay = Duration.ofSeconds(30);
  private Duration randomDelayCap = Duration.ofSeconds(15);
  private Duration maxTaskRunTime = Duration.ofHours(6);
  private Duration heartbeatInterval = Duration.ofMinutes(5);

  private int taskFetchSizeCap = 50;
  private int maxParallelTasks = 5;

  public Long getId() {
    return id;
  }

  public TaskDriverConfiguration setId(final Long id) {
    if(betaEnabled){
      this.id = Math.abs(new Random().nextLong());
    } else {
      this.id = id;
    }
    return this;
  }

  public boolean isBetaEnabled() {
    return betaEnabled;
  }

  public TaskDriverConfiguration setBetaEnabled(final boolean betaEnabled) {
    this.betaEnabled = betaEnabled;
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
}
