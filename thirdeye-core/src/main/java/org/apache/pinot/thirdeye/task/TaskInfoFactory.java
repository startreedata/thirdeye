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

package org.apache.pinot.thirdeye.task;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.task.TaskType.DATA_QUALITY;
import static org.apache.pinot.thirdeye.spi.task.TaskType.DETECTION;
import static org.apache.pinot.thirdeye.spi.task.TaskType.MONITOR;
import static org.apache.pinot.thirdeye.spi.task.TaskType.NOTIFICATION;
import static org.apache.pinot.thirdeye.spi.task.TaskType.ONBOARDING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.pinot.thirdeye.spi.task.TaskInfo;
import org.apache.pinot.thirdeye.spi.task.TaskType;
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
          .put(DATA_QUALITY, DetectionPipelineTaskInfo.class)
          .put(DETECTION, DetectionPipelineTaskInfo.class)
          .put(NOTIFICATION, DetectionAlertTaskInfo.class)
          .put(ONBOARDING, OnboardingTaskInfo.class)
          .put(MONITOR, MonitorTaskInfo.class)
          .build();

  public static TaskInfo get(TaskType taskType, String jsonPayload) throws JsonProcessingException {
    final Class<? extends TaskInfo> clazz = TASK_TYPE_POJO_MAP.get(taskType);
    requireNonNull(clazz,
        String.format("Invalid TaskType. Allowed: %s", TASK_TYPE_POJO_MAP.keySet()));

    return OBJECT_MAPPER.readValue(jsonPayload, clazz);
  }
}
