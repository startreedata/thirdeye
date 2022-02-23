/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.task.runner;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detection.alert.AlertUtils;
import ai.startree.thirdeye.detection.alert.DetectionAlertFilterResult;
import ai.startree.thirdeye.detection.alert.NotificationSchemeFactory;
import ai.startree.thirdeye.notification.NotificationDispatcher;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.task.DetectionAlertTaskInfo;
import ai.startree.thirdeye.task.TaskContext;
import ai.startree.thirdeye.task.TaskResult;
import ai.startree.thirdeye.task.TaskRunner;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
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
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  private final Counter notificationTaskSuccessCounter;
  private final Counter notificationTaskCounter;
  private final NotificationDispatcher notificationDispatcher;

  @Inject
  public NotificationTaskRunner(
      final NotificationSchemeFactory notificationSchemeFactory,
      final SubscriptionGroupManager subscriptionGroupManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final MetricRegistry metricRegistry,
      final NotificationDispatcher notificationDispatcher) {
    this.notificationSchemeFactory = notificationSchemeFactory;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;

    notificationTaskCounter = metricRegistry.counter("notificationTaskCounter");
    notificationTaskSuccessCounter = metricRegistry.counter("notificationTaskSuccessCounter");
    this.notificationDispatcher = notificationDispatcher;
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
      final List<MergedAnomalyResultDTO> allAnomalies) {
    if (!allAnomalies.isEmpty()) {
      subscriptionConfig.setVectorClocks(
          AlertUtils.mergeVectorClock(subscriptionConfig.getVectorClocks(),
              AlertUtils.makeVectorClock(allAnomalies)));

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
    notificationTaskCounter.inc();

    final SubscriptionGroupDTO subscriptionGroupDTO = getSubscriptionGroupDTO(subscriptionGroupId);

    executeInternal(subscriptionGroupDTO);
    notificationTaskSuccessCounter.inc();
    return Collections.emptyList();
  }

  private void executeInternal(final SubscriptionGroupDTO subscriptionGroupDTO) throws Exception {
    final DetectionAlertFilterResult result = requireNonNull(notificationSchemeFactory
        .getDetectionAlertFilterResult(subscriptionGroupDTO), "DetectionAlertFilterResult is null");

    if (result.getAllAnomalies().size() == 0) {
      LOG.debug("Zero anomalies found, skipping notification for subscription group: {}",
          subscriptionGroupDTO.getId());
      return;
    }

    /* Dispatch notifications */
    notificationDispatcher.dispatch(subscriptionGroupDTO, result);

    /* Record watermarks and update entities */
    for (final MergedAnomalyResultDTO anomaly : result.getAllAnomalies()) {
      anomaly.setNotified(true);
      mergedAnomalyResultManager.update(anomaly);
    }
    updateSubscriptionWatermarks(subscriptionGroupDTO, result.getAllAnomalies());
  }
}
