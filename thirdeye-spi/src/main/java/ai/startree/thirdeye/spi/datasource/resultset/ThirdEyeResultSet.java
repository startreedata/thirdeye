/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource.resultset;

import ai.startree.thirdeye.spi.detection.v2.ColumnType;

/**
 * An interface to mimic {@link org.apache.pinot.client.ResultSet}. Note that this class is used to
 * decouple
 * ThirdEye's data structure from Pinot's. During the initial migration, this class provides a
 * similar interface for
 * backward compatibility.
 *
 * TODO: Refine this class and promote it to a standard container that store the data from any
 * database.
 */
public interface ThirdEyeResultSet {

  int getRowCount();

  int getColumnCount();

  String getColumnName(int columnIdx);

  ColumnType getColumnType(int columnIdx);

  boolean getBoolean(int rowIdx);

  long getLong(int rowIdx);

  double getDouble(int rowIdx);

  String getString(int rowIdx);

  boolean getBoolean(int rowIdx, int columnIdx);

  int getInteger(int rowIdx, int columnIdx);

  long getLong(int rowIdx, int columnIdx);

  double getDouble(int rowIdx, int columnIdx);

  String getString(int rowIdx, int columnIdx);

  int getGroupKeyLength();

  String getGroupKeyColumnName(int columnIdx);

  ColumnType getGroupKeyColumnType(int columnIdx);

  String getGroupKeyColumnValue(int rowIdx, int columnIdx);
}
