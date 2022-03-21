/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Holds utility functions related to ThirdEye Tasks
 */
public class TaskUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static Long getIdFromJobKey(String jobKey) {
    final String[] tokens = jobKey.split("_");
    final String id = tokens[tokens.length - 1];
    return Long.valueOf(id);
  }

  public static TaskDTO createTaskDto(final long id, final TaskInfo taskInfo,
      final TaskType taskType)
      throws JsonProcessingException {
    final String taskInfoJson;
    taskInfoJson = OBJECT_MAPPER.writeValueAsString(taskInfo);

    return new TaskDTO()
        .setTaskType(taskType)
        .setJobName(taskType.toString() + "_" + id)
        .setStatus(TaskStatus.WAITING)
        .setTaskInfo(taskInfoJson);
  }
}
