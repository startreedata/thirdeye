package ai.startree.thirdeye.spi.detection.v2;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.detection.model.DetectionResult;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

public class SimpleDataTable extends AbstractDataTableImpl {

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
    final DataFrame df = new DataFrame();
    for (int colIdx = 0; colIdx < getColumnCount(); colIdx++) {
      switch (columnTypes.get(colIdx).getType()) {
        case INT:
        case LONG:
          df.addSeries(columns.get(colIdx).toLowerCase(), getLongsForColumn(colIdx));
          break;
        case DOUBLE:
          df.addSeries(columns.get(colIdx).toLowerCase(), getDoublesForColumn(colIdx));
          break;
        case BOOLEAN:
          df.addSeries(columns.get(colIdx).toLowerCase(), getBooleansForColumn(colIdx));
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

  private boolean[] getBooleansForColumn(final int colIdx) {
    boolean[] booleans = new boolean[getRowCount()];
    for (int rowId = 0; rowId < getRowCount(); rowId++) {
      booleans[rowId] = getBoolean(rowId, colIdx);
    }
    return booleans;
  }

  private String[] getStringsForColumn(final int colIdx) {
    String[] strings = new String[getRowCount()];
    for (int rowId = 0; rowId < getRowCount(); rowId++) {
      strings[rowId] = getString(rowId, colIdx);
    }
    return strings;
  }

  private double[] getDoublesForColumn(final int colIdx) {
    double[] doubles = new double[getRowCount()];
    for (int rowId = 0; rowId < getRowCount(); rowId++) {
      doubles[rowId] = getDouble(rowId, colIdx);
    }
    return doubles;
  }

  private long[] getLongsForColumn(final int colIdx) {
    long[] longs = new long[getRowCount()];
    for (int rowId = 0; rowId < getRowCount(); rowId++) {
      longs[rowId] = getLong(rowId, colIdx);
    }
    return longs;
  }

  @Override
  public boolean getBoolean(final int rowIdx, final int colIdx) {
    return Boolean.parseBoolean((dataCache.get(rowIdx))[colIdx].toString());
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
    return Double.valueOf((dataCache.get(rowIdx))[colIdx].toString()).longValue();
  }

  @Override
  public double getDouble(final int rowIdx, final int colIdx) {
    return Double.valueOf((dataCache.get(rowIdx))[colIdx].toString());
  }

  @Override
  public List<DetectionResult> getDetectionResults() {
    return ImmutableList.of(DetectionResult.from(getDataFrame()));
  }

  public static DataTable fromDataFrame(DataFrame dataFrame) {
    final List<String> columns = new ArrayList<>(dataFrame.getSeriesNames());
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

    public List<ColumnType> getColumnTypes() {
      return columnTypes;
    }

    public List<String> getColumns() {
      return columns;
    }
  }
}
