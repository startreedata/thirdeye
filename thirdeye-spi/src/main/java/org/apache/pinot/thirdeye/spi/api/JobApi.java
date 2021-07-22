package org.apache.pinot.thirdeye.spi.api;

import java.sql.Timestamp;
import org.apache.pinot.thirdeye.spi.Constants.JobStatus;
import org.apache.pinot.thirdeye.spi.task.TaskType;

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
