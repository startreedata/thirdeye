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

package ai.startree.thirdeye.detection.anomaly.monitor;

import ai.startree.thirdeye.detection.anomaly.job.JobRunner;
import ai.startree.thirdeye.spi.Constants.JobStatus;
import ai.startree.thirdeye.spi.datalayer.bao.JobManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.JobDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.task.MonitorTaskInfo;
import ai.startree.thirdeye.task.TaskGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorJobRunner implements JobRunner {

  private static final Logger LOG = LoggerFactory.getLogger(MonitorJobRunner.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final JobManager jobDAO;
  private final TaskManager taskDAO;
  private final TaskGenerator taskGenerator;
  private final MonitorJobContext monitorJobContext;

  public MonitorJobRunner(MonitorJobContext monitorJobContext) {
    this.monitorJobContext = monitorJobContext;
    this.jobDAO = monitorJobContext.getJobDAO();
    this.taskDAO = monitorJobContext.getTaskDAO();

    taskGenerator = new TaskGenerator();
  }

  @Override
  public void run() {
    try {
      LOG.info("Starting monitor job");

      monitorJobContext.setJobName(TaskType.MONITOR.toString());
      Long jobExecutionId = createJob();
      if (jobExecutionId != null) {
        monitorJobContext.setJobExecutionId(jobExecutionId);
        List<Long> taskIds = createTasks();
      }
    } catch (Exception e) {
      LOG.error("Exception in monitor job runner", e);
    }
  }

  public Long createJob() {
    Long jobExecutionId = null;
    try {

      LOG.info("Creating monitor job");
      JobDTO jobSpec = new JobDTO();
      jobSpec.setJobName(monitorJobContext.getJobName());
      jobSpec.setScheduleStartTime(System.currentTimeMillis());
      jobSpec.setStatus(JobStatus.SCHEDULED);
      jobSpec.setTaskType(TaskType.MONITOR);
      jobSpec.setConfigId(0); // Monitor job does not have a config id
      jobExecutionId = jobDAO.save(jobSpec);
      LOG.info("Created JobSpec {} with jobExecutionId {}", jobSpec,
          jobExecutionId);
    } catch (Exception e) {
      LOG.error("Exception in creating monitor job", e);
    }
    return jobExecutionId;
  }

  public List<Long> createTasks() {
    List<Long> taskIds = new ArrayList<>();
    try {
      LOG.info("Creating monitor tasks");
      List<MonitorTaskInfo> monitorTasks = taskGenerator.createMonitorTasks(monitorJobContext);
      LOG.info("Monitor tasks {}", monitorTasks);
      for (MonitorTaskInfo taskInfo : monitorTasks) {
        String taskInfoJson = null;
        try {
          taskInfoJson = OBJECT_MAPPER.writeValueAsString(taskInfo);
        } catch (JsonProcessingException e) {
          LOG.error("Exception when converting MonitorTaskInfo {} to jsonString", taskInfo, e);
        }

        TaskDTO taskSpec = new TaskDTO();
        taskSpec.setTaskType(TaskType.MONITOR);
        taskSpec.setJobName(monitorJobContext.getJobName());
        taskSpec.setStatus(TaskStatus.WAITING);
        taskSpec.setTaskInfo(taskInfoJson);
        taskSpec.setJobId(monitorJobContext.getJobExecutionId());
        long taskId = taskDAO.save(taskSpec);
        taskIds.add(taskId);
        LOG.info("Created monitorTask {} with taskId {}", taskSpec, taskId);
      }
    } catch (Exception e) {
      LOG.error("Exception in creating anomaly tasks", e);
    }
    return taskIds;
  }
}
