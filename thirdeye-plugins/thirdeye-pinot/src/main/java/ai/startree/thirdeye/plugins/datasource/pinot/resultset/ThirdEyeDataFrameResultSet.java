/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.plugins.datasource.pinot.resultset;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import com.google.common.base.Preconditions;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An unified container that store Select, Aggregation, and Group-By {@link ResultSet} in a data
 * frame.
 */
public class ThirdEyeDataFrameResultSet extends AbstractThirdEyeResultSet {

  private final ThirdEyeResultSetMetaData thirdEyeResultSetMetaData;
  private final DataFrame dataFrame;

  public ThirdEyeDataFrameResultSet(
      ThirdEyeResultSetMetaData thirdEyeResultSetMetaData,
      final DataFrame dataFrame) {
    Preconditions.checkState(isMetaDataAndDataHaveSameColumns(thirdEyeResultSetMetaData, dataFrame),
        "Meta data and data's columns do not match.");

    this.thirdEyeResultSetMetaData = thirdEyeResultSetMetaData;
    this.dataFrame = dataFrame;
  }

  private boolean isMetaDataAndDataHaveSameColumns(
      ThirdEyeResultSetMetaData thirdEyeResultSetMetaData, DataFrame dataFrame) {
    Set<String> metaDataAllColumns = new HashSet<>(thirdEyeResultSetMetaData.getAllColumnNames());
    return metaDataAllColumns.equals(dataFrame.getSeries().keySet());
  }

  @Override
  public int getRowCount() {
    return dataFrame.size();
  }

  @Override
  public int getColumnCount() {
    return thirdEyeResultSetMetaData.getMetricColumnNames().size();
  }

  @Override
  public String getColumnName(int columnIdx) {
    Preconditions.checkPositionIndexes(0, columnIdx,
        thirdEyeResultSetMetaData.getMetricColumnNames().size() - 1);
    return thirdEyeResultSetMetaData.getMetricColumnNames().get(columnIdx);
  }

  @Override
  public ColumnType getColumnType(final int columnIdx) {
    Preconditions.checkPositionIndexes(0, columnIdx,
        thirdEyeResultSetMetaData.getMetricColumnTypes().size() - 1);
    return thirdEyeResultSetMetaData.getMetricColumnTypes().get(columnIdx);
  }

  @Override
  public String getString(int rowIdx, int columnIdx) {
    Preconditions.checkPositionIndexes(0, columnIdx,
        thirdEyeResultSetMetaData.getMetricColumnNames().size() - 1);
    return dataFrame.get(thirdEyeResultSetMetaData.getMetricColumnNames().get(columnIdx))
        .getString(rowIdx);
  }

  @Override
  public int getGroupKeyLength() {
    return thirdEyeResultSetMetaData.getGroupKeyColumnNames().size();
  }

  @Override
  public String getGroupKeyColumnName(int columnIdx) {
    Preconditions.checkPositionIndexes(0, columnIdx, getGroupKeyLength() - 1);
    return thirdEyeResultSetMetaData.getGroupKeyColumnNames().get(columnIdx);
  }

  @Override
  public ColumnType getGroupKeyColumnType(int columnIdx) {
    Preconditions.checkPositionIndexes(0, columnIdx, getGroupKeyLength() - 1);
    return thirdEyeResultSetMetaData.getGroupKeyColumnTypes().get(columnIdx);
  }

  @Override
  public String getGroupKeyColumnValue(int rowIdx, int columnIdx) {
    Preconditions.checkPositionIndexes(0, columnIdx, getGroupKeyLength() - 1);
    return dataFrame.get(thirdEyeResultSetMetaData.getGroupKeyColumnNames().get(columnIdx))
        .getString(rowIdx);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThirdEyeDataFrameResultSet that = (ThirdEyeDataFrameResultSet) o;
    return Objects.equals(thirdEyeResultSetMetaData, that.thirdEyeResultSetMetaData) && Objects
        .equals(dataFrame,
            that.dataFrame);
  }

  @Override
  public int hashCode() {
    return Objects.hash(thirdEyeResultSetMetaData, dataFrame);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ThirdEyeDataFrameResultSet{");
    sb.append("metaData=").append(thirdEyeResultSetMetaData);
    sb.append(", data=").append(dataFrame);
    sb.append('}');
    return sb.toString();
  }
}
