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

import static ai.startree.thirdeye.spi.Constants.CTX_INJECTOR;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.task.TaskType;
import com.google.inject.Injector;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobUtils {

  private static final Logger LOG = LoggerFactory.getLogger(JobUtils.class);

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

  // FIXME CYRIL implement a custom JobFactory instead of getting the instance at execution time 
  public static <T> T getInstance(final JobExecutionContext context, Class<T> clazz) {
    final Injector injector = (Injector) getObjectFromContext(context, CTX_INJECTOR);
    return injector.getInstance(clazz);
  }

  private static Object getObjectFromContext(final JobExecutionContext context, final String key) {
    try {
      return requireNonNull(context.getScheduler().getContext().get(key));
    } catch (SchedulerException e) {
      final String message = String.format("Scheduler error. No key: %s", key);
      LOG.error(message, e);
      throw new RuntimeException(message, e);
    }
  }
}
