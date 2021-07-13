package org.apache.pinot.thirdeye.spi.api;

import java.sql.Timestamp;
import org.apache.pinot.thirdeye.spi.task.TaskConstants;

public class TaskApi implements ThirdEyeCrudApi<TaskApi> {

  private Long id;
  private TaskConstants.TaskType taskType;
  private Long workerId;
  private JobApi job;
  private TaskConstants.TaskStatus status;
  private long startTime;
  private long endTime;
  // A JSON string of the task info such as anomaly function, monitoring windows, etc.
  private String taskInfo;
  // The task results, which could contain the error messages of tasks' execution.
  private String message;
  private Timestamp lastModified;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public TaskApi setId(Long id) {
    this.id = id;
    return this;
  }

  public Long getWorkerId() {
    return workerId;
  }

  public TaskApi setWorkerId(Long workerId) {
    this.workerId = workerId;
    return this;
  }

  public TaskConstants.TaskStatus getStatus() {
    return status;
  }

  public TaskApi setStatus(TaskConstants.TaskStatus status) {
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

  public TaskConstants.TaskType getTaskType() {
    return taskType;
  }

  public String getMessage() {
    return message;
  }

  public TaskApi setMessage(String message) {
    this.message = message;
    return this;
  }

  public TaskApi setTaskType(TaskConstants.TaskType taskType) {
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

  public JobApi getJob() {
    return job;
  }

  public TaskApi setJob(JobApi job) {
    this.job = job;
    return this;
  }
}
