package org.apache.pinot.thirdeye.notification.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.notification.NotificationService;
import org.apache.pinot.thirdeye.spi.notification.NotificationServiceFactory;

public class EmailNotificationServiceFactory implements NotificationServiceFactory {

  @Override
  public String name() {
    return "email";
  }

  @Override
  public NotificationService build(final Map<String, String> properties) {
    final SmtpConfiguration configuration = new ObjectMapper()
        .convertValue(properties, SmtpConfiguration.class);

    return new EmailNotificationService(configuration);
  }
}
