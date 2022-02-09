package ai.startree.thirdeye.spi;

import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorV2Factory;
import ai.startree.thirdeye.spi.detection.EventTriggerFactory;
import ai.startree.thirdeye.spi.notification.NotificationServiceFactory;
import java.util.Collections;

public interface Plugin {

  default Iterable<ThirdEyeDataSourceFactory> getDataSourceFactories() {
    return Collections.emptyList();
  }

  default Iterable<AnomalyDetectorV2Factory> getAnomalyDetectorV2Factories() {
    return Collections.emptyList();
  }

  default Iterable<EventTriggerFactory> getEventTriggerFactories() {
    return Collections.emptyList();
  }

  default Iterable<NotificationServiceFactory> getNotificationServiceFactories() {
    return Collections.emptyList();
  }
}
