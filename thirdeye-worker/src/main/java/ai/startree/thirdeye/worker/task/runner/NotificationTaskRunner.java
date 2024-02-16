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
package ai.startree.thirdeye.worker.task.runner;

import static ai.startree.thirdeye.spi.util.MetricsUtils.record;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.notification.NotificationDispatcher;
import ai.startree.thirdeye.notification.NotificationPayloadBuilder;
import ai.startree.thirdeye.notification.SubscriptionGroupFilter;
import ai.startree.thirdeye.notification.SubscriptionGroupWatermarkManager;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.worker.task.DetectionAlertTaskInfo;
import ai.startree.thirdeye.worker.task.TaskContext;
import ai.startree.thirdeye.worker.task.TaskResult;
import ai.startree.thirdeye.worker.task.TaskRunner;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Detection alert task runner. This runner looks for the new anomalies and run the detection
 * alert filter to get mappings from anomalies to recipients and then send email to the recipients.
 */
@Singleton
public class NotificationTaskRunner implements TaskRunner {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationTaskRunner.class);

  private final SubscriptionGroupManager subscriptionGroupManager;
  private final AnomalyManager anomalyManager;
  private final NotificationDispatcher notificationDispatcher;
  private final NotificationPayloadBuilder notificationPayloadBuilder;
  private final SubscriptionGroupFilter subscriptionGroupFilter;
  private final SubscriptionGroupWatermarkManager subscriptionGroupWatermarkManager;

  @Deprecated
  private final Counter notificationTaskSuccessCounter;
  @Deprecated
  private final Counter notificationTaskCounter;
  @Deprecated
  private final Histogram notificationTaskDuration;
  private final Timer notificationTaskTimerOfSuccess;
  private final Timer notificationTaskTimerOfException;

  @Inject
  public NotificationTaskRunner(
      final SubscriptionGroupManager subscriptionGroupManager,
      final AnomalyManager anomalyManager,
      final MetricRegistry metricRegistry,
      final NotificationDispatcher notificationDispatcher,
      final NotificationPayloadBuilder notificationPayloadBuilder,
      final SubscriptionGroupFilter subscriptionGroupFilter,
      final SubscriptionGroupWatermarkManager subscriptionGroupWatermarkManager) {
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.anomalyManager = anomalyManager;
    this.notificationDispatcher = notificationDispatcher;
    this.notificationPayloadBuilder = notificationPayloadBuilder;
    this.subscriptionGroupFilter = subscriptionGroupFilter;
    this.subscriptionGroupWatermarkManager = subscriptionGroupWatermarkManager;

    // deprecated - use thirdeye_notification_task
    notificationTaskCounter = metricRegistry.counter("notificationTaskCounter");
    // deprecated - use thirdeye_notification_task
    notificationTaskSuccessCounter = metricRegistry.counter("notificationTaskSuccessCounter");
    // deprecated - use thirdeye_notification_task
    notificationTaskDuration = metricRegistry.histogram("notificationTaskDuration");

    final String description = "Start: a task is started from an input subscription group id. End: the task execution is finished. Tag exception=true means an exception was thrown by the method call.";
    this.notificationTaskTimerOfSuccess = Timer.builder("thirdeye_notification_task")
        .description(description)
        .publishPercentiles(Constants.METRICS_TIMER_PERCENTILES)
        .tag("exception", "false")
        .register(Metrics.globalRegistry);
    this.notificationTaskTimerOfException = Timer.builder("thirdeye_notification_task")
        .description(description)
        .publishPercentiles(Constants.METRICS_TIMER_PERCENTILES)
        .tag("exception", "true")
        .register(Metrics.globalRegistry);
  }

  private SubscriptionGroupDTO getSubscriptionGroupDTO(final long id) {
    final SubscriptionGroupDTO sg = requireNonNull(subscriptionGroupManager.findById(id),
        "Cannot find subscription group: " + id);

    if (sg.getProperties() == null) {
      LOG.warn(String.format("Detection alert %d contains no properties", id));
    }
    return sg;
  }

  @Override
  public List<TaskResult> execute(final TaskInfo taskInfo, final TaskContext taskContext)
      throws Exception {
    return execute(((DetectionAlertTaskInfo) taskInfo).getDetectionAlertConfigId());
  }

  public List<TaskResult> execute(final long subscriptionGroupId) throws Exception {
    return record(
        () -> {
          final long tStart = System.currentTimeMillis();
          notificationTaskCounter.inc();

          final SubscriptionGroupDTO sg = getSubscriptionGroupDTO(subscriptionGroupId);

          executeInternal(sg);
          notificationTaskSuccessCounter.inc();
          notificationTaskDuration.update(System.currentTimeMillis() - tStart);
          return Collections.emptyList();
        },
        notificationTaskTimerOfSuccess, 
        notificationTaskTimerOfException);
  }

  private void executeInternal(final SubscriptionGroupDTO subscriptionGroup) {
    requireNonNull(subscriptionGroup, "subscription Group is null");
    final var anomalies = subscriptionGroupFilter.filter(
        subscriptionGroup,
        System.currentTimeMillis());

    if (anomalies.isEmpty()) {
      LOG.debug("Zero anomalies found, skipping notification for subscription group: {}",
          subscriptionGroup.getId());
      return;
    }

    /* Dispatch notifications */
    final NotificationPayloadApi payload = notificationPayloadBuilder.buildNotificationPayload(
        subscriptionGroup,
        anomalies);

    /* fire notifications */
    notificationDispatcher.dispatch(subscriptionGroup, payload);

    /* Update anomalies */
    for (final AnomalyDTO anomaly : anomalies) {
      anomalyManager.update(anomaly.setNotified(true));
    }
    /* Record watermarks */
    subscriptionGroupWatermarkManager.updateWatermarks(subscriptionGroup, anomalies);
  }
}
