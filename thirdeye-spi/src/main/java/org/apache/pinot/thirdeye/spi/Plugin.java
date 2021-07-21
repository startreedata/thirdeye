package org.apache.pinot.thirdeye.spi;

import java.util.Collections;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactory;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Factory;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerFactory;

public interface Plugin {

  default Iterable<ThirdEyeDataSourceFactory> getDataSourceFactories() {
    return Collections.emptyList();
  }

  default Iterable<AnomalyDetectorFactory> getAnomalyDetectorFactories() {
    return Collections.emptyList();
  }

  default Iterable<AnomalyDetectorV2Factory> getAnomalyDetectorV2Factories() {
    return Collections.emptyList();
  }

  default Iterable<EventTriggerFactory> getEventTriggerFactories() {
    return Collections.emptyList();
  }
}
