package org.apache.pinot.thirdeye.notification;

import java.util.Properties;

public class NotificationContext {

  private Properties properties;
  private String uiPublicUrl;

  public Properties getProperties() {
    return properties;
  }

  public NotificationContext setProperties(final Properties properties) {
    this.properties = properties;
    return this;
  }

  public String getUiPublicUrl() {
    return uiPublicUrl;
  }

  public NotificationContext setUiPublicUrl(final String uiPublicUrl) {
    this.uiPublicUrl = uiPublicUrl;
    return this;
  }
}
