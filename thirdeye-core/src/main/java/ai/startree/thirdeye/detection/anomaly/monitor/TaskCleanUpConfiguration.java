/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.monitor;

public class TaskCleanUpConfiguration {

  private Integer intervalInMinutes = 5;
  private Integer retentionInDays = 30;
  private Integer maxEntriesToDelete = 1000;

  public Integer getIntervalInMinutes() {
    return intervalInMinutes;
  }

  public TaskCleanUpConfiguration setIntervalInMinutes(final Integer intervalInMinutes) {
    this.intervalInMinutes = intervalInMinutes;
    return this;
  }

  public Integer getRetentionInDays() {
    return retentionInDays;
  }

  public TaskCleanUpConfiguration setRetentionInDays(final Integer retentionInDays) {
    this.retentionInDays = retentionInDays;
    return this;
  }

  public Integer getMaxEntriesToDelete() {
    return maxEntriesToDelete;
  }

  public TaskCleanUpConfiguration setMaxEntriesToDelete(final Integer maxEntriesToDelete) {
    this.maxEntriesToDelete = maxEntriesToDelete;
    return this;
  }
}
