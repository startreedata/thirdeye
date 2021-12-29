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

package org.apache.pinot.thirdeye.task.runner;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.pinot.thirdeye.detection.alert.AlertUtils;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilter;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.detection.alert.NotificationSchemeFactory;
import org.apache.pinot.thirdeye.detection.alert.scheme.NotificationScheme;
import org.apache.pinot.thirdeye.detection.alert.suppress.DetectionAlertSuppressor;
import org.apache.pinot.thirdeye.notification.NotificationServiceRegistry;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.task.TaskInfo;
import org.apache.pinot.thirdeye.task.DetectionAlertTaskInfo;
import org.apache.pinot.thirdeye.task.TaskContext;
import org.apache.pinot.thirdeye.task.TaskResult;
import org.apache.pinot.thirdeye.task.TaskRunner;
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

  private final NotificationServiceRegistry notificationServiceRegistry;
  private final NotificationSchemeFactory notificationSchemeFactory;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;

  private final Counter notificationTaskSuccessCounter;
  private final Counter notificationTaskCounter;

  @Inject
  public NotificationTaskRunner(
      final NotificationServiceRegistry notificationServiceRegistry,
      final NotificationSchemeFactory notificationSchemeFactory,
      final SubscriptionGroupManager subscriptionGroupManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final MetricRegistry metricRegistry) {
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.notificationSchemeFactory = notificationSchemeFactory;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;

    notificationTaskCounter = metricRegistry.counter("notificationTaskCounter");
    notificationTaskSuccessCounter = metricRegistry.counter("notificationTaskSuccessCounter");
  }

  private SubscriptionGroupDTO loadDetectionAlertConfig(final long detectionAlertConfigId) {
    final SubscriptionGroupDTO detectionAlertConfig = this.subscriptionGroupManager
        .findById(detectionAlertConfigId);
    if (detectionAlertConfig == null) {
      throw new RuntimeException("Cannot find detection alert config id " + detectionAlertConfigId);
    }

    if (detectionAlertConfig.getProperties() == null) {
      LOG.warn(String.format("Detection alert %d contains no properties", detectionAlertConfigId));
    }
    return detectionAlertConfig;
  }

  private void updateSubscriptionWatermarks(final DetectionAlertFilterResult result,
      final SubscriptionGroupDTO subscriptionConfig) {
    if (!result.getAllAnomalies().isEmpty()) {
      subscriptionConfig.setVectorClocks(
          AlertUtils.mergeVectorClock(subscriptionConfig.getVectorClocks(),
              AlertUtils.makeVectorClock(result.getAllAnomalies())));

      LOG.info("Updating watermarks for subscription config : {}", subscriptionConfig.getId());
      this.subscriptionGroupManager.save(subscriptionConfig);
    }
  }

  @Override
  public List<TaskResult> execute(final TaskInfo taskInfo, final TaskContext taskContext)
      throws Exception {
    notificationTaskCounter.inc();

    try {
      final long alertId = ((DetectionAlertTaskInfo) taskInfo).getDetectionAlertConfigId();
      final SubscriptionGroupDTO alertConfig = loadDetectionAlertConfig(alertId);

      // Load all the anomalies along with their recipients
      final DetectionAlertFilter alertFilter = notificationSchemeFactory
          .loadAlertFilter(alertConfig, System.currentTimeMillis());
      DetectionAlertFilterResult result = alertFilter.run();

      // TODO: The old UI relies on notified tag to display the anomalies. After the migration
      // we need to clean up all references to notified tag.
      for (final MergedAnomalyResultDTO anomaly : result.getAllAnomalies()) {
        anomaly.setNotified(true);
        mergedAnomalyResultManager.update(anomaly);
      }

      // Suppress alerts if any and get the filtered anomalies to be notified
      final Set<DetectionAlertSuppressor> alertSuppressors = notificationSchemeFactory
          .loadAlertSuppressors(alertConfig);
      for (final DetectionAlertSuppressor alertSuppressor : alertSuppressors) {
        result = alertSuppressor.run(result);
      }

      // Send out alert notifications (email and/or iris)
      final Set<NotificationScheme> alertSchemes = notificationSchemeFactory.getAlertSchemes();
      for (final NotificationScheme alertScheme : alertSchemes) {
        alertScheme.run(alertConfig, result);
        alertScheme.destroy();
      }

      updateSubscriptionWatermarks(result, alertConfig);
      return new ArrayList<>();
    } finally {
      notificationTaskSuccessCounter.inc();
    }
  }
}
