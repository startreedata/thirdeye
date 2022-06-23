/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import java.sql.Timestamp;
import java.util.Date;

public class TaskApi implements ThirdEyeCrudApi<TaskApi> {

  private Long id;
  private Date created;
  private Date updated;
  private TaskType taskType;
  private Long workerId;
  private JobApi job;
  private TaskStatus status;
  private long startTime;
  private long endTime;
  // A JSON string of the task info such as anomaly function, monitoring windows, etc.
  private String taskInfo;
  // The task results, which could contain the error messages of tasks' execution.
  private String message;
  private Timestamp lastModified;
  private Timestamp lastActive;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public TaskApi setId(Long id) {
    this.id = id;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public TaskApi setCreated(final Date created) {
    this.created = created;
    return this;
  }

  public Date getUpdated() {
    return updated;
  }

  public TaskApi setUpdated(final Date updated) {
    this.updated = updated;
    return this;
  }

  public Long getWorkerId() {
    return workerId;
  }

  public TaskApi setWorkerId(Long workerId) {
    this.workerId = workerId;
    return this;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public TaskApi setStatus(TaskStatus status) {
    this.status = status;
    return this;
  }

  public long getStartTime() {
    return startTime;
  }

  public TaskApi setStartTime(long startTime) {
    this.startTime = startTime;
    return this;
  }

  public long getEndTime() {
    return endTime;
  }

  public TaskApi setEndTime(long endTime) {
    this.endTime = endTime;
    return this;
  }

  public String getTaskInfo() {
    return taskInfo;
  }

  public TaskApi setTaskInfo(String taskInfo) {
    this.taskInfo = taskInfo;
    return this;
  }

  public TaskType getTaskType() {
    return taskType;
  }

  public String getMessage() {
    return message;
  }

  public TaskApi setMessage(String message) {
    this.message = message;
    return this;
  }

  public TaskApi setTaskType(TaskType taskType) {
    this.taskType = taskType;
    return this;
  }

  public Timestamp getLastModified() {
    return lastModified;
  }

  public TaskApi setLastModified(Timestamp lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  public Timestamp getLastActive() {
    return lastActive;
  }

  public TaskApi setLastActive(final Timestamp lastActive) {
    this.lastActive = lastActive;
    return this;
  }

  public JobApi getJob() {
    return job;
  }

  public TaskApi setJob(JobApi job) {
    this.job = job;
    return this;
  }
}
