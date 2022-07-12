/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
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

  Boolean getBoolean(int rowIdx);

  Long getLong(int rowIdx);

  Double getDouble(int rowIdx);

  String getString(int rowIdx);

  Boolean getBoolean(int rowIdx, int columnIdx);

  Integer getInteger(int rowIdx, int columnIdx);

  Long getLong(int rowIdx, int columnIdx);

  Double getDouble(int rowIdx, int columnIdx);

  String getString(int rowIdx, int columnIdx);

  int getGroupKeyLength();

  String getGroupKeyColumnName(int columnIdx);

  ColumnType getGroupKeyColumnType(int columnIdx);

  String getGroupKeyColumnValue(int rowIdx, int columnIdx);
}
