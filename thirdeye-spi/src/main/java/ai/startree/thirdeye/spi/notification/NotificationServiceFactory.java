package ai.startree.thirdeye.spi.notification;

import java.util.Map;

public interface NotificationServiceFactory {

  String name();

  NotificationService build(Map<String, String> properties);
}
