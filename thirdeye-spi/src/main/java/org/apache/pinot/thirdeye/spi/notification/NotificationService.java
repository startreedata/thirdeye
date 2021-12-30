package org.apache.pinot.thirdeye.spi.notification;

import org.apache.pinot.thirdeye.spi.api.NotificationPayloadApi;

public interface NotificationService {

  void notify(NotificationPayloadApi api);
}
