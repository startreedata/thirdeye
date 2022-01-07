package org.apache.pinot.thirdeye.notification;

import static java.util.Objects.requireNonNull;

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
    requireNonNull(name, "name is null");
    final NotificationServiceFactory notificationServiceFactory = requireNonNull(factoryMap.get(name),
        "Unable to load NotificationServiceFactory: " + name);
    return notificationServiceFactory.build(properties);
  }
}
