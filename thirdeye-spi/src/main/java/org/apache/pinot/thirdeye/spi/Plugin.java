package org.apache.pinot.thirdeye.spi;

import java.util.Collections;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSourceFactory;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorFactory;

public interface Plugin {

  default Iterable<ThirdEyeDataSourceFactory> getDataSourceFactories() {
    return Collections.emptyList();
  }

  default Iterable<AnomalyDetectorFactory> getAnomalyDetectorFactories() {
    return Collections.emptyList();
  }
}
