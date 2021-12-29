package org.apache.pinot.thirdeye.notification;

import java.util.Map;
import org.apache.pinot.thirdeye.spi.notification.NotificationService;
import org.apache.pinot.thirdeye.spi.notification.NotificationServiceFactory;

public class WebhookNotificationServiceFactory implements NotificationServiceFactory {

  @Override
  public String name() {
    return "webhook";
  }

  @Override
  public NotificationService build(final Map<String, String> properties) {
    return new WebhookNotificationService();
  }
}
