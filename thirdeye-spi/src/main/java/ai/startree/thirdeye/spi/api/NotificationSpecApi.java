/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import java.util.Map;

public class NotificationSpecApi {

  private String type;
  private Map<String, Object> params;

  public String getType() {
    return type;
  }

  public NotificationSpecApi setType(final String type) {
    this.type = type;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public NotificationSpecApi setParams(final Map<String, Object> params) {
    this.params = params;
    return this;
  }
}
