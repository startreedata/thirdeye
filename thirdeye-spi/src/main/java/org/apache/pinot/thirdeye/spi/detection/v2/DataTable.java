package org.apache.pinot.thirdeye.spi.detection.v2;

import java.util.List;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;

public interface DataTable extends DetectionPipelineResult {

  int getRowCount();

  int getColumnCount();

  List<String> getColumns();

  List<ColumnType> getColumnTypes();

  DataFrame getDataFrame();

  Object getObject(int rowIdx, int colIdx);

  String getString(int rowIdx, int colIdx);

  long getLong(int rowIdx, int colIdx);

  double getDouble(int rowIdx, int colIdx);
}
