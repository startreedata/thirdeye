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
package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.Constants.JobStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import java.sql.Timestamp;

// fixme cyril - don't think this should implement ThirdEyeCrudApi<JobApi>  
public class JobApi implements ThirdEyeCrudApi<JobApi>{
  private Long id;
  private String jobName;
  private JobStatus status;
  private TaskType taskType;
  private long scheduleStartTime;
  private long scheduleEndTime;
  private long windowStartTime;
  private long windowEndTime;
  private Timestamp lastModified;
  private long configId;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public JobApi setId(final Long id) {
    this.id = id;
    return this;
  }

  // fixme cyril - auth not implemented for JobApi. See fixme above
  @Override
  public AuthorizationConfigurationApi getAuth() {
    return null;
  }

  @Override
  public JobApi setAuth(final AuthorizationConfigurationApi auth) {
    return null;
  }

  public String getJobName() {
    return jobName;
  }

  public JobApi setJobName(final String jobName) {
    this.jobName = jobName;
    return this;
  }

  public JobStatus getStatus() {
    return status;
  }

  public JobApi setStatus(final JobStatus status) {
    this.status = status;
    return this;
  }

  public TaskType getTaskType() {
    return taskType;
  }

  public JobApi setTaskType(final TaskType taskType) {
    this.taskType = taskType;
    return this;
  }

  public long getScheduleStartTime() {
    return scheduleStartTime;
  }

  public JobApi setScheduleStartTime(final long scheduleStartTime) {
    this.scheduleStartTime = scheduleStartTime;
    return this;
  }

  public long getScheduleEndTime() {
    return scheduleEndTime;
  }

  public JobApi setScheduleEndTime(final long scheduleEndTime) {
    this.scheduleEndTime = scheduleEndTime;
    return this;
  }

  public long getWindowStartTime() {
    return windowStartTime;
  }

  public JobApi setWindowStartTime(final long windowStartTime) {
    this.windowStartTime = windowStartTime;
    return this;
  }

  public long getWindowEndTime() {
    return windowEndTime;
  }

  public JobApi setWindowEndTime(final long windowEndTime) {
    this.windowEndTime = windowEndTime;
    return this;
  }

  public Timestamp getLastModified() {
    return lastModified;
  }

  public JobApi setLastModified(final Timestamp lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  public long getConfigId() {
    return configId;
  }

  public JobApi setConfigId(final long configId) {
    this.configId = configId;
    return this;
  }
}
