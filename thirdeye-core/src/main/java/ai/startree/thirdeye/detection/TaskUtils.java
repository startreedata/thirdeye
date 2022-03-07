/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.CoreConstants;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.task.DetectionPipelineTaskInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

  // fixme cyril move this to class and make private
  public static DetectionPipelineTaskInfo buildTaskInfo(JobExecutionContext jobExecutionContext,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry,
      final AlertManager detectionConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    final JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
    final Long id = getIdFromJobKey(jobKey.getName());
    final AlertDTO alert = detectionConfigManager.findById(id);

    if (alert == null) {
      // no task needs to be created if alert does not exist!
      return null;
    }

    return buildTaskInfoFromDetectionConfig(alert,
        jobExecutionContext.getScheduledFireTime().getTime(),
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

  /**
   * Get the expected delay for the detection pipeline.
   * This delay should be the longest of the expected delay of the underline datasets.
   *
   * @param config The detection config.
   * @return The expected delay for this alert in milliseconds.
   */
  public static long getDetectionExpectedDelay(AlertDTO config,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    long maxExpectedDelay = 0;

    // fixme cyril temp behavior - equivalent to current behavior
    // extractMetricUrnsFromProperties

    return maxExpectedDelay;
  }
}
