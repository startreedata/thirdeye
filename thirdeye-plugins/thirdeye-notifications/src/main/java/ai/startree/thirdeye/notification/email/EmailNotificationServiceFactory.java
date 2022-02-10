package ai.startree.thirdeye.notification.email;

import ai.startree.thirdeye.spi.notification.NotificationService;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

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
