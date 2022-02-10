package ai.startree.thirdeye.spi.notification;

import ai.startree.thirdeye.spi.api.NotificationPayloadApi;

public interface NotificationService {

  void notify(NotificationPayloadApi api);
}
