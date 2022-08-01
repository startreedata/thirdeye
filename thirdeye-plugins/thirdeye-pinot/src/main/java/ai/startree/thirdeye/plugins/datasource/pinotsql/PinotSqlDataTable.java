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
package ai.startree.thirdeye.plugins.datasource.pinotsql;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.detection.model.DetectionResult;
import ai.startree.thirdeye.spi.detection.v2.AbstractDataTableImpl;
import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import com.google.common.collect.ImmutableList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PinotSqlDataTable extends AbstractDataTableImpl {

  private final ResultSet resultset;

  public PinotSqlDataTable(ResultSet resultSet) {
    this.resultset = resultSet;
  }

  @Override
  public int getRowCount() {
    final int size;
    try {
      resultset.last();
      size = resultset.getRow();
      resultset.beforeFirst();
    } catch (SQLException throwables) {
      return 0;
    }
    return size;
  }

  @Override
  public int getColumnCount() {
    try {
      return resultset.getMetaData().getColumnCount();
    } catch (SQLException throwables) {
      return 0;
    }
  }

  @Override
  public List<String> getColumns() {
    List<String> columns = new ArrayList<>();
    try {
      for (int i = 1; i <= getColumnCount(); i++) {
        columns.add(resultset.getMetaData().getColumnName(i));
      }
    } catch (SQLException throwables) {
      return null;
    }
    return columns;
  }

  @Override
  public List<ColumnType> getColumnTypes() {
    List<ColumnType> columnTypes = new ArrayList<>();
    try {
      for (int i = 0; i < getColumnCount(); i++) {
        columnTypes.add(ColumnType.jdbcTypeToColumnType(resultset.getMetaData().getColumnType(i)));
      }
    } catch (SQLException throwables) {
      return null;
    }
    return columnTypes;
  }

  @Override
  public DataFrame getDataFrame() {
    DataFrame.Builder dfBuilder = DataFrame.builder(getColumns());
    final int columnCount = getColumnCount();
    try {
      resultset.beforeFirst();
      while (resultset.next()) {
        Object[] rowValues = new Object[columnCount];
        for (int column = 1; column <= columnCount; column++) {
          try {
            rowValues[column-1] = resultset.getObject(column);
          } catch (SQLException throwables) {
            rowValues[column-1] = null;
          }
        }
        dfBuilder.append(rowValues);
      }
    } catch (SQLException throwables) {
    }
    return dfBuilder.build().dropNull();
  }

  @Override
  public Object getObject(final int rowIdx, final int colIdx) {
    try {
      resultset.beforeFirst();
      while (resultset.getRow()!=rowIdx){
        resultset.next();
      }
      return resultset.getObject(colIdx);
    } catch (SQLException throwables) {
      return null;
    }
  }

  @Override
  public boolean getBoolean(final int rowIdx, final int colIdx) {
    return Boolean.parseBoolean(getString(rowIdx, colIdx));
  }

  @Override
  public String getString(final int rowIdx, final int colIdx) {
    return getObject(rowIdx, colIdx).toString();
  }

  @Override
  public long getLong(final int rowIdx, final int colIdx) {
    return Long.parseLong(getString(rowIdx, colIdx));
  }

  @Override
  public double getDouble(final int rowIdx, final int colIdx) {
    return Double.parseDouble(getString(rowIdx, colIdx));
  }

  @Override
  public List<DetectionResult> getDetectionResults() {
    return ImmutableList.of(DetectionResult.from(getDataFrame()));
  }
}
