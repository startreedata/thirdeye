package org.apache.pinot.thirdeye.spi.detection.v2;

import java.util.ArrayList;
import java.util.List;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;

public class SimpleDataTable implements DataTable {

  private final List<String> columns;
  private final List<ColumnType> columnTypes;
  private final List<Object[]> dataCache = new ArrayList<>();

  public SimpleDataTable(final List<String> columns, final List<ColumnType> columnTypes,
      final List<Object[]> dataCache) {
    this.columns = columns;
    this.columnTypes = columnTypes;
    this.dataCache.addAll(dataCache);
  }

  @Override
  public int getRowCount() {
    return dataCache.size();
  }

  @Override
  public int getColumnCount() {
    return columns.size();
  }

  @Override
  public List<String> getColumns() {
    return columns;
  }

  @Override
  public List<ColumnType> getColumnTypes() {
    return columnTypes;
  }

  @Override
  public DataFrame getDataFrame() {
    return null;
  }

  @Override
  public Object getObject(final int rowIdx, final int colIdx) {
    return (dataCache.get(rowIdx))[colIdx];
  }

  @Override
  public String getString(final int rowIdx, final int colIdx) {
    return (dataCache.get(rowIdx))[colIdx].toString();
  }

  @Override
  public long getLong(final int rowIdx, final int colIdx) {
    return Long.parseLong((dataCache.get(rowIdx))[colIdx].toString());
  }

  @Override
  public double getDouble(final int rowIdx, final int colIdx) {
    return Double.parseDouble((dataCache.get(rowIdx))[colIdx].toString());
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

    public List<ColumnType> getColumnTypes() {
      return columnTypes;
    }

    public List<String> getColumns() {
      return columns;
    }
  }

  @Override
  public List<DetectionResult> getDetectionResults() {
    throw new UnsupportedOperationException("Not supported");
  }
}
