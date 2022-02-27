/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.detection.alert.DetectionAlertFilterResult;
import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSpecDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.datalayer.dto.WebhookSchemeDto;
import ai.startree.thirdeye.spi.notification.NotificationService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Singleton
public class NotificationDispatcher {

  private final NotificationPayloadBuilder notificationPayloadBuilder;
  private final NotificationServiceRegistry notificationServiceRegistry;

  private final SmtpConfiguration smtpConfig;

  @Inject
  public NotificationDispatcher(
      final NotificationPayloadBuilder notificationPayloadBuilder,
      final NotificationServiceRegistry notificationServiceRegistry,
      final NotificationConfiguration notificationConfiguration) {
    this.notificationPayloadBuilder = notificationPayloadBuilder;
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.smtpConfig = notificationConfiguration.getSmtpConfiguration();
  }

  public void dispatch(
      final SubscriptionGroupDTO subscriptionGroup,
      final DetectionAlertFilterResult result) {
    final Set<MergedAnomalyResultDTO> anomalies = getAnomalies(subscriptionGroup, result);

    final NotificationPayloadApi payload = notificationPayloadBuilder.buildNotificationPayload(
        subscriptionGroup,
        anomalies);

    /* fire notifications */
    optional(subscriptionGroup.getSpecs())
        .orElseGet(() -> specFromLegacySubscriptionGroup(subscriptionGroup))
        .stream()
        .map(this::getNotificationService)
        .forEach(service -> service.notify(payload));
  }

  private NotificationService getNotificationService(final NotificationSpecDTO spec) {
    return notificationServiceRegistry.get(spec.getType(), spec.getParams());
  }

  private List<NotificationSpecDTO> specFromLegacySubscriptionGroup(final SubscriptionGroupDTO sg) {
    final NotificationSchemesDto legacySchemes = sg.getNotificationSchemes();
    final List<NotificationSpecDTO> specs = new ArrayList<>();

    optional(legacySchemes.getEmailScheme())
        .map(emailScheme -> toSpec(emailScheme, sg.getFrom()))
        .ifPresent(specs::add);

    optional(legacySchemes.getWebhookScheme())
        .map(this::toSpec)
        .ifPresent(specs::add);

    return specs;
  }

  private NotificationSpecDTO toSpec(final EmailSchemeDto emailScheme, final String from) {
    final Map<String, Object> smtpParams = buildSmtpParams();

    final Map<String, Object> params = new HashMap<>();
    params.put("smtp", smtpParams);
    params.put("from", optional(from).orElse(smtpParams.get("user").toString()));
    params.put("to", emailScheme.getTo());
    params.put("cc", emailScheme.getCc());
    params.put("bcc", emailScheme.getBcc());

    return new NotificationSpecDTO()
        .setType("email-smtp")
        .setParams(params);
  }

  private NotificationSpecDTO toSpec(final WebhookSchemeDto webhookScheme) {
    return new NotificationSpecDTO()
        .setType("webhook")
        .setParams(buildWebhookProperties(webhookScheme));
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

  public Map<String, Object> buildSmtpParams() {
    final Map<String, Object> properties = new HashMap<>();
    properties.put("host", smtpConfig.getHost());
    properties.put("port", String.valueOf(smtpConfig.getPort()));
    properties.put("user", smtpConfig.getUser());
    properties.put("password", smtpConfig.getPassword());

    return properties;
  }

  private Map<String, Object> buildWebhookProperties(final WebhookSchemeDto webhookScheme) {
    final Map<String, Object> params = new HashMap<>();
    params.put("url", webhookScheme.getUrl());
    if (webhookScheme.getHashKey() != null) {
      params.put("hashKey", webhookScheme.getHashKey());
    }
    return params;
  }
}
