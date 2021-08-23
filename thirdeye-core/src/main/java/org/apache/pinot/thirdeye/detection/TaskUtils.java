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

import static org.apache.pinot.thirdeye.util.ThirdEyeUtils.getDetectionExpectedDelay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.CoreConstants;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.TaskManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.TaskDTO;
import org.apache.pinot.thirdeye.spi.task.TaskStatus;
import org.apache.pinot.thirdeye.spi.task.TaskType;
import org.apache.pinot.thirdeye.task.DetectionPipelineTaskInfo;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds utility functions related to ThirdEye Tasks
 */
public class TaskUtils {

  private static final Logger LOG = LoggerFactory.getLogger(TaskUtils.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * Build a task with the specified type and properties
   */
  public static TaskDTO buildTask(long id, String taskInfoJson, TaskType taskType) {
    TaskDTO taskDTO = new TaskDTO();
    taskDTO.setTaskType(taskType);
    taskDTO.setJobName(taskType.toString() + "_" + id);
    taskDTO.setStatus(TaskStatus.WAITING);
    taskDTO.setTaskInfo(taskInfoJson);
    return taskDTO;
  }

  public static boolean checkTaskAlreadyRun(String jobName, DetectionPipelineTaskInfo taskInfo,
      long timeout, final TaskManager taskManager) {
    // check if a task for this detection pipeline is already scheduled
    List<TaskDTO> scheduledTasks = taskManager
        .findByPredicate(Predicate.AND(
            Predicate.EQ("name", jobName),
            Predicate.OR(
                Predicate.EQ("status", TaskStatus.RUNNING.toString()),
                Predicate.EQ("status", TaskStatus.WAITING.toString()))
            )
        );

    List<DetectionPipelineTaskInfo> scheduledTaskInfos = scheduledTasks.stream().map(taskDTO -> {
      try {
        return OBJECT_MAPPER.readValue(taskDTO.getTaskInfo(), DetectionPipelineTaskInfo.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).collect(Collectors.toList());
    Optional<DetectionPipelineTaskInfo> latestScheduledTask = scheduledTaskInfos.stream()
        .reduce((taskInfo1, taskInfo2) -> taskInfo1.getEnd() > taskInfo2.getEnd() ? taskInfo1
            : taskInfo2);
    return latestScheduledTask.isPresent()
        && taskInfo.getEnd() - latestScheduledTask.get().getEnd() < timeout;
  }

  public static Long getIdFromJobKey(String jobKey) {
    String[] tokens = jobKey.split("_");
    String id = tokens[tokens.length - 1];
    return Long.valueOf(id);
  }

  public static DetectionPipelineTaskInfo buildTaskInfo(JobExecutionContext jobExecutionContext,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry,
      final AlertManager detectionConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
    Long id = getIdFromJobKey(jobKey.getName());
    AlertDTO configDTO = detectionConfigManager.findById(id);

    return buildTaskInfoFromDetectionConfig(configDTO,
        System.currentTimeMillis(),
        thirdEyeCacheRegistry,
        datasetConfigManager,
        metricConfigManager);
  }

  public static DetectionPipelineTaskInfo buildTaskInfoFromDetectionConfig(AlertDTO alert,
      long end,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    long delay;
    try {
      delay = getDetectionExpectedDelay(alert,
          thirdEyeCacheRegistry,
          datasetConfigManager,
          metricConfigManager);
    } catch (Exception e) {
      LOG.error("Failed to calc delay", e);
      delay = 0;
    }
    final long start = Math.max(alert.getLastTimestamp(),
        end - CoreConstants.DETECTION_TASK_MAX_LOOKBACK_WINDOW - delay);
    return new DetectionPipelineTaskInfo(alert.getId(), start, end);
  }

  public static long createDetectionTask(DetectionPipelineTaskInfo taskInfo,
      final TaskManager taskManager) {
    return TaskUtils.createTask(TaskType.DETECTION, taskInfo,
        taskManager);
  }

  public static long createDataQualityTask(DetectionPipelineTaskInfo taskInfo,
      final TaskManager taskManager) {
    return TaskUtils.createTask(TaskType.DATA_QUALITY, taskInfo,
        taskManager);
  }

  /**
   * Creates a generic task and saves it.
   */
  public static long createTask(TaskType taskType,
      DetectionPipelineTaskInfo taskInfo, final TaskManager taskManager) {
    String taskInfoJson = null;
    try {
      taskInfoJson = OBJECT_MAPPER.writeValueAsString(taskInfo);
    } catch (JsonProcessingException e) {
      LOG.error("Exception when converting DetectionPipelineTaskInfo {} to jsonString", taskInfo,
          e);
    }

    TaskDTO taskDTO = TaskUtils.buildTask(taskInfo.getConfigId(), taskInfoJson, taskType);
    long id = taskManager.save(taskDTO);
    LOG.info("Created {} task {} with taskId {}", taskType, taskDTO, id);
    return id;
  }
}
