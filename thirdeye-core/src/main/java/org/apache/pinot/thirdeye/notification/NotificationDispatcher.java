package org.apache.pinot.thirdeye.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.detection.alert.scheme.NotificationPayloadBuilder;
import org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration;
import org.apache.pinot.thirdeye.spi.api.NotificationPayloadApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.WebhookSchemeDto;
import org.apache.pinot.thirdeye.spi.notification.NotificationService;

@Singleton
public class NotificationDispatcher {

  private final NotificationPayloadBuilder notificationPayloadBuilder;
  private final NotificationServiceRegistry notificationServiceRegistry;

  private final SmtpConfiguration smtpConfig;

  @Inject
  public NotificationDispatcher(
      final NotificationPayloadBuilder notificationPayloadBuilder,
      final NotificationServiceRegistry notificationServiceRegistry,
      final ThirdEyeServerConfiguration configuration) {
    this.notificationPayloadBuilder = notificationPayloadBuilder;
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.smtpConfig = configuration.getNotificationConfiguration().getSmtpConfiguration();
  }

  public void dispatch(
      final SubscriptionGroupDTO subscriptionGroup,
      final DetectionAlertFilterResult result) {
    final Set<MergedAnomalyResultDTO> anomalies = getAnomalies(subscriptionGroup, result);

    final NotificationPayloadApi payload = notificationPayloadBuilder.buildNotificationPayload(
        subscriptionGroup,
        anomalies);

    fireNotifications(subscriptionGroup, payload);
  }

  private void fireNotifications(final SubscriptionGroupDTO subscriptionGroup,
      final NotificationPayloadApi payload) {
    // Send out emails
    final EmailSchemeDto emailScheme = subscriptionGroup.getNotificationSchemes().getEmailScheme();
    if (emailScheme != null) {
      fireEmails(payload);
    }

    // fire webhook
    final WebhookSchemeDto webhookScheme = subscriptionGroup.getNotificationSchemes()
        .getWebhookScheme();
    if (webhookScheme != null) {
      fireWebhook(webhookScheme, payload);
    }
  }

  private Set<MergedAnomalyResultDTO> getAnomalies(SubscriptionGroupDTO subscriptionGroup,
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

  private void fireEmails(final NotificationPayloadApi api) {
    final Map<String, String> properties = new HashMap<>();
    properties.put("host", smtpConfig.getHost());
    properties.put("port", String.valueOf(smtpConfig.getPort()));
    properties.put("user", smtpConfig.getUser());
    properties.put("password", smtpConfig.getPassword());

    final NotificationService emailNotificationService = notificationServiceRegistry
        .get("email", properties);
    emailNotificationService.notify(api);
  }

  private void fireWebhook(final WebhookSchemeDto webhookScheme,
      final NotificationPayloadApi entity) {

    final Map<String, String> properties = new HashMap<>();
    properties.put("url", webhookScheme.getUrl());
    if (webhookScheme.getHashKey() != null) {
      properties.put("hashKey", webhookScheme.getHashKey());
    }
    final NotificationService webhookNotificationService = notificationServiceRegistry
        .get("webhook", properties);
    webhookNotificationService.notify(entity);
  }
}
