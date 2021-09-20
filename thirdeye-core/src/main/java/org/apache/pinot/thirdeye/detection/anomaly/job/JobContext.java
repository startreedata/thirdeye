/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.detection.anomaly.job;

import org.apache.pinot.thirdeye.spi.datalayer.bao.AnomalyFunctionManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.JobManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.TaskManager;

public abstract class JobContext {

  // Todo : remove DAOs from here as we can inject these wherever needed.
  private JobManager jobDAO;
  private TaskManager taskDAO;
  private AnomalyFunctionManager anomalyFunctionDAO;
  private DatasetConfigManager datasetConfigDAO;
  private MetricConfigManager metricConfigDAO;

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

  public AnomalyFunctionManager getAnomalyFunctionDAO() {
    return anomalyFunctionDAO;
  }

  public void setAnomalyFunctionDAO(AnomalyFunctionManager anomalyFunctionDAO2) {
    this.anomalyFunctionDAO = anomalyFunctionDAO2;
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

  public MetricConfigManager getMetricConfigDAO() {
    return metricConfigDAO;
  }

  public void setMetricConfigDAO(MetricConfigManager metricConfigDAO) {
    this.metricConfigDAO = metricConfigDAO;
  }
}
