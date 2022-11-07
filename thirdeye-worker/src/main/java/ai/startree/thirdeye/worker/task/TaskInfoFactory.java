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
package ai.startree.thirdeye.worker.task;

import static ai.startree.thirdeye.spi.task.TaskType.DETECTION;
import static ai.startree.thirdeye.spi.task.TaskType.MONITOR;
import static ai.startree.thirdeye.spi.task.TaskType.NOTIFICATION;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.dto.DetectionPipelineTaskInfo;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.spi.task.TaskType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class returns deserializes the task info json and returns the TaskInfo,
 * depending on the task type
 */
public class TaskInfoFactory {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Logger LOG = LoggerFactory.getLogger(TaskInfoFactory.class);

  private static final ImmutableMap<TaskType, Class<? extends TaskInfo>> TASK_TYPE_POJO_MAP =
      ImmutableMap.<TaskType, Class<? extends TaskInfo>>builder()
          .put(DETECTION, DetectionPipelineTaskInfo.class)
          .put(NOTIFICATION, DetectionAlertTaskInfo.class)
          .put(MONITOR, MonitorTaskInfo.class)
          .build();

  public static TaskInfo get(TaskType taskType, String jsonPayload) throws JsonProcessingException {
    final Class<? extends TaskInfo> clazz = TASK_TYPE_POJO_MAP.get(taskType);
    requireNonNull(clazz,
        String.format("Invalid TaskType. Allowed: %s", TASK_TYPE_POJO_MAP.keySet()));

    return OBJECT_MAPPER.readValue(jsonPayload, clazz);
  }
}
