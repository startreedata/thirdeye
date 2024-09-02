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
import ai.startree.thirdeye.notification.NotificationTaskFilter;
import ai.startree.thirdeye.notification.NotificationTaskPostProcessor;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSpecDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.worker.task.DetectionAlertTaskInfo;
import ai.startree.thirdeye.worker.task.TaskContext;
import ai.startree.thirdeye.worker.task.TaskResult;
import ai.startree.thirdeye.worker.task.TaskRunner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
  private final NotificationDispatcher notificationDispatcher;
  private final NotificationPayloadBuilder notificationPayloadBuilder;
  private final NotificationTaskFilter notificationTaskFilter;
  private final NotificationTaskPostProcessor notificationTaskPostProcessor;

  private final Timer notificationTaskTimerOfSuccess;
  private final Timer notificationTaskTimerOfException;

  @Inject
  public NotificationTaskRunner(
      final SubscriptionGroupManager subscriptionGroupManager,
      final NotificationDispatcher notificationDispatcher,
      final NotificationPayloadBuilder notificationPayloadBuilder,
      final NotificationTaskFilter notificationTaskFilter,
      final NotificationTaskPostProcessor notificationTaskPostProcessor) {
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.notificationDispatcher = notificationDispatcher;
    this.notificationPayloadBuilder = notificationPayloadBuilder;
    this.notificationTaskFilter = notificationTaskFilter;
    this.notificationTaskPostProcessor = notificationTaskPostProcessor;

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
      LOG.warn("Detection alert {} contains no properties", id);
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
          final SubscriptionGroupDTO sg = getSubscriptionGroupDTO(subscriptionGroupId);
          executeInternal(sg);
          return Collections.emptyList();
        },
        notificationTaskTimerOfSuccess, 
        notificationTaskTimerOfException);
  }

  private void executeInternal(final SubscriptionGroupDTO sg) {
    requireNonNull(sg, "subscription Group is null");

    final long now = System.currentTimeMillis();
    final var result = notificationTaskFilter.filter(sg, now);

    /* Dispatch notifications */
    final NotificationPayloadApi payload = notificationPayloadBuilder.build(result);
    if (payload == null) {
      LOG.debug("Subscription group: {} "
              + "has no anomalies to notify and no completed anomalies to notify",
          sg.getId());
      return;
    }

    /* fire notifications */
    final Map<NotificationSpecDTO, Exception> specToException = notificationDispatcher.dispatch(sg, payload);

    /* post process, Update watermarks, etc once notification is successfully sent */
    notificationTaskPostProcessor.postProcess(result, specToException);
  }
}
