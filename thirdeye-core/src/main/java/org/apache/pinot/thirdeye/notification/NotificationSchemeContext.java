package org.apache.pinot.thirdeye.notification;

import com.codahale.metrics.MetricRegistry;
import org.apache.pinot.thirdeye.notification.commons.SmtpConfiguration;
import org.apache.pinot.thirdeye.notification.content.NotificationContent;

public class NotificationSchemeContext {

  private String uiPublicUrl;
  private NotificationContent metricAnomaliesContent;
  private NotificationContent entityGroupKeyContent;
  private MetricRegistry metricRegistry;
  private SmtpConfiguration smtpConfiguration;
  private NotificationServiceRegistry notificationServiceRegistry;

  public String getUiPublicUrl() {
    return uiPublicUrl;
  }

  public NotificationSchemeContext setUiPublicUrl(final String uiPublicUrl) {
    this.uiPublicUrl = uiPublicUrl;
    return this;
  }

  public NotificationContent getMetricAnomaliesContent() {
    return metricAnomaliesContent;
  }

  public NotificationSchemeContext setMetricAnomaliesContent(
      final NotificationContent metricAnomaliesContent) {
    this.metricAnomaliesContent = metricAnomaliesContent;
    return this;
  }

  public NotificationContent getEntityGroupKeyContent() {
    return entityGroupKeyContent;
  }

  public NotificationSchemeContext setEntityGroupKeyContent(
      final NotificationContent entityGroupKeyContent) {
    this.entityGroupKeyContent = entityGroupKeyContent;
    return this;
  }

  public MetricRegistry getMetricRegistry() {
    return metricRegistry;
  }

  public NotificationSchemeContext setMetricRegistry(final MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
    return this;
  }

  public SmtpConfiguration getSmtpConfiguration() {
    return smtpConfiguration;
  }

  public NotificationSchemeContext setSmtpConfiguration(
      final SmtpConfiguration smtpConfiguration) {
    this.smtpConfiguration = smtpConfiguration;
    return this;
  }

  public NotificationServiceRegistry getNotificationServiceRegistry() {
    return notificationServiceRegistry;
  }

  public NotificationSchemeContext setNotificationServiceRegistry(
      final NotificationServiceRegistry notificationServiceRegistry) {
    this.notificationServiceRegistry = notificationServiceRegistry;
    return this;
  }
}
