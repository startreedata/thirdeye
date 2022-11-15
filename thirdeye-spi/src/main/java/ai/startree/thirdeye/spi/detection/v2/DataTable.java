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
package ai.startree.thirdeye.spi.detection.v2;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// todo cyril simplify interface - most of the methods are not used
public interface DataTable extends OperatorResult {

  static Object[] getRow(final DataTable dataTable, final int rowIdx) {
    int columnCount = dataTable.getColumnCount();
    Object[] row = new Object[columnCount];
    for (int colIdx = 0; colIdx < columnCount; colIdx++) {
      row[colIdx] = dataTable.getObject(rowIdx, colIdx);
    }
    return row;
  }

  static Map<String, Object> getRecord(final List<String> columnNames, final Object[] event) {
    Map<String, Object> record = new HashMap<>();
    for (int i = 0; i < columnNames.size(); i++) {
      record.put(columnNames.get(i), event[i]);
    }
    return record;
  }

  int getRowCount();

  int getColumnCount();

  List<String> getColumns();

  List<ColumnType> getColumnTypes();

  DataFrame getDataFrame();

  Object getObject(int rowIdx, int colIdx);

  Map<String, String> getProperties();

  void addProperties(Map<String, String> metadata);
}

