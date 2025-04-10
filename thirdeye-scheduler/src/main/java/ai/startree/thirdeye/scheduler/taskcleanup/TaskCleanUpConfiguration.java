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
package ai.startree.thirdeye.scheduler.taskcleanup;

import static ai.startree.thirdeye.spi.Constants.TASK_MAX_DELETES_PER_CLEANUP;

public class TaskCleanUpConfiguration {

  // 0 can be used as a special value to disable old tasks clean up
  private Integer intervalInMinutes = 5;
  private Integer retentionInDays = 30;
  private Integer maxEntriesToDelete = TASK_MAX_DELETES_PER_CLEANUP;
  // 0 can be used a special value to disable orphan tasks clean up
  private Integer orphanIntervalInSeconds = 30;

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

  public Integer getOrphanIntervalInSeconds() {
    return orphanIntervalInSeconds;
  }

  public TaskCleanUpConfiguration setOrphanIntervalInSeconds(
      final Integer orphanIntervalInSeconds) {
    this.orphanIntervalInSeconds = orphanIntervalInSeconds;
    return this;
  }
}
