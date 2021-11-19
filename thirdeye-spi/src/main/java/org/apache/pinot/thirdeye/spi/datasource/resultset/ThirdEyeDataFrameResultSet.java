/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.spi.datasource.resultset;

import com.google.common.base.Preconditions;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.apache.pinot.thirdeye.spi.detection.TimeSpec;
import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType;
import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;

/**
 * An unified container that store Select, Aggregation, and Group-By {@link ResultSet} in a data
 * frame.
 */
public class ThirdEyeDataFrameResultSet extends AbstractThirdEyeResultSet {

  private final ThirdEyeResultSetMetaData thirdEyeResultSetMetaData;
  private final DataFrame dataFrame;

  public ThirdEyeDataFrameResultSet(ThirdEyeResultSetMetaData thirdEyeResultSetMetaData,
      DataFrame dataFrame) {
    Preconditions.checkState(isMetaDataAndDataHaveSameColumns(thirdEyeResultSetMetaData, dataFrame),
        "Meta data and data's columns do not match.");

    this.thirdEyeResultSetMetaData = thirdEyeResultSetMetaData;
    this.dataFrame = dataFrame;
  }

  /**
   * Constructs a {@link ThirdEyeDataFrameResultSet} from any SQL's {@link java.sql.ResultSet}.
   *
   * @param resultSet resultset from SQL query
   * @param metric the metric the SQL is querying
   * @param groupByKeys all groupbykeys from query
   * @param aggGranularity aggregation granualrity of the query
   * @param timeSpec timeSpec of the query
   * @return an unified {@link ThirdEyeDataFrameResultSet}
   */
  public static ThirdEyeDataFrameResultSet fromSQLResultSet(java.sql.ResultSet resultSet,
      String metric,
      List<String> groupByKeys, TimeGranularity aggGranularity, TimeSpec timeSpec)
      throws Exception {

    List<String> groupKeyColumnNames = new ArrayList<>();
    List<ColumnType> groupKeyColumnTypes = new ArrayList<>();
    if (aggGranularity != null && !groupByKeys.contains(timeSpec.getColumnName())) {
      groupKeyColumnNames.add(0, DataFrame.COL_TIME);
      groupKeyColumnTypes.add(0, new ColumnType(ColumnDataType.LONG));
    }

    for (String groupByKey : groupByKeys) {
      groupKeyColumnNames.add(groupByKey);
      groupKeyColumnTypes.add(getColumnTypeFromJdbcResultSet(resultSet, groupByKey));
    }

    List<String> metrics = new ArrayList<>();
    metrics.add(metric);
    List<ColumnType> metricTypes = new ArrayList<>();
    metricTypes.add(getColumnTypeFromJdbcResultSet(resultSet, metric));
    ThirdEyeResultSetMetaData thirdEyeResultSetMetaData =
        new ThirdEyeResultSetMetaData(groupKeyColumnNames,
            metrics,
            groupKeyColumnTypes,
            metricTypes);
    // Build the DataFrame
    List<String> columnNameWithDataType = new ArrayList<>();
    //   Always cast dimension values to STRING type

    for (String groupColumnName : thirdEyeResultSetMetaData.getGroupKeyColumnNames()) {
      columnNameWithDataType.add(groupColumnName + ":STRING");
    }

    columnNameWithDataType.addAll(thirdEyeResultSetMetaData.getMetricColumnNames());
    DataFrame.Builder dfBuilder = DataFrame.builder(columnNameWithDataType);

    int metricColumnCount = metrics.size();
    int groupByColumnCount = groupKeyColumnNames.size();
    int totalColumnCount = groupByColumnCount + metricColumnCount;

    outer:
    while (resultSet.next()) {
      String[] columnsOfTheRow = new String[totalColumnCount];
      // GroupBy column value(i.e., dimension values)
      for (int groupByColumnIdx = 1; groupByColumnIdx <= groupByColumnCount; groupByColumnIdx++) {
        String valueString = null;
        try {
          valueString = resultSet.getString(groupByColumnIdx);
        } catch (Exception e) {
          // Do nothing and subsequently insert a null value to the current series.
        }
        columnsOfTheRow[groupByColumnIdx - 1] = valueString;
      }
      // Metric column's value
      for (int metricColumnIdx = 1; metricColumnIdx <= metricColumnCount; metricColumnIdx++) {
        String valueString = null;
        try {
          valueString = resultSet.getString(groupByColumnCount + metricColumnIdx);
          if (valueString == null) {
            break outer;
          }
        } catch (Exception e) {
          // Do nothing and subsequently insert a null value to the current series.
        }
        columnsOfTheRow[metricColumnIdx + groupByColumnCount - 1] = valueString;
      }
      dfBuilder.append(columnsOfTheRow);
    }

    DataFrame dataFrame = dfBuilder.build().dropNull();

    // Build ThirdEye's result set
    ThirdEyeDataFrameResultSet thirdEyeDataFrameResultSet =
        new ThirdEyeDataFrameResultSet(thirdEyeResultSetMetaData, dataFrame);
    return thirdEyeDataFrameResultSet;
  }

  public static ColumnType getColumnTypeFromJdbcResultSet(java.sql.ResultSet resultSet,
      String columnName) {
    try {
      for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
        String label = resultSet.getMetaData().getColumnLabel(i).replace("\"", "");
        if (label.contains(" as")) {
          label = label.split(" as")[1].trim().replace("\"", "");
        }
        if (label.contains("(")) {
          label = label.substring(label.indexOf("(") + 1, label.indexOf(")")).trim().replace("\"", "");
        }
        if (columnName.equalsIgnoreCase(label)) {
          return ColumnType.jdbcTypeToColumnType(resultSet.getMetaData().getColumnType(i));
        }
      }
    } catch (SQLException e) {
      return null;
    }
    return null;
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

  // todo cyril decide if this should be moved to interface
  protected DataFrame getDataFrame() {
    return dataFrame;
  }
}
