package org.apache.pinot.thirdeye.datasource.pinotsql;

public enum PinotSqlDataSourceProperties {
  CONTROLLER_HOST("controllerHost"),
  CONTROLLER_PORT("controllerPort"),
  CONTROLLER_CONNECTION_SCHEME("controllerConnectionScheme"),
  CONTROLLER_HEADERS("headers");

  private final String value;

  PinotSqlDataSourceProperties(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

