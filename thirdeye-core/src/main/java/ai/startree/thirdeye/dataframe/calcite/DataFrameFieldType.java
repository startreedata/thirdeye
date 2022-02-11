/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.dataframe.calcite;

import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.dataframe.Series.SeriesType;
import java.util.HashMap;
import java.util.Map;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.tree.Primitive;
import org.apache.calcite.rel.type.RelDataType;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum DataFrameFieldType {
  STRING(String.class, SeriesType.STRING),
  BOOLEAN(Primitive.BOOLEAN, SeriesType.BOOLEAN),
  LONG(Primitive.LONG, SeriesType.LONG),
  DOUBLE(Primitive.DOUBLE, SeriesType.DOUBLE);
  // DATE, TIME, TIMESTAMP, OBJECT  - no implementation - expect casting to string

  private final Class clazz;
  private final SeriesType seriesType;

  private static final Map<SeriesType, DataFrameFieldType> MAP = new HashMap<>();

  static {
    for (DataFrameFieldType value : values()) {
      MAP.put(value.seriesType, value);
    }
  }

  DataFrameFieldType(Primitive primitive, SeriesType seriesType) {
    this(primitive.getBoxClass(), seriesType);
  }

  DataFrameFieldType(Class clazz, SeriesType seriesType) {
    this.clazz = clazz;
    this.seriesType = seriesType;
  }

  public RelDataType toType(JavaTypeFactory typeFactory) {
    RelDataType javaType = typeFactory.createJavaType(clazz);
    RelDataType sqlType = typeFactory.createSqlType(javaType.getSqlTypeName());
    return typeFactory.createTypeWithNullability(sqlType, true);
  }

  public static @Nullable DataFrameFieldType of(Series.SeriesType seriesType) {
    return MAP.get(seriesType);
  }
}
