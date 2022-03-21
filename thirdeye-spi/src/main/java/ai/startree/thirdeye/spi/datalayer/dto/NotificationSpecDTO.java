/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import java.util.Map;
import java.util.StringJoiner;

public class NotificationSpecDTO {

  private String type;
  private Map<String, Object> params;

  public String getType() {
    return type;
  }

  public NotificationSpecDTO setType(final String type) {
    this.type = type;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public NotificationSpecDTO setParams(final Map<String, Object> params) {
    this.params = params;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", NotificationSpecDTO.class.getSimpleName() + "[", "]")
        .add("type='" + type + "'")
        .add("params=" + params)
        .toString();
  }
}
