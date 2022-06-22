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
