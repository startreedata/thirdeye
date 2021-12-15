package org.apache.pinot.thirdeye.spi.datasource.resultset;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.v2.AbstractDataTableImpl;
import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType;
import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeResultSetDataTable extends AbstractDataTableImpl {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeResultSetDataTable.class);

  private final ThirdEyeResultSet thirdEyeResultSet;
  private List<String> columns = new ArrayList<>();
  private List<ColumnType> columnTypes = new ArrayList<>();
  private int groupKeyLength;
  private DataFrame dataFrame;

  public ThirdEyeResultSetDataTable(final ThirdEyeResultSet thirdEyeResultSet) {
    this.thirdEyeResultSet = thirdEyeResultSet;
    this.groupKeyLength = thirdEyeResultSet.getGroupKeyLength();
    for (int i = 0; i < thirdEyeResultSet.getGroupKeyLength(); i++) {
      columns.add(thirdEyeResultSet.getGroupKeyColumnName(i));
      columnTypes.add(thirdEyeResultSet.getGroupKeyColumnType(i));
    }
    for (int i = 0; i < thirdEyeResultSet.getColumnCount(); i++) {
      columns.add(thirdEyeResultSet.getColumnName(i));
      columnTypes.add(thirdEyeResultSet.getColumnType(i));
    }
  }

  @Override
  public List<String> getColumns() {
    return this.columns;
  }

  @Override
  public int getRowCount() {
    return this.thirdEyeResultSet.getRowCount();
  }

  @Override
  public int getColumnCount() {
    return this.thirdEyeResultSet.getColumnCount() + thirdEyeResultSet.getGroupKeyLength();
  }

  @Override
  public List<ColumnType> getColumnTypes() {
    return this.columnTypes;
  }

  @Override
  public String getString(int rowIdx, int colIdx) {
    if (colIdx < groupKeyLength) {
      return thirdEyeResultSet.getGroupKeyColumnValue(rowIdx, colIdx);
    }
    return thirdEyeResultSet.getString(rowIdx, colIdx - groupKeyLength);
  }

  @Override
  public boolean getBoolean(int rowIdx, int colIdx) {
    if (colIdx < groupKeyLength) {
      return Boolean.parseBoolean(thirdEyeResultSet.getGroupKeyColumnValue(rowIdx, colIdx));
    }
    return Boolean.parseBoolean(thirdEyeResultSet.getString(rowIdx, colIdx - groupKeyLength));
  }

  @Override
  public long getLong(int rowIdx, int colIdx) {
    if (colIdx < groupKeyLength) {
      return Long.parseLong(thirdEyeResultSet.getGroupKeyColumnValue(rowIdx, colIdx));
    }
    return Long.parseLong(thirdEyeResultSet.getString(rowIdx, colIdx - groupKeyLength));
  }

  @Override
  public double getDouble(int rowIdx, int colIdx) {
    if (colIdx < groupKeyLength) {
      return Double.parseDouble(thirdEyeResultSet.getGroupKeyColumnValue(rowIdx, colIdx));
    }
    return Double.parseDouble(thirdEyeResultSet.getString(rowIdx, colIdx - groupKeyLength));
  }

  public DataFrame getDataFrame() {
    if (dataFrame == null) {
      dataFrame = generateDataFrame();
    }
    return dataFrame;
  }

  @Override
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
    List<String> columnNameWithDataType = new ArrayList<>();
    for (int i = 0; i < getColumnCount(); i++) {
      columnNameWithDataType.add(getColumns().get(i));
    }

    DataFrame.Builder dfBuilder = DataFrame.builder(columnNameWithDataType);
    for (int rowIdx = 0; rowIdx < getRowCount(); rowIdx++) {
      Object[] row = new Object[getColumnCount()];
      for (int columnIdx = 0; columnIdx < getColumnCount(); columnIdx++) {
        Object value = null;
        try {
          value = getObject(rowIdx, columnIdx);
        } catch (Exception e) {
          // will add a null
          LOG.error("Could not get value of position {},{}. Replacing by null. Error: {}",
              rowIdx, columnIdx, e);
        }
        row[columnIdx] = value;
      }
      dfBuilder.append(row);
    }
    return dfBuilder.build();
  }

  @Override
  public List<DetectionResult> getDetectionResults() {
    return ImmutableList.of(DetectionResult.from(getDataFrame()));
  }
}
