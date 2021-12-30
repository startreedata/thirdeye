/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.datasource.pinotsql;

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
