/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.job;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.JobManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;

public abstract class JobContext {

  // Todo : remove DAOs from here as we can inject these wherever needed.
  private JobManager jobDAO;
  private TaskManager taskDAO;
  private DatasetConfigManager datasetConfigDAO;

  private String jobName;
  private long jobExecutionId;

  public long getJobExecutionId() {
    return jobExecutionId;
  }

  public void setJobExecutionId(long jobExecutionId) {
    this.jobExecutionId = jobExecutionId;
  }

  public JobManager getJobDAO() {
    return jobDAO;
  }

  public void setJobDAO(JobManager jobDAO) {
    this.jobDAO = jobDAO;
  }

  public TaskManager getTaskDAO() {
    return taskDAO;
  }

  public void setTaskDAO(TaskManager taskDAO) {
    this.taskDAO = taskDAO;
  }

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public DatasetConfigManager getDatasetConfigDAO() {
    return datasetConfigDAO;
  }

  public void setDatasetConfigDAO(DatasetConfigManager datasetConfigDAO) {
    this.datasetConfigDAO = datasetConfigDAO;
  }
}
