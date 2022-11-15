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

import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import java.util.ArrayList;
import java.util.List;

public class SimpleDataTable extends AbstractDataTableImpl {

  private final List<String> columns;
  private final List<ColumnType> columnTypes;
  private final List<Object[]> dataCache = new ArrayList<>();
  private DataFrame dataFrame;

  private SimpleDataTable(final List<String> columns, final List<ColumnType> columnTypes,
      final List<Object[]> dataCache) {
    this.columns = columns;
    this.columnTypes = columnTypes;
    this.dataCache.addAll(dataCache);
  }

  @Override
  public DataFrame getDataFrame() {
    if (dataFrame == null) {
      dataFrame = generateDataFrame();
    }
    return dataFrame;
  }

  private DataFrame generateDataFrame() {
    final DataFrame df = new DataFrame();
    for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
      switch (columnTypes.get(colIdx).getType()) {
        case INT:
        case LONG:
          df.addSeries(columns.get(colIdx).toLowerCase(), getLongsForColumn(colIdx));
          break;
        case FLOAT:
        case DOUBLE:
          df.addSeries(columns.get(colIdx).toLowerCase(), getDoublesForColumn(colIdx));
          break;
        case BOOLEAN:
          df.addSeries(columns.get(colIdx).toLowerCase(), getBooleanBytesForColumn(colIdx));
          break;
        case DATE:
        case STRING:
          df.addSeries(columns.get(colIdx).toLowerCase(), getStringsForColumn(colIdx));
          break;
        default:
          throw new RuntimeException(
              "Unrecognized column type - " + columnTypes.get(colIdx).getType()
                  + ", only support INT/LONG/DOUBLE/STRING.");
      }
    }
    return df;
  }

  private byte[] getBooleanBytesForColumn(final int colIdx) {
    byte[] bytes = new byte[dataCache.size()];
    for (int rowId = 0; rowId < dataCache.size(); rowId++) {
      bytes[rowId] = getBoolean(rowId, colIdx);
    }
    return bytes;
  }

  private String[] getStringsForColumn(final int colIdx) {
    String[] strings = new String[dataCache.size()];
    for (int rowId = 0; rowId < dataCache.size(); rowId++) {
      strings[rowId] = getString(rowId, colIdx);
    }
    return strings;
  }

  private double[] getDoublesForColumn(final int colIdx) {
    double[] doubles = new double[dataCache.size()];
    for (int rowId = 0; rowId < dataCache.size(); rowId++) {
      doubles[rowId] = getDouble(rowId, colIdx);
    }
    return doubles;
  }

  private long[] getLongsForColumn(final int colIdx) {
    long[] longs = new long[dataCache.size()];
    for (int rowId = 0; rowId < dataCache.size(); rowId++) {
      longs[rowId] = getLong(rowId, colIdx);
    }
    return longs;
  }

  public byte getBoolean(final int rowIdx, final int colIdx) {
    final Object o = dataCache.get(rowIdx)[colIdx];
    if (o != null) {
      return BooleanSeries.valueOf(Boolean.parseBoolean(o.toString()));
    }
    return BooleanSeries.NULL;
  }

  public String getString(final int rowIdx, final int colIdx) {
    final Object o = dataCache.get(rowIdx)[colIdx];
    if (o !=null) {
      return o.toString();
    }
    return StringSeries.NULL;
  }

  public long getLong(final int rowIdx, final int colIdx) {
    final Object o = dataCache.get(rowIdx)[colIdx];
    if (o != null) {
      return Double.valueOf(o.toString()).longValue();
    }
    return LongSeries.NULL;
  }

  public double getDouble(final int rowIdx, final int colIdx) {
    final Object o = dataCache.get(rowIdx)[colIdx];
    if (o != null) {
      return Double.parseDouble(o.toString());
    }
    return DoubleSeries.NULL;
  }

  public static DataTable fromDataFrame(final DataFrame dataFrame) {
    final List<String> columns = dataFrame.getSeriesNames();
    final List<ColumnType> columnTypes = new ArrayList<>();
    for (String key : columns) {
      columnTypes.add(ColumnType.seriesTypeToColumnType(dataFrame.getSeries().get(key).type()));
    }
    final SimpleDataTableBuilder simpleDataTableBuilder = new SimpleDataTableBuilder(columns,
        columnTypes);
    for (int rowNumber = 0; rowNumber < dataFrame.size(); rowNumber++) {
      Object[] rowData = simpleDataTableBuilder.newRow();
      for (int columnNumber = 0; columnNumber < columns.size(); columnNumber++) {
        String columnKey = columns.get(columnNumber);
        rowData[columnNumber] = dataFrame.get(columnKey).getObject(rowNumber);
      }
    }
    return simpleDataTableBuilder.build();
  }

  public static class SimpleDataTableBuilder {

    private final List<String> columns;
    private final List<Object[]> dataCache = new ArrayList<>();
    private final List<ColumnType> columnTypes;

    public SimpleDataTableBuilder(List<String> columns, List<ColumnType> columnTypes) {
      this.columns = columns;
      this.columnTypes = columnTypes;
    }

    public Object[] newRow() {
      final Object[] row = new Object[columns.size()];
      dataCache.add(row);
      return row;
    }

    public DataTable build() {
      return new SimpleDataTable(columns, columnTypes, this.dataCache);
    }
  }
}
