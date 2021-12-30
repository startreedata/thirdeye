package org.apache.pinot.thirdeye.notification.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    final WebhookConfiguration configuration = new ObjectMapper()
        .convertValue(properties, WebhookConfiguration.class);

    return new WebhookNotificationService(configuration);
  }
}
