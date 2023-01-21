/*
 * Copyright 2023 StarTree Inc
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

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalySubscriptionGroupNotificationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionPipelineTaskInfo;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import org.quartz.JobKey;

@Singleton
public class JobSchedulerService {

  private final TaskManager taskManager;
  private final AlertManager alertManager;
  private final AnomalyManager anomalyManager;
  private final AnomalySubscriptionGroupNotificationManager notificationManager;

  @Inject
  public JobSchedulerService(final TaskManager taskManager,
      final AlertManager alertManager,
      final AnomalyManager anomalyManager,
      final AnomalySubscriptionGroupNotificationManager notificationManager) {
    this.taskManager = taskManager;
    this.alertManager = alertManager;
    this.anomalyManager = anomalyManager;
    this.notificationManager = notificationManager;
  }

  public boolean taskAlreadyRunning(final String jobName) {
    List<TaskDTO> scheduledTasks = taskManager.findByPredicate(Predicate.AND(
        Predicate.EQ("name", jobName),
        Predicate.OR(
            Predicate.EQ("status", TaskStatus.RUNNING.toString()),
            Predicate.EQ("status", TaskStatus.WAITING.toString())
        ))
    );
    return !scheduledTasks.isEmpty();
  }

  public DetectionPipelineTaskInfo buildTaskInfo(final JobKey jobKey, final long endTime) {
    final Long id = getIdFromJobKey(jobKey.getName());
    final AlertDTO alert = alertManager.findById(id);

    if (alert == null) {
      // no task needs to be created if alert does not exist!
      return null;
    }

    // start and end are corrected with delay and granularity at execution time
    long start = alert.getLastTimestamp();
    return new DetectionPipelineTaskInfo(alert.getId(), start, endTime);
  }

  public static Long getIdFromJobKey(String jobKey) {
    final String[] tokens = jobKey.split("_");
    final String id = tokens[tokens.length - 1];
    return Long.valueOf(id);
  }

  /**
   * Check if we need to create a subscription task.
   * If there is no anomaly generated (by looking at anomaly create_time) between last notification
   * time
   * till now (left inclusive, right exclusive) then no need to create this task.
   *
   * Even if an anomaly gets merged (end_time updated) it will not renotify this anomaly as the
   * create_time is not
   * modified.
   * For example, if previous anomaly is from t1 to t2 generated at t3, then the timestamp in
   * vectorLock is t3.
   * If there is a new anomaly from t2 to t4 generated at t5, then we can still get this anomaly as
   * t5 > t3.
   *
   * Also, check if there is any anomaly that needs re-notifying
   *
   * @param configDTO The Subscription Configuration.
   * @return true if it needs notification task. false otherwise.
   */
  public boolean needNotification(final SubscriptionGroupDTO configDTO) {
    Map<Long, Long> vectorClocks = configDTO.getVectorClocks();
    if (vectorClocks == null || vectorClocks.size() == 0) {
      return true;
    }
    for (Map.Entry<Long, Long> e : vectorClocks.entrySet()) {
      long configId = e.getKey();
      long lastNotifiedTime = e.getValue();
      if (anomalyManager.findByCreatedTimeInRangeAndDetectionConfigId(lastNotifiedTime,
              System.currentTimeMillis(), configId)
          .stream().anyMatch(x -> !x.isChild())) {
        return true;
      }
    }
    // in addition to checking the watermarks, check if any anomalies need to be re-notified by querying the anomaly subscription group notification table
    List<AnomalySubscriptionGroupNotificationDTO> anomalySubscriptionGroupNotifications =
        notificationManager.findByPredicate(
            Predicate.IN("detectionConfigId", vectorClocks.keySet().toArray()));
    return !anomalySubscriptionGroupNotifications.isEmpty();
  }
}
