/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.plugins.datasource.pinot;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import ai.startree.thirdeye.spi.detection.v2.AbstractDataTableImpl;
import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import ai.startree.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeResultSetDataTable extends AbstractDataTableImpl {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeResultSetDataTable.class);

  private final ThirdEyeResultSet thirdEyeResultSet;
  private final List<String> columns;
  private final List<ColumnType> columnTypes;
  private final int groupKeyLength;
  private DataFrame dataFrame;

  public ThirdEyeResultSetDataTable(final ThirdEyeResultSet thirdEyeResultSet) {
    this.thirdEyeResultSet = thirdEyeResultSet;
    this.groupKeyLength = thirdEyeResultSet.getGroupKeyLength();
    final int initialCapacity =
        thirdEyeResultSet.getGroupKeyLength() + thirdEyeResultSet.getColumnCount();
    columns = new ArrayList<>(initialCapacity);
    columnTypes = new ArrayList<>(initialCapacity);
    for (int i = 0; i < thirdEyeResultSet.getGroupKeyLength(); i++) {
      columns.add(thirdEyeResultSet.getGroupKeyColumnName(i));
      columnTypes.add(thirdEyeResultSet.getGroupKeyColumnType(i));
    }
    for (int i = 0; i < thirdEyeResultSet.getColumnCount(); i++) {
      columns.add(thirdEyeResultSet.getColumnName(i));
      columnTypes.add(thirdEyeResultSet.getColumnType(i));
    }
  }

  public DataFrame getDataFrame() {
    if (dataFrame == null) {
      dataFrame = generateDataFrame();
    }
    return dataFrame;
  }

  public Object getObject(final int rowIdx, final int colIdx) {
    if (colIdx < groupKeyLength) {
      return thirdEyeResultSet.getGroupKeyColumnValue(rowIdx, colIdx);
    }
    final ColumnDataType type = columnTypes.get(colIdx).getType();
    switch (type) {
      case BOOLEAN:
        return thirdEyeResultSet.getBoolean(rowIdx, colIdx - groupKeyLength);
      case INT:
        return thirdEyeResultSet.getInteger(rowIdx, colIdx - groupKeyLength);
      case LONG:
        return thirdEyeResultSet.getLong(rowIdx, colIdx - groupKeyLength);
      case FLOAT:
      case DOUBLE:
        return thirdEyeResultSet.getDouble(rowIdx, colIdx - groupKeyLength);
      case STRING:
        return thirdEyeResultSet.getString(rowIdx, colIdx - groupKeyLength);
      default:
        throw new RuntimeException(
            "Unrecognized column type - " + type + ", only supports LONG/DOUBLE/STRING.");
    }
  }

  private DataFrame generateDataFrame() {
    // todo cyril- at build() time this creates object series then uses inferType() to cast to correct types
    //  does not look efficient but I could not identify a hotspot in method profiling - so refactoring was not prioritized
    DataFrame.Builder dfBuilder = DataFrame.builder(columns);
    for (int rowIdx = 0; rowIdx < thirdEyeResultSet.getRowCount(); rowIdx++) {
      Object[] row = new Object[columns.size()];
      for (int columnIdx = 0; columnIdx < columns.size(); columnIdx++) {
        Object value = null;
        try {
          value = getObject(rowIdx, columnIdx);
        } catch (Exception e) {
          // will add a null
          LOG.error("Could not get value of position {},{}. Replacing by null. Error: ",
              rowIdx, columnIdx, e);
        }
        row[columnIdx] = value;
      }
      dfBuilder.append(row);
    }
    return dfBuilder.build();
  }
}
