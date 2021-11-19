package org.apache.pinot.thirdeye.dataframe.calcite;

import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.tree.Primitive;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.pinot.thirdeye.spi.dataframe.Series;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum DataFrameFieldType {
  STRING(String.class, "string"),
  BOOLEAN(Primitive.BOOLEAN),
  LONG(Primitive.LONG),
  DOUBLE(Primitive.DOUBLE);

  private final Class clazz;
  private final String simpleName;

  DataFrameFieldType(Primitive primitive) {
    this(primitive.getBoxClass(), primitive.getPrimitiveName());
  }

  DataFrameFieldType(Class clazz, String simpleName) {
    this.clazz = clazz;
    this.simpleName = simpleName;
  }

  public RelDataType toType(JavaTypeFactory typeFactory) {
    RelDataType javaType = typeFactory.createJavaType(clazz);
    RelDataType sqlType = typeFactory.createSqlType(javaType.getSqlTypeName());
    return typeFactory.createTypeWithNullability(sqlType, true);
  }

  public static @Nullable DataFrameFieldType of(Series.SeriesType typeString) {
    switch (typeString) {
      case LONG:
        return LONG;
      case DOUBLE:
        return DOUBLE;
      case STRING:
        return STRING;
      case BOOLEAN:
        return BOOLEAN;
      case OBJECT:
        // DATE, TIME, TIMESTAMP have no specific management
        return STRING;
    }
    throw new IllegalArgumentException(String.format("%s", typeString));
  }
}
