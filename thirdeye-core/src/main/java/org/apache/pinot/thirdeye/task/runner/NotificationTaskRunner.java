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

import static java.util.Objects.requireNonNull;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.detection.alert.AlertUtils;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.detection.alert.NotificationSchemeFactory;
import org.apache.pinot.thirdeye.detection.alert.scheme.EmailAlertScheme;
import org.apache.pinot.thirdeye.detection.alert.scheme.NotificationPayloadBuilder;
import org.apache.pinot.thirdeye.notification.NotificationServiceRegistry;
import org.apache.pinot.thirdeye.notification.commons.EmailEntity;
import org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration;
import org.apache.pinot.thirdeye.spi.api.NotificationPayloadApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.WebhookSchemeDto;
import org.apache.pinot.thirdeye.spi.notification.NotificationService;
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

  private final NotificationSchemeFactory notificationSchemeFactory;
  private final SubscriptionGroupManager subscriptionGroupManager;
  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final NotificationPayloadBuilder notificationPayloadBuilder;
  private final NotificationServiceRegistry notificationServiceRegistry;

  private final Counter notificationTaskSuccessCounter;
  private final Counter notificationTaskCounter;
  private final SmtpConfiguration smtpConfig;

  @Inject
  public NotificationTaskRunner(
      final NotificationSchemeFactory notificationSchemeFactory,
      final SubscriptionGroupManager subscriptionGroupManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager,
      final NotificationPayloadBuilder notificationPayloadBuilder,
      final MetricRegistry metricRegistry,
      final NotificationServiceRegistry notificationServiceRegistry,
      final ThirdEyeServerConfiguration configuration) {
    this.notificationSchemeFactory = notificationSchemeFactory;
    this.subscriptionGroupManager = subscriptionGroupManager;
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.notificationPayloadBuilder = notificationPayloadBuilder;
    this.notificationServiceRegistry = notificationServiceRegistry;

    smtpConfig = configuration.getNotificationConfiguration().getSmtpConfiguration();

    notificationTaskCounter = metricRegistry.counter("notificationTaskCounter");
    notificationTaskSuccessCounter = metricRegistry.counter("notificationTaskSuccessCounter");
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

  public ArrayList<TaskResult> execute(final long subscriptionGroupId)
      throws Exception {
    notificationTaskCounter.inc();

    try {
      final SubscriptionGroupDTO subscriptionGroupDTO = getSubscriptionGroupDTO(subscriptionGroupId);

      executeInternal(subscriptionGroupDTO);
      return new ArrayList<>();
    } finally {
      notificationTaskSuccessCounter.inc();
    }
  }

  private void executeInternal(final SubscriptionGroupDTO subscriptionGroupDTO) throws Exception {
    final DetectionAlertFilterResult result = notificationSchemeFactory
        .getDetectionAlertFilterResult(subscriptionGroupDTO);

    // TODO: The old UI relies on notified tag to display the anomalies. After the migration
    // we need to clean up all references to notified tag.
    for (final MergedAnomalyResultDTO anomaly : result.getAllAnomalies()) {
      anomaly.setNotified(true);
      mergedAnomalyResultManager.update(anomaly);
    }

    requireNonNull(result);
    if (result.getAllAnomalies().size() == 0) {
      LOG.debug("Zero anomalies found, skipping webhook alert for {}",
          subscriptionGroupDTO.getId());
      return;
    }

    fireNotifications(subscriptionGroupDTO, result);

    updateSubscriptionWatermarks(subscriptionGroupDTO, result.getAllAnomalies());
  }

  private void fireNotifications(
      final SubscriptionGroupDTO subscriptionGroupDTO,
      final DetectionAlertFilterResult result) {

    // Send out emails
    final EmailAlertScheme emailAlertScheme = notificationSchemeFactory.getEmailAlertScheme();
    final EmailSchemeDto emailScheme = subscriptionGroupDTO.getNotificationSchemes()
        .getEmailScheme();
    if (emailScheme != null) {
      fireEmails(subscriptionGroupDTO, result, emailAlertScheme);
    }

    // fire webhook
    final WebhookSchemeDto webhookScheme = subscriptionGroupDTO.getNotificationSchemes()
        .getWebhookScheme();
    if (webhookScheme != null) {
      fireWebhook(subscriptionGroupDTO, result);
    }
  }

  private void fireEmails(final SubscriptionGroupDTO subscriptionGroupDTO,
      final DetectionAlertFilterResult result,
      final EmailAlertScheme emailAlertScheme) {
    final Set<MergedAnomalyResultDTO> anomalies = getAnomalies(subscriptionGroupDTO, result);
    final EmailEntity entity = emailAlertScheme.buildAndSendEmail(subscriptionGroupDTO,
        new ArrayList<>(anomalies));

    final Map<String, String> properties = new HashMap<>();
    properties.put("host", smtpConfig.getHost());
    properties.put("port", String.valueOf(smtpConfig.getPort()));
    properties.put("user", smtpConfig.getUser());
    properties.put("password", smtpConfig.getPassword());


    final NotificationService emailNotificationService = notificationServiceRegistry
        .get("email", properties);
    emailNotificationService.notify(new NotificationPayloadApi().setEmailEntity(entity));
  }

  private void fireWebhook(final SubscriptionGroupDTO subscriptionGroupDTO,
      final DetectionAlertFilterResult result) {
    final NotificationPayloadApi entity = buildNotificationPayload(
        subscriptionGroupDTO,
        result);

    final WebhookSchemeDto webhookScheme = subscriptionGroupDTO.getNotificationSchemes()
        .getWebhookScheme();

    final Map<String, String> properties = new HashMap<>();
    properties.put("url", webhookScheme.getUrl());
    if (webhookScheme.getHashKey() != null) {
      properties.put("hashKey", webhookScheme.getHashKey());
    }
    final NotificationService webhookNotificationService = notificationServiceRegistry
        .get("webhook", properties);
    webhookNotificationService.notify(entity);
  }

  private NotificationPayloadApi buildNotificationPayload(
      final SubscriptionGroupDTO subscriptionGroupDTO, final DetectionAlertFilterResult result) {
    final Set<MergedAnomalyResultDTO> anomalies = getAnomalies(subscriptionGroupDTO, result);
    return notificationPayloadBuilder.buildNotificationPayload(
        subscriptionGroupDTO,
        anomalies);
  }

  public Set<MergedAnomalyResultDTO> getAnomalies(SubscriptionGroupDTO subscriptionGroup,
      final DetectionAlertFilterResult results) {
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
