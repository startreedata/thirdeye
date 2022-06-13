/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.datasource.pinot;

public enum PinotThirdeyeDataSourceProperties {
  CONTROLLER_HOST("controllerHost"),
  CONTROLLER_PORT("controllerPort"),
  CONTROLLER_CONNECTION_SCHEME("controllerConnectionScheme"),
  CLUSTER_NAME("clusterName"),
  ZOOKEEPER_URL("zookeeperUrl"),
  TAG("tag"),
  BROKER_URL("brokerUrl"),
  NAME("name"),
  HEADERS("headers");

  private final String value;

  PinotThirdeyeDataSourceProperties(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

