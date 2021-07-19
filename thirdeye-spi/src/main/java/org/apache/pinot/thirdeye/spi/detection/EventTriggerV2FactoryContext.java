package org.apache.pinot.thirdeye.spi.detection;

import java.util.Map;

public class EventTriggerV2FactoryContext {

  private Map<String, Object> properties;

  public Map<String, Object> getProperties() {
    return properties;
  }

  public EventTriggerV2FactoryContext setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }
}
