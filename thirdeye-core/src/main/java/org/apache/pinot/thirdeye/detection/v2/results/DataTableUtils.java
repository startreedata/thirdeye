package org.apache.pinot.thirdeye.detection.v2.results;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.detection.v2.ColumnType;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.SimpleDataTable.SimpleDataTableBuilder;

public class DataTableUtils {

  private static final String DIMENSION_NAMES = "dimension_names";
  private static final String DIMENSION_VALUES = "dimension_values";

  public static Map<DimensionInfo, DataTable> splitDataTable(DataTable dataTable) {
    Map<DimensionInfo, SimpleDataTableBuilder> splitDataTableBuilder = new HashMap<>();
    int dimensionNamesIndex = dataTable.getColumns().indexOf(DIMENSION_NAMES);
    int dimensionValuesIndex = dataTable.getColumns().indexOf(DIMENSION_VALUES);
    if (dimensionNamesIndex >= 0 && dimensionValuesIndex >= 0) {
      for (int row = 0; row < dataTable.getRowCount(); row++) {
        String dimNames = dataTable.getString(row, dimensionNamesIndex);
        String dimValues = dataTable.getString(row, dimensionValuesIndex);

        DimensionInfo dimensionInfo = new DimensionInfo(Arrays.asList(dimNames.split(",")),
            Arrays.asList(dimValues.split(",")));

        if (!splitDataTableBuilder.containsKey(dimensionInfo)) {
          splitDataTableBuilder.put(dimensionInfo, createDimensionDataTableBuilder(dataTable));
        }
        final SimpleDataTableBuilder dataTableBuilder = splitDataTableBuilder.get(dimensionInfo);
        final Object[] objects = dataTableBuilder.newRow();
        for (int columnIdx = 0; columnIdx < objects.length; columnIdx++) {
          final String columnName = dataTableBuilder.getColumns().get(columnIdx);
          final int newColIdx = dataTable.getColumns().indexOf(columnName);
          switch (dataTableBuilder.getColumnTypes().get(columnIdx).getType()) {
            case "LONG":
              objects[columnIdx] = dataTable.getLong(row, newColIdx);
              break;
            case "DOUBLE":
              objects[columnIdx] = dataTable.getDouble(row, newColIdx);
              break;
            default:
              objects[columnIdx] = dataTable.getString(row, newColIdx);
              break;
          }
        }
      }
      Map<DimensionInfo, DataTable> splitDataTable = new HashMap<>();
      for (DimensionInfo info : splitDataTableBuilder.keySet()) {
        splitDataTable.put(info, splitDataTableBuilder.get(info).build());
      }
      return splitDataTable;
    } else {
      return ImmutableMap.of(new DimensionInfo(new ArrayList<>(), new ArrayList<>()), dataTable);
    }
  }

  private static SimpleDataTableBuilder createDimensionDataTableBuilder(
      final DataTable dataTable) {
    List<String> newColumns = new ArrayList<>();
    List<ColumnType> newColumnTypes = new ArrayList<>();
    for (int i = 0; i < dataTable.getColumns().size(); i++) {
      final String columnName = dataTable.getColumns().get(i);
      final ColumnType columnType = dataTable.getColumnTypes().get(i);
      if (DIMENSION_NAMES.equals(columnName) || DIMENSION_VALUES.equals(columnName)) {
        continue;
      }
      newColumns.add(columnName);
      newColumnTypes.add(columnType);
    }
    return new SimpleDataTableBuilder(newColumns, newColumnTypes);
  }
}
