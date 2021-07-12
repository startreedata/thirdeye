package org.apache.pinot.thirdeye.spi.api;

import java.sql.Timestamp;
import org.apache.pinot.thirdeye.spi.task.TaskConstants;

public class TaskApi implements ThirdEyeCrudApi<TaskApi> {

  private Long id;
  private TaskConstants.TaskType taskType;
  private Long workerId;
  private Long jobId;
  private String jobName;
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

  public void setWorkerId(Long workerId) {
    this.workerId = workerId;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public String getJobName() {
    return jobName;
  }

  public TaskConstants.TaskStatus getStatus() {
    return status;
  }

  public void setStatus(TaskConstants.TaskStatus status) {
    this.status = status;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public String getTaskInfo() {
    return taskInfo;
  }

  public void setTaskInfo(String taskInfo) {
    this.taskInfo = taskInfo;
  }

  public TaskConstants.TaskType getTaskType() {
    return taskType;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setTaskType(TaskConstants.TaskType taskType) {
    this.taskType = taskType;
  }

  public Timestamp getLastModified() {
    return lastModified;
  }

  public void setLastModified(Timestamp lastModified) {
    this.lastModified = lastModified;
  }

  public Long getJobId() {
    return jobId;
  }

  public void setJobId(Long jobId) {
    this.jobId = jobId;
  }
}
