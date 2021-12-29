package org.apache.pinot.thirdeye.spi.notification;

import java.util.Map;
import org.apache.pinot.thirdeye.spi.api.NotificationPayloadApi;

public interface NotificationService {

  void init(Map<String, String> properties);

  void notify(NotificationPayloadApi api);
}
