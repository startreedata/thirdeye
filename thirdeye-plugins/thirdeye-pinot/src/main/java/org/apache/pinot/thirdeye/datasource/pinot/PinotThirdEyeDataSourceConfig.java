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

package org.apache.pinot.thirdeye.datasource.pinot;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * An immutable configurations for setting up {@link PinotThirdEyeDataSource}'s connection to Pinot.
 */
public class PinotThirdEyeDataSourceConfig {

  private String zookeeperUrl;
  private String controllerHost;
  private int controllerPort;
  private String controllerConnectionScheme;
  private String clusterName;
  private String brokerUrl;
  private String tag;
  private String name;

  public String getZookeeperUrl() {
    return zookeeperUrl;
  }

  public PinotThirdEyeDataSourceConfig setZookeeperUrl(final String zookeeperUrl) {
    this.zookeeperUrl = zookeeperUrl;
    return this;
  }

  public String getControllerHost() {
    return controllerHost;
  }

  public PinotThirdEyeDataSourceConfig setControllerHost(final String controllerHost) {
    this.controllerHost = controllerHost;
    return this;
  }

  public int getControllerPort() {
    return controllerPort;
  }

  public PinotThirdEyeDataSourceConfig setControllerPort(final int controllerPort) {
    this.controllerPort = controllerPort;
    return this;
  }

  public String getControllerConnectionScheme() {
    return controllerConnectionScheme;
  }

  public PinotThirdEyeDataSourceConfig setControllerConnectionScheme(
      final String controllerConnectionScheme) {
    this.controllerConnectionScheme = controllerConnectionScheme;
    return this;
  }

  public String getClusterName() {
    return clusterName;
  }

  public PinotThirdEyeDataSourceConfig setClusterName(final String clusterName) {
    this.clusterName = clusterName;
    return this;
  }

  public String getBrokerUrl() {
    return brokerUrl;
  }

  public PinotThirdEyeDataSourceConfig setBrokerUrl(final String brokerUrl) {
    this.brokerUrl = brokerUrl;
    return this;
  }

  public String getTag() {
    return tag;
  }

  public PinotThirdEyeDataSourceConfig setTag(final String tag) {
    this.tag = tag;
    return this;
  }

  public String getName() {
    return name;
  }

  public PinotThirdEyeDataSourceConfig setName(final String name) {
    this.name = name;
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
    PinotThirdEyeDataSourceConfig config = (PinotThirdEyeDataSourceConfig) o;
    return getControllerPort() == config.getControllerPort() && Objects
        .equals(getZookeeperUrl(), config.getZookeeperUrl()) && Objects
        .equals(getControllerHost(), config.getControllerHost()) && Objects
        .equals(getControllerConnectionScheme(), config.getControllerConnectionScheme()) && Objects
        .equals(getClusterName(), config.getClusterName()) && Objects
        .equals(getBrokerUrl(), config.getBrokerUrl())
        && Objects.equals(getTag(), config.getTag());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getZookeeperUrl(), getControllerHost(), getControllerPort(),
        getControllerConnectionScheme(),
        getClusterName(), getBrokerUrl(), getTag(), getName());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("zookeeperUrl", zookeeperUrl)
        .add("controllerHost", controllerHost)
        .add("controllerPort", controllerPort)
        .add("controllerConnectionScheme", controllerConnectionScheme)
        .add("clusterName", clusterName).add("brokerUrl", brokerUrl).add("tag", tag)
        .add("name", name).toString();
  }
}
