package ai.startree.thirdeye.spi.detection;

import java.util.Map;

public class AnomalyDetectorFactoryContext {

  private Map<String, Object> properties;

  public Map<String, Object> getProperties() {
    return properties;
  }

  public AnomalyDetectorFactoryContext setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }
}
