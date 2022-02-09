/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package ai.startree.thirdeye.spi.detection.health;

import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The detection task status for a detection config
 */
public class DetectionTaskStatus {

  // the task success rate for the detection config
  @JsonProperty
  private final double taskSuccessRate;

  // the health status for the detection tasks
  @JsonProperty
  private final HealthStatus healthStatus;

  // the time stamp of last successfully finishing task
  @JsonProperty
  private final Long lastTaskExecutionTime;

  public Long getLastTaskExecutionTime() {
    return lastTaskExecutionTime;
  }

  // the counting for detection task status
  @JsonProperty
  private final Map<TaskStatus, Long> taskCounts = new HashMap<TaskStatus, Long>() {{
    put(TaskStatus.COMPLETED, 0L);
    put(TaskStatus.FAILED, 0L);
    put(TaskStatus.WAITING, 0L);
    put(TaskStatus.TIMEOUT, 0L);
  }};

  // the list of tasks for the detection config
  @JsonProperty
  private final List<TaskDTO> tasks;

  private static final double TASK_SUCCESS_RATE_BAD_THRESHOLD = 0.2;
  private static final double TASK_SUCCESS_RATE_MODERATE_THRESHOLD = 0.8;

  public DetectionTaskStatus(double taskSuccessRate, HealthStatus healthStatus,
      Map<TaskStatus, Long> counts, List<TaskDTO> tasks, long lastTaskExecutionTime) {
    this.taskSuccessRate = taskSuccessRate;
    this.healthStatus = healthStatus;
    this.tasks = tasks;
    this.taskCounts.putAll(counts);
    this.lastTaskExecutionTime = lastTaskExecutionTime;
  }

  // default constructor for deserialization
  public DetectionTaskStatus() {
    this.taskSuccessRate = Double.NaN;
    this.healthStatus = HealthStatus.UNKNOWN;
    this.tasks = Collections.emptyList();
    this.lastTaskExecutionTime = -1L;
  }

  public double getTaskSuccessRate() {
    return taskSuccessRate;
  }

  public HealthStatus getHealthStatus() {
    return healthStatus;
  }

  public List<TaskDTO> getTasks() {
    return tasks;
  }

  public Map<TaskStatus, Long> getTaskCounts() {
    return taskCounts;
  }

  public static DetectionTaskStatus fromTasks(List<TaskDTO> tasks, long lastTaskExecutionTime) {
    // count the number of tasks by task status
    tasks.sort(Comparator.comparingLong(TaskDTO::getStartTime).reversed());
    Map<TaskStatus, Long> counts =
        tasks.stream().collect(Collectors.groupingBy(TaskDTO::getStatus, Collectors.counting()));
    double taskSuccessRate = getTaskSuccessRate(counts);
    long newTaskExecutionTime = getLastSuccessTaskExecutionTime(tasks);
    newTaskExecutionTime =
        newTaskExecutionTime == -1L ? lastTaskExecutionTime : newTaskExecutionTime;
    return new DetectionTaskStatus(taskSuccessRate, classifyTaskStatus(taskSuccessRate), counts,
        tasks, newTaskExecutionTime);
  }

  /**
   * Create a Detection task status from a list of tasks
   *
   * @param tasks the list of tasks
   * @param lastTaskExecutionTime the last task exeuction time
   * @param taskLimit the number of tasks should be returned in the task status
   * @return the DetectionTaskStatus
   */
  public static DetectionTaskStatus fromTasks(List<TaskDTO> tasks, long lastTaskExecutionTime,
      long taskLimit) {
    // count the number of tasks by task status
    tasks.sort(Comparator.comparingLong(TaskDTO::getStartTime).reversed());
    Map<TaskStatus, Long> counts =
        tasks.stream().collect(Collectors.groupingBy(TaskDTO::getStatus, Collectors.counting()));
    double taskSuccessRate = getTaskSuccessRate(counts);
    long newTaskExecutionTime = getLastSuccessTaskExecutionTime(tasks);
    newTaskExecutionTime =
        newTaskExecutionTime == -1L ? lastTaskExecutionTime : newTaskExecutionTime;
    tasks = tasks.stream().limit(taskLimit).collect(Collectors.toList());
    return new DetectionTaskStatus(taskSuccessRate, classifyTaskStatus(taskSuccessRate), counts,
        tasks,
        newTaskExecutionTime);
  }

  private static Long getLastSuccessTaskExecutionTime(List<TaskDTO> tasks) {
    return tasks.stream()
        .filter(task -> task.getStatus().equals(TaskStatus.COMPLETED))
        .map(TaskDTO::getEndTime)
        .findFirst()
        .orElse(-1L);
  }

  private static double getTaskSuccessRate(Map<TaskStatus, Long> counts) {
    if (counts.size() != 0) {
      long completedTasks = counts.getOrDefault(TaskStatus.COMPLETED, 0L);
      long failedTasks = counts.getOrDefault(TaskStatus.FAILED, 0L);
      long timeoutTasks = counts.getOrDefault(TaskStatus.TIMEOUT, 0L);
      long waitingTasks = counts.getOrDefault(TaskStatus.WAITING, 0L);
      return (double) completedTasks / (failedTasks + timeoutTasks + completedTasks + waitingTasks);
    }
    return Double.NaN;
  }

  private static HealthStatus classifyTaskStatus(double taskSuccessRate) {
    if (Double.isNaN(taskSuccessRate)) {
      return HealthStatus.UNKNOWN;
    }
    if (taskSuccessRate < TASK_SUCCESS_RATE_BAD_THRESHOLD) {
      return HealthStatus.BAD;
    }
    if (taskSuccessRate < TASK_SUCCESS_RATE_MODERATE_THRESHOLD) {
      return HealthStatus.MODERATE;
    }
    return HealthStatus.GOOD;
  }
}
