package org.apache.pinot.thirdeye.datalayer.util;

import java.lang.reflect.Field;

class ColumnInfo {

  String columnNameInDB;
  int sqlType;
  String columnNameInEntity;
  Field field;
}
