/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * This class corresponds to anomaly tasks. An execution of an anomaly function creates an anomaly
 * job, which in turn spawns into 1 or more anomaly tasks. The anomaly tasks are picked by the
 * workers
 */
public class TaskDTO extends AbstractDTO {

  private TaskType taskType;
  private Long workerId;
  private Long jobId;
  private String jobName;
  private TaskStatus status;
  private long startTime;
  private long endTime;
  // A JSON string of the task info such as anomaly function, monitoring windows, etc.
  private String taskInfo;
  // The task results, which could contain the error messages of tasks' execution.
  private String message;
  private Timestamp lastModified;
  private Timestamp lastActive;

  public Long getWorkerId() {
    return workerId;
  }

  public TaskDTO setWorkerId(Long workerId) {
    this.workerId = workerId;
    return this;
  }

  public TaskDTO setJobName(String jobName) {
    this.jobName = jobName;
    return this;
  }

  public String getJobName() {
    return jobName;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public TaskDTO setStatus(TaskStatus status) {
    this.status = status;
    return this;
  }

  public long getStartTime() {
    return startTime;
  }

  public TaskDTO setStartTime(long startTime) {
    this.startTime = startTime;
    return this;
  }

  public long getEndTime() {
    return endTime;
  }

  public TaskDTO setEndTime(long endTime) {
    this.endTime = endTime;
    return this;
  }

  public String getTaskInfo() {
    return taskInfo;
  }

  public TaskDTO setTaskInfo(String taskInfo) {
    this.taskInfo = taskInfo;
    return this;
  }

  public TaskType getTaskType() {
    return taskType;
  }

  public String getMessage() {
    return message;
  }

  public TaskDTO setMessage(String message) {
    this.message = message;
    return this;
  }

  public TaskDTO setTaskType(TaskType taskType) {
    this.taskType = taskType;
    return this;
  }

  public Timestamp getLastModified() {
    return lastModified;
  }

  public TaskDTO setLastModified(Timestamp lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  public Timestamp getLastActive() {
    return lastActive;
  }

  public TaskDTO setLastActive(final Timestamp lastActive) {
    this.lastActive = lastActive;
    return this;
  }

  public Long getJobId() {
    return jobId;
  }

  public TaskDTO setJobId(Long jobId) {
    this.jobId = jobId;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TaskDTO)) {
      return false;
    }
    TaskDTO af = (TaskDTO) o;
    return Objects.equals(getId(), af.getId()) && Objects.equals(status, af.getStatus())
        && Objects.equals(startTime, af.getStartTime()) && Objects.equals(endTime, af.getEndTime())
        && Objects.equals(taskInfo, af.getTaskInfo());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), status, startTime, endTime, taskInfo);
  }
}
