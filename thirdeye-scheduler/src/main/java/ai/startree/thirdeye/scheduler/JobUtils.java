/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.scheduler;

import ai.startree.thirdeye.spi.task.TaskType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.quartz.JobKey;

public class JobUtils {
  // TODO cyril add to grafana dashboard
  // count the number of times the *creation* of a task failed. Should always be zero.  
  public static final Map<TaskType, Counter> FAILED_TASK_CREATION_COUNTERS =
      Arrays.stream(TaskType.values()).collect(Collectors.toMap(
          t -> t,
          t -> Metrics.counter("thirdeye_failed_task_creation_total", "task_type",
              t.toString())));

  // count the number of times a task is not created because the same task is already WAITING or RUNNING. 
  public static final Map<TaskType, Counter> BACKPRESSURE_COUNTERS =
      Arrays.stream(TaskType.values()).collect(Collectors.toMap(
          t -> t,
          t -> Metrics.counter("thirdeye_scheduler_backpressure_total", "task_type",
              t.toString())));

  public static Long getIdFromJobKey(JobKey jobKey) {
    final String[] tokens = jobKey.getName().split("_");
    final String id = tokens[tokens.length - 1];
    return Long.valueOf(id);
  }
}
