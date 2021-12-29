package org.apache.pinot.thirdeye.notification;

import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.notification.NotificationService;
import org.apache.pinot.thirdeye.spi.notification.NotificationServiceFactory;

@Singleton
public class NotificationServiceRegistry {

  private final Map<String, NotificationServiceFactory> factoryMap = new HashMap<>();

  public void addNotificationServiceFactory(NotificationServiceFactory factory) {
    factoryMap.put(factory.name(), factory);
  }

  public NotificationService get(
      final String name,
      final Map<String, String> properties) {
    return factoryMap.get(name).build(properties);
  }
}
