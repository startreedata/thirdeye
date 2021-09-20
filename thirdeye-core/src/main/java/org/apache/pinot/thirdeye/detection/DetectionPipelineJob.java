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

package org.apache.pinot.thirdeye.detection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.scheduler.ThirdEyeAbstractJob;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskDTO;
import org.apache.pinot.thirdeye.spi.task.TaskType;
import org.apache.pinot.thirdeye.task.DetectionPipelineTaskInfo;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectionPipelineJob extends ThirdEyeAbstractJob {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelineJob.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final long DETECTION_TASK_TIMEOUT = TimeUnit.DAYS.toMillis(1);

  @Override
  public void execute(JobExecutionContext ctx) {
    final DetectionPipelineTaskInfo taskInfo = TaskUtils.buildTaskInfo(ctx,
        getInstance(ctx, ThirdEyeCacheRegistry.class),
        getInstance(ctx, AlertManager.class),
        getInstance(ctx, DatasetConfigManager.class),
        getInstance(ctx, MetricConfigManager.class));

    final TaskManager taskManager = getInstance(ctx, TaskManager.class);

    // if a task is pending and not time out yet, don't schedule more
    String jobName = String.format("%s_%d", TaskType.DETECTION,
        taskInfo.getConfigId());
    if (TaskUtils.checkTaskAlreadyRun(jobName, taskInfo, DETECTION_TASK_TIMEOUT, taskManager)) {
      LOG.info(
          "Skip scheduling detection task for {} with start time {}. Task is already in the queue.",
          jobName,
          taskInfo.getStart());
      return;
    }

    String taskInfoJson = null;
    try {
      taskInfoJson = OBJECT_MAPPER.writeValueAsString(taskInfo);
    } catch (JsonProcessingException e) {
      LOG.error("Exception when converting DetectionPipelineTaskInfo {} to jsonString", taskInfo,
          e);
    }

    TaskDTO taskDTO = TaskUtils
        .buildTask(taskInfo.getConfigId(), taskInfoJson, TaskType.DETECTION);
    long taskId = taskManager.save(taskDTO);
    LOG.info("Created {} task {} with taskId {}", TaskType.DETECTION, taskDTO,
        taskId);
  }
}


