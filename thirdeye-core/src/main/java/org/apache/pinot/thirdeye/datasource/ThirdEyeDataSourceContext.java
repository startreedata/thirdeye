package org.apache.pinot.thirdeye.datasource;

import java.util.Map;

public class ThirdEyeDataSourceContext {

  private Map<String, Object> properties;

  public Map<String, Object> getProperties() {
    return properties;
  }

  public ThirdEyeDataSourceContext setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }
}
