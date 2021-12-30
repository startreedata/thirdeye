package org.apache.pinot.thirdeye.notification;

import java.util.Arrays;
import org.apache.pinot.thirdeye.notification.email.EmailNotificationServiceFactory;
import org.apache.pinot.thirdeye.notification.webhook.WebhookNotificationServiceFactory;
import org.apache.pinot.thirdeye.spi.Plugin;
import org.apache.pinot.thirdeye.spi.notification.NotificationServiceFactory;

public class DefaultNotificationsPlugin implements Plugin {

  @Override
  public Iterable<NotificationServiceFactory> getNotificationServiceFactories() {
    return Arrays.asList(
        new WebhookNotificationServiceFactory(),
        new EmailNotificationServiceFactory()
    );
  }
}
