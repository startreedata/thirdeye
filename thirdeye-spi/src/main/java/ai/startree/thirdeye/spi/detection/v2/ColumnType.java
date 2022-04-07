/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection.v2;

import ai.startree.thirdeye.spi.dataframe.Series;
import java.sql.Types;
import java.util.Objects;

public class ColumnType {

  private final ColumnDataType columnDataType;
  private final String ARRAY_TYPE_SUFFIX = "_ARRAY";

  public ColumnType(final ColumnDataType columnDataType) {
    this.columnDataType = columnDataType;
  }

  public static ColumnType pinotTypeToColumnType(final String columnDataType) {
    return new ColumnType(ColumnDataType.valueOf(columnDataType));
  }

  public static ColumnType jdbcTypeToColumnType(final int columnType) {
    switch (columnType) {
      case Types.DATE:
      case Types.TIME:
      case Types.TIMESTAMP:
        return new ColumnType(ColumnDataType.DATE);
      case Types.ARRAY:
      case Types.BINARY:
      case Types.DATALINK:
      case Types.BLOB:
      case Types.DISTINCT:
      case Types.JAVA_OBJECT:
      case Types.NULL:
      case Types.OTHER:
      case Types.REF:
      case Types.STRUCT:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        return new ColumnType(ColumnDataType.BYTES);
      case Types.BIT:
      case Types.BOOLEAN:
        return new ColumnType(ColumnDataType.BOOLEAN);
      case Types.DECIMAL:
      case Types.DOUBLE:
      case Types.FLOAT:
      case Types.NUMERIC:
      case Types.REAL:
        return new ColumnType(ColumnDataType.DOUBLE);
      case Types.INTEGER:
      case Types.SMALLINT:
        return new ColumnType(ColumnDataType.INT);
      case Types.BIGINT:
        return new ColumnType(ColumnDataType.LONG);
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.CLOB:
      case Types.LONGVARCHAR:
        return new ColumnType(ColumnDataType.STRING);
    }
    throw new UnsupportedOperationException("Unknown JDBC data type - " + columnType);
  }

  public static ColumnType seriesTypeToColumnType(final Series.SeriesType columnType) {
    switch (columnType) {
      case DOUBLE:
        return new ColumnType(ColumnDataType.DOUBLE);
      case LONG:
        return new ColumnType(ColumnDataType.LONG);
      case STRING:
        return new ColumnType(ColumnDataType.STRING);
      case BOOLEAN:
        return new ColumnType(ColumnDataType.BOOLEAN);
      case OBJECT:
        throw new UnsupportedOperationException("Casting SeriesType to ColumnType not supported for "+ Series.SeriesType.OBJECT);
    }
    throw new UnsupportedOperationException("Unknown SeriesType data type - " + columnType);
  }

  public ColumnDataType getType() {
    return columnDataType;
  }

  public boolean isArray() {
    return columnDataType.name().endsWith(ARRAY_TYPE_SUFFIX);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ColumnType that = (ColumnType) o;
    return columnDataType == that.columnDataType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(columnDataType);
  }

  @Override
  public String toString() {
    return "ColumnType{" +
        "columnDataType=" + columnDataType +
        '}';
  }

  public enum ColumnDataType {
    BOOLEAN, INT, LONG, FLOAT, DOUBLE, DATE, STRING, BYTES, OBJECT, INT_ARRAY, LONG_ARRAY, FLOAT_ARRAY, DOUBLE_ARRAY, STRING_ARRAY;
  }
}
