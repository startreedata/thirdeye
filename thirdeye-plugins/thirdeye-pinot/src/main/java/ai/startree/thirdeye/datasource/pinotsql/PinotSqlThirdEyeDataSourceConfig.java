/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.pinotsql;

import com.google.common.base.MoreObjects;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable configurations for setting up {@link PinotSqlThirdEyeDataSource}'s connection to
 * Pinot.
 */
public class PinotSqlThirdEyeDataSourceConfig {

  private String controllerHost;
  private int controllerPort;
  private String controllerConnectionScheme;
  private Map<String, String> headers;

  public String connectionUrl() {
    return String.format("jdbc:pinot://%s:%s", controllerHost, controllerPort);
  }

  public String getControllerConnectionScheme() {
    return controllerConnectionScheme;
  }

  public PinotSqlThirdEyeDataSourceConfig setControllerConnectionScheme(
      final String controllerConnectionScheme) {
    this.controllerConnectionScheme = controllerConnectionScheme;
    return this;
  }

  public String getControllerHost() {
    return controllerHost;
  }

  public PinotSqlThirdEyeDataSourceConfig setControllerHost(final String controllerHost) {
    this.controllerHost = controllerHost;
    return this;
  }

  public int getControllerPort() {
    return controllerPort;
  }

  public PinotSqlThirdEyeDataSourceConfig setControllerPort(final int controllerPort) {
    this.controllerPort = controllerPort;
    return this;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public PinotSqlThirdEyeDataSourceConfig setHeaders(final Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PinotSqlThirdEyeDataSourceConfig config = (PinotSqlThirdEyeDataSourceConfig) o;
    return getControllerPort() == config.getControllerPort() && Objects
        .equals(getControllerConnectionScheme(), config.getControllerConnectionScheme()) && Objects
        .equals(getControllerHost(), config.getControllerHost());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getControllerConnectionScheme(), getControllerHost(), getControllerPort());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("controllerHost", controllerHost)
      .add("controllerPort", controllerPort)
      .add("controllerConnectionScheme", controllerConnectionScheme)
      .add("headers", headers)
      .toString();
  }
}
