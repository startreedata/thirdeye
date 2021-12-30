package org.apache.pinot.thirdeye.spi.detection;

import java.util.Map;

public class AnomalyDetectorFactoryV2Context {

  private Map<String, Object> properties;

  public Map<String, Object> getProperties() {
    return properties;
  }

  public AnomalyDetectorFactoryV2Context setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }
}
