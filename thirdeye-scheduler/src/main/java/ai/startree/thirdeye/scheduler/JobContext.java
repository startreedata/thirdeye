/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.scheduler;

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
