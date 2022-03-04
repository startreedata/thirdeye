/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.datalayer.dto.EmailSchemeDto;
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

@Singleton
public class NotificationDispatcher {

  private final NotificationServiceRegistry notificationServiceRegistry;

  private final SmtpConfiguration smtpConfig;

  @Inject
  public NotificationDispatcher(
      final NotificationServiceRegistry notificationServiceRegistry,
      final NotificationConfiguration notificationConfiguration) {
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.smtpConfig = notificationConfiguration.getSmtpConfiguration();
  }

  public void dispatch(final SubscriptionGroupDTO subscriptionGroup,
      final NotificationPayloadApi payload) {
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
    final String fromAddress = requireNonNull(optional(from).orElse(smtpParams.get("user")
            .toString()),
        "from address is null");

    checkArgument(!fromAddress.trim().isEmpty(), "from address is empty");

    final Map<String, Object> emailRecipients = new HashMap<>();
    emailRecipients.put("from", fromAddress);
    emailRecipients.put("to", emailScheme.getTo());
    emailRecipients.put("cc", emailScheme.getCc());
    emailRecipients.put("bcc", emailScheme.getBcc());

    return new NotificationSpecDTO()
        .setType("email-smtp")
        .setParams(Map.of("smtp", smtpParams, "emailRecipients", emailRecipients));
  }

  private NotificationSpecDTO toSpec(final WebhookSchemeDto webhookScheme) {
    return new NotificationSpecDTO()
        .setType("webhook")
        .setParams(buildWebhookProperties(webhookScheme));
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
