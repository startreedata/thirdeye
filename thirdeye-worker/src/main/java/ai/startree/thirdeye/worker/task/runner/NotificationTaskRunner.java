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
package ai.startree.thirdeye.worker.task.runner;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.notification.NotificationDispatcher;
import ai.startree.thirdeye.notification.NotificationPayloadBuilder;
import ai.startree.thirdeye.notification.NotificationSchemeFactory;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.subscriptiongroup.filter.SubscriptionGroupFilterResult;
import ai.startree.thirdeye.worker.task.DetectionAlertTaskInfo;
import ai.startree.thirdeye.worker.task.TaskContext;
import ai.startree.thirdeye.worker.task.TaskResult;
import ai.startree.thirdeye.worker.task.TaskRunner;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Detection alert task runner. This runner looks for the new anomalies and run the detection
 * alert filter to get
 * mappings from anomalies to recipients and then send email to the recipients.
 */
@Singleton
public class NotificationTaskRunner implements TaskRunner {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationTaskRunner.class);

  private final NotificationSchemeFactory notificationSchemeFactory;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final AnomalyManager anomalyManager;

  private final Counter notificationTaskSuccessCounter;
  private final Counter notificationTaskCounter;
  private final Histogram notificationTaskDuration;
  private final NotificationDispatcher notificationDispatcher;
  private final NotificationPayloadBuilder notificationPayloadBuilder;

  @Inject
  public NotificationTaskRunner(
      final NotificationSchemeFactory notificationSchemeFactory,
      final SubscriptionGroupManager subscriptionGroupManager,
      final AnomalyManager anomalyManager,
      final MetricRegistry metricRegistry,
      final NotificationDispatcher notificationDispatcher,
      final NotificationPayloadBuilder notificationPayloadBuilder) {
    this.notificationSchemeFactory = notificationSchemeFactory;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.anomalyManager = anomalyManager;

    notificationTaskCounter = metricRegistry.counter("notificationTaskCounter");
    notificationTaskSuccessCounter = metricRegistry.counter("notificationTaskSuccessCounter");
    notificationTaskDuration = metricRegistry.histogram("notificationTaskDuration");
    this.notificationDispatcher = notificationDispatcher;
    this.notificationPayloadBuilder = notificationPayloadBuilder;
  }

  private static Map<Long, Long> buildVectorClock(Collection<AnomalyDTO> anomalies) {
    final Map<Long, Long> alertIdToAnomalyCreateTimeMax = new HashMap<>();
    for (final AnomalyDTO a : anomalies) {
      final Long alertId = a.getDetectionConfigId();
      if (alertId == null) {
        continue;
      }
      final long createTime = optional(a.getCreateTime()).map(Timestamp::getTime).orElse(-1L);
      final long currentMax = alertIdToAnomalyCreateTimeMax.getOrDefault(alertId, createTime);
      final long newMax = Math.max(currentMax, createTime);
      alertIdToAnomalyCreateTimeMax.put(alertId, newMax);
    }
    return alertIdToAnomalyCreateTimeMax;
  }

  private static Map<Long, Long> mergeVectorClock(final Map<Long, Long> a,
      final Map<Long, Long> b) {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }
    final Map<Long, Long> result = new HashMap<>(a);
    b.forEach((key, value) -> result.merge(key, value, Math::max));

    return result;
  }

  private SubscriptionGroupDTO getSubscriptionGroupDTO(final long id) {
    final SubscriptionGroupDTO subscriptionGroupDTO = requireNonNull(
        subscriptionGroupManager.findById(id),
        "Cannot find subscription group: " + id);

    if (subscriptionGroupDTO.getProperties() == null) {
      LOG.warn(String.format("Detection alert %d contains no properties", id));
    }
    return subscriptionGroupDTO;
  }

  private void updateSubscriptionWatermarks(final SubscriptionGroupDTO subscriptionConfig,
      final List<AnomalyDTO> allAnomalies) {
    if (!allAnomalies.isEmpty()) {
      subscriptionConfig.setVectorClocks(
          mergeVectorClock(subscriptionConfig.getVectorClocks(),
              buildVectorClock(allAnomalies)));

      LOG.info("Updating watermarks for subscription config : {}", subscriptionConfig.getId());
      subscriptionGroupManager.save(subscriptionConfig);
    }
  }

  @Override
  public List<TaskResult> execute(final TaskInfo taskInfo, final TaskContext taskContext)
      throws Exception {
    return execute(((DetectionAlertTaskInfo) taskInfo).getDetectionAlertConfigId());
  }

  public List<TaskResult> execute(final long subscriptionGroupId) throws Exception {
    final long tStart = System.currentTimeMillis();
    notificationTaskCounter.inc();

    final SubscriptionGroupDTO subscriptionGroupDTO = getSubscriptionGroupDTO(subscriptionGroupId);

    executeInternal(subscriptionGroupDTO);
    notificationTaskSuccessCounter.inc();
    notificationTaskDuration.update(System.currentTimeMillis() - tStart);
    return Collections.emptyList();
  }

  private void executeInternal(final SubscriptionGroupDTO subscriptionGroup) throws Exception {
    final SubscriptionGroupFilterResult result = requireNonNull(notificationSchemeFactory
        .getDetectionAlertFilterResult(subscriptionGroup), "DetectionAlertFilterResult is null");

    if (result.getAllAnomalies().size() == 0) {
      LOG.debug("Zero anomalies found, skipping notification for subscription group: {}",
          subscriptionGroup.getId());
      return;
    }

    /* Dispatch notifications */
    final Set<AnomalyDTO> anomalies = getAnomalies(subscriptionGroup, result);
    final NotificationPayloadApi payload = notificationPayloadBuilder.buildNotificationPayload(
        subscriptionGroup,
        anomalies);

    /* fire notifications */
    notificationDispatcher.dispatch(subscriptionGroup, payload);

    /* Record watermarks and update entities */
    for (final AnomalyDTO anomaly : result.getAllAnomalies()) {
      anomaly.setNotified(true);
      anomalyManager.update(anomaly);
    }
    updateSubscriptionWatermarks(subscriptionGroup, result.getAllAnomalies());
  }

  public Set<AnomalyDTO> getAnomalies(SubscriptionGroupDTO subscriptionGroup,
      final SubscriptionGroupFilterResult results) {
    return results
        .getResult()
        .entrySet()
        .stream()
        .filter(result -> subscriptionGroup.equals(result.getKey().getSubscriptionConfig()))
        .findFirst()
        .map(Entry::getValue)
        .orElse(null);
  }
}
