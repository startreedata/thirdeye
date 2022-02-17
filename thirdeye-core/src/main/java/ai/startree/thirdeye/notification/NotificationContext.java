/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

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
