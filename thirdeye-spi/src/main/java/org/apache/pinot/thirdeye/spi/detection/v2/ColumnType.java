package org.apache.pinot.thirdeye.spi.detection.v2;

public class ColumnType {

  private final String type;
  private final boolean isArray;

  public ColumnType(final String type, final boolean isArray) {
    this.type = type;
    this.isArray = isArray;
  }

  public String getType() {
    return type;
  }

  public boolean isArray() {
    return isArray;
  }
}
