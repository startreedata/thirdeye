/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.task.DetectionPipelineTaskInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds utility functions related to ThirdEye Tasks
 */
public class TaskUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Logger LOG = LoggerFactory.getLogger(TaskUtils.class);

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

  /**
   * Create a task in the DB with the specified properties
   *
   * @throws JsonProcessingException if jackson is not able to serialize the TaskInfo
   */
  public static long createTask(final TaskManager taskDAO, final long id, final TaskInfo taskInfo, final TaskType taskType)
      throws JsonProcessingException {
    final String taskInfoJson;
    taskInfoJson = OBJECT_MAPPER.writeValueAsString(taskInfo);

    final TaskDTO taskDTO = TaskUtils.buildTask(id, taskInfoJson, taskType);
    final long taskId = taskDAO.save(taskDTO);
    LOG.info("Created {} task {} with settings {}", taskType, taskId, taskDTO);
    return taskId;
  }

  /**
   * Build a task with the specified type and properties
   */
  private static TaskDTO buildTask(long id, String taskInfoJson, TaskType taskType) {
    TaskDTO taskDTO = new TaskDTO();
    taskDTO.setTaskType(taskType);
    taskDTO.setJobName(taskType.toString() + "_" + id);
    taskDTO.setStatus(TaskStatus.WAITING);
    taskDTO.setTaskInfo(taskInfoJson);
    return taskDTO;
  }
}
