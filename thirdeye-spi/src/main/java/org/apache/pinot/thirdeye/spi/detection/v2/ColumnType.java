package org.apache.pinot.thirdeye.spi.detection.v2;

public class ColumnType {

  private final ColumnDataType columnDataType;
  private final String ARRAY_TYPE_SUFFIX = "_ARRAY";

  public ColumnType(final ColumnDataType columnDataType) {
    this.columnDataType = columnDataType;
  }

  public ColumnDataType getType() {
    return columnDataType;
  }

  public boolean isArray() {
    return columnDataType.name().endsWith(ARRAY_TYPE_SUFFIX);
  }

  public enum ColumnDataType {
    BOOLEAN, INT, LONG, FLOAT, DOUBLE, DATE, STRING, BYTES, OBJECT, INT_ARRAY, LONG_ARRAY, FLOAT_ARRAY, DOUBLE_ARRAY, STRING_ARRAY;
  }
}
