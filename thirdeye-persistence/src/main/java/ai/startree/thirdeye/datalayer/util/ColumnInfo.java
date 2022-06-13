/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.util;

import java.lang.reflect.Field;

class ColumnInfo {

  String columnNameInDB;
  int sqlType;
  String columnNameInEntity;
  Field field;
}
