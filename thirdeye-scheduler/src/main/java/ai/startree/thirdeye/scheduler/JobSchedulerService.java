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

import static ai.startree.thirdeye.alert.AlertDetectionIntervalCalculator.getDateTimeZone;
import static ai.startree.thirdeye.spi.Constants.DEFAULT_CHRONOLOGY;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.spi.datalayer.AnomalyFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertMetadataDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalySubscriptionGroupNotificationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionPipelineTaskInfo;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.util.TimeUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JobSchedulerService {

  private static final Logger LOG = LoggerFactory.getLogger(JobSchedulerService.class);
  public static final Interval UNUSED_DETECTION_INTERVAL = new Interval(0, 0, DEFAULT_CHRONOLOGY);
  private final TaskManager taskManager;
  private final AlertManager alertManager;
  private final AnomalyManager anomalyManager;
  private final AnomalySubscriptionGroupNotificationManager notificationManager;
  private final AlertTemplateRenderer alertTemplateRenderer;

  @Inject
  public JobSchedulerService(final TaskManager taskManager,
      final AlertManager alertManager,
      final AnomalyManager anomalyManager,
      final AnomalySubscriptionGroupNotificationManager notificationManager,
      final AlertTemplateRenderer alertTemplateRenderer) {
    this.taskManager = taskManager;
    this.alertManager = alertManager;
    this.anomalyManager = anomalyManager;
    this.notificationManager = notificationManager;
    this.alertTemplateRenderer = alertTemplateRenderer;
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

    final long start = computeTaskStart(alert, endTime);
    return new DetectionPipelineTaskInfo(alert.getId(), start, endTime);
  }

  @VisibleForTesting
  protected long computeTaskStart(final AlertDTO alert, final long endTime) {
    try {
      final AlertTemplateDTO templateWithProperties = alertTemplateRenderer.renderAlert(alert,
          UNUSED_DETECTION_INTERVAL);
      final Chronology chronology = optional(getDateTimeZone(templateWithProperties)).orElse(
          DEFAULT_CHRONOLOGY);
      final DateTime defaultStartTime = new DateTime(alert.getLastTimestamp(), chronology);
      final DateTime endDateTime = new DateTime(endTime, chronology);
      final Period mutabilityPeriod = getMutabilityPeriod(templateWithProperties);
      final DateTime mutabilityStart = endDateTime.minus(mutabilityPeriod);
      if (mutabilityStart.isBefore(defaultStartTime)) {
        LOG.info(
            "Applied mutability period of {} for alert id {} between {} and {}. Corrected task interval is between {} and {}",
            mutabilityPeriod,
            alert.getId(),
            defaultStartTime,
            endDateTime,
            mutabilityStart,
            endDateTime
        );
        return mutabilityStart.getMillis();
      } else {
        return defaultStartTime.getMillis();
      }
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
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
      if (anomalyManager.filter(new AnomalyFilter()
              .setCreateTimeWindow(new Interval(lastNotifiedTime, System.currentTimeMillis()))
              .setAlertId(configId))
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

  @NonNull
  private static Period getMutabilityPeriod(final AlertTemplateDTO templateWithProperties) {
    return optional(templateWithProperties.getMetadata())
        .map(AlertMetadataDTO::getDataset)
        .map(DatasetConfigDTO::getMutabilityPeriod)
        .map(TimeUtils::isoPeriod)
        .orElse(Period.ZERO);
  }
}
