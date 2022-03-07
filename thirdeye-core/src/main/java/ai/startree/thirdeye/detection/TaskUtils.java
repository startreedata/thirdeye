/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds utility functions related to ThirdEye Tasks
 */
public class TaskUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Logger LOG = LoggerFactory.getLogger(TaskUtils.class);

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
