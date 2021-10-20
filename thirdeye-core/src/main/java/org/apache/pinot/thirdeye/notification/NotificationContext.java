package org.apache.pinot.thirdeye.notification;

import java.util.Properties;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;

public class NotificationContext {

  private Properties properties;
  private ThirdEyeServerConfiguration config;

  public Properties getProperties() {
    return properties;
  }

  public NotificationContext setProperties(final Properties properties) {
    this.properties = properties;
    return this;
  }

  public ThirdEyeServerConfiguration getConfig() {
    return config;
  }

  public NotificationContext setConfig(
      final ThirdEyeServerConfiguration config) {
    this.config = config;
    return this;
  }
}
