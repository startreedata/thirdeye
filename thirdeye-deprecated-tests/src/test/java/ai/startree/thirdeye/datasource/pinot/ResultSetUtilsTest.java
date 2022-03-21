/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.pinot;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.ObjectSeries;
import ai.startree.thirdeye.spi.dataframe.StringSeries;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeDataFrameResultSet;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetMetaData;
import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import ai.startree.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.pinot.client.PinotClientException;
import org.apache.pinot.client.ResultSet;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ResultSetUtilsTest {
  @Test
  public void testFromPinotSelectResultSet() {
    List<String> columnArray = new ArrayList<>();
    columnArray.add("col1");
    columnArray.add("col2");
    List<ColumnType> columnTypeArray = new ArrayList<>();
    columnTypeArray.add(new ColumnType(ColumnDataType.STRING));
    columnTypeArray.add(new ColumnType(ColumnDataType.STRING));

    List<String[]> resultArray = new ArrayList<>();
    String[] row1 = new String[columnArray.size()];
    String[] row2 = new String[columnArray.size()];
    String[] row3 = new String[columnArray.size()];
    resultArray.add(row1);
    resultArray.add(row2);
    resultArray.add(row3);

    for (int rowIdx = 0; rowIdx < resultArray.size(); rowIdx++) {
      for (int columnIdx = 0; columnIdx < columnArray.size(); columnIdx++) {
        resultArray.get(rowIdx)[columnIdx] = Integer.toString(rowIdx) + columnIdx;
      }
    }

    ResultSet selectResultSet = new MockedSelectResultSet(columnArray,
        columnTypeArray,
        resultArray);

    ThirdEyeDataFrameResultSet actualDataFrameResultSet =
        ResultSetUtils.fromPinotResultSet(selectResultSet);

    DataFrame dataFrame = new DataFrame();
    dataFrame.addSeries("col1", 0, 10, 20);
    dataFrame.addSeries("col2", 1, 11, 21);
    ThirdEyeResultSetMetaData metaData = new ThirdEyeResultSetMetaData(
        Collections.emptyList(), columnArray, Collections.emptyList(), columnTypeArray);
    ThirdEyeDataFrameResultSet expectedDataFrameResultSet = new ThirdEyeDataFrameResultSet(metaData,
        dataFrame);

    Assert.assertEquals(actualDataFrameResultSet, expectedDataFrameResultSet);
    Assert.assertEquals(actualDataFrameResultSet.getGroupKeyLength(), 0);
    Assert.assertEquals(actualDataFrameResultSet.getColumnCount(), 2);
  }

  @Test
  public void testFromPinotSingleAggregationResultSet() {
    final String functionName = "sum_metric_name";

    ResultSet singleAggregationResultSet = new MockedSingleAggregationResultSet(functionName,
        new ColumnType(ColumnDataType.DOUBLE), "150.33576");
    ThirdEyeDataFrameResultSet actualDataFrameResultSet =
        ResultSetUtils.fromPinotResultSet(singleAggregationResultSet);

    ThirdEyeResultSetMetaData metaData =
        new ThirdEyeResultSetMetaData(Collections.emptyList(),
            Collections.singletonList(functionName), Collections.emptyList(),
            Collections.singletonList(new ColumnType(ColumnDataType.DOUBLE)));
    DataFrame dataFrame = new DataFrame();
    dataFrame.addSeries(functionName, 150.33576);
    ThirdEyeDataFrameResultSet expectedDataFrameResultSet = new ThirdEyeDataFrameResultSet(metaData,
        dataFrame);

    Assert.assertEquals(actualDataFrameResultSet, expectedDataFrameResultSet);
    Assert.assertEquals(actualDataFrameResultSet.getGroupKeyLength(), 0);
    Assert.assertEquals(actualDataFrameResultSet.getColumnCount(), 1);
  }

  @Test
  public void testFromPinotSingleGroupByResultSet() {
    final String functionName = "sum_metric_name";

    List<String> groupByColumnNames = new ArrayList<>();
    groupByColumnNames.add("country");
    groupByColumnNames.add("pageName");
    List<ColumnType> groupByColumnTypes = new ArrayList<>();
    groupByColumnTypes.add(new ColumnType(ColumnDataType.STRING));
    groupByColumnTypes.add(new ColumnType(ColumnDataType.STRING));

    List<MockedSingleGroupByResultSet.GroupByRowResult> resultArray = new ArrayList<>();
    resultArray.add(
        new MockedSingleGroupByResultSet.GroupByRowResult(Arrays.asList("US", "page1"), "1111"));
    resultArray.add(
        new MockedSingleGroupByResultSet.GroupByRowResult(Arrays.asList("US", "page2"), "2222.2"));
    resultArray.add(
        new MockedSingleGroupByResultSet.GroupByRowResult(Arrays.asList("IN", "page3"), "333.3"));
    resultArray.add(
        new MockedSingleGroupByResultSet.GroupByRowResult(Arrays.asList("JP", "page2"), "44444.4"));

    final List<ColumnType> columnTypes = new ArrayList<>();
    columnTypes.add(new ColumnType(ColumnDataType.DOUBLE));
    ResultSet singleGroupByResultSet = new MockedSingleGroupByResultSet(groupByColumnNames,
        functionName, columnTypes, resultArray);
    ThirdEyeDataFrameResultSet actualDataFrameResultSet =
        ResultSetUtils.fromPinotResultSet(singleGroupByResultSet);

    ThirdEyeResultSetMetaData metaData =
        new ThirdEyeResultSetMetaData(groupByColumnNames, Collections.singletonList(functionName),
            groupByColumnTypes, Collections.singletonList(new ColumnType(ColumnDataType.DOUBLE)));
    DataFrame dataFrame = new DataFrame();
    dataFrame.addSeries("country", "US", "US", "IN", "JP");
    dataFrame.addSeries("pageName", "page1", "page2", "page3", "page2");
    dataFrame.addSeries(functionName, 1111, 2222.2, 333.3, 44444.4);
    ThirdEyeDataFrameResultSet expectedDataFrameResultSet = new ThirdEyeDataFrameResultSet(metaData,
        dataFrame);

    Assert.assertEquals(actualDataFrameResultSet, expectedDataFrameResultSet);
    Assert.assertEquals(actualDataFrameResultSet.getGroupKeyLength(), 2);
    Assert.assertEquals(actualDataFrameResultSet.getColumnCount(), 1);
  }

  @Test
  public void testFromEmptyPinotSingleGroupByResultSet() {
    final String functionName = "sum_metric_name";

    List<String> groupByColumnNames = new ArrayList<>();
    groupByColumnNames.add("country");
    groupByColumnNames.add("pageName");
    List<ColumnType> groupByColumnTypes = new ArrayList<>();
    groupByColumnTypes.add(new ColumnType(ColumnDataType.STRING));
    groupByColumnTypes.add(new ColumnType(ColumnDataType.STRING));

    List<MockedSingleGroupByResultSet.GroupByRowResult> resultArray = new ArrayList<>();

    final List<ColumnType> columnTypes = Collections.singletonList(new ColumnType(ColumnDataType.DOUBLE));
    ResultSet singleGroupByResultSet = new MockedSingleGroupByResultSet(groupByColumnNames,
        functionName, columnTypes, resultArray);
    ThirdEyeDataFrameResultSet actualDataFrameResultSet =
        ResultSetUtils.fromPinotResultSet(singleGroupByResultSet);

    ThirdEyeResultSetMetaData metaData =
        new ThirdEyeResultSetMetaData(groupByColumnNames, Collections.singletonList(functionName),
            groupByColumnTypes, columnTypes);
    DataFrame dataFrame = new DataFrame();
    dataFrame.addSeries("country", StringSeries.builder().build());
    dataFrame.addSeries("pageName", StringSeries.builder().build());
    dataFrame.addSeries(functionName, ObjectSeries.builder().build());
    ThirdEyeDataFrameResultSet expectedDataFrameResultSet = new ThirdEyeDataFrameResultSet(metaData,
        dataFrame);

    Assert.assertEquals(actualDataFrameResultSet, expectedDataFrameResultSet);
  }

  private static class MockedSingleGroupByResultSet extends MockedAbstractResultSet {

    private final List<String> groupByColumnNames;
    private final String functionName;
    private final List<GroupByRowResult> resultArray;
    private final List<ColumnType> columnTypes;

    MockedSingleGroupByResultSet(List<String> groupByColumnNames, String functionName,
        List<ColumnType> columnTypes, List<GroupByRowResult> resultArray) {
      Preconditions.checkNotNull(groupByColumnNames);
      Preconditions.checkNotNull(functionName);
      Preconditions.checkNotNull(columnTypes);
      Preconditions.checkNotNull(resultArray);

      this.groupByColumnNames = groupByColumnNames;
      this.functionName = functionName;
      this.resultArray = resultArray;
      this.columnTypes = columnTypes;
    }

    @Override
    public int getRowCount() {
      return resultArray.size();
    }

    @Override
    public int getColumnCount() {
      return 1;
    }

    @Override
    public String getColumnName(int i) {
      if (i == 0) {
        return functionName;
      }
      return groupByColumnNames.get(i - 1);
    }

    @Override
    public String getColumnDataType(final int columnIndex) {
      return columnTypes.get(columnIndex).getType().name();
    }

    @Override
    public String getString(int rowIdx, int columnIdx) {
      if (columnIdx != 0) {
        throw new IllegalArgumentException("Column index has to be 0 for single group by result.");
      }
      return resultArray.get(rowIdx).value;
    }

    @Override
    public int getGroupKeyLength() {
      // This method mimics the behavior of GroupByResultSet in Pinot, which throw exceptions when the result array
      // is empty.
      try {
        return resultArray.get(0).groupByColumnValues.size();
      } catch (Exception e) {
        throw new PinotClientException(
            "For some reason, Pinot decides to throw exception when the result is empty.");
      }
    }

    @Override
    public String getGroupKeyColumnName(int i) {
      return groupByColumnNames.get(i);
    }

    @Override
    public String getGroupKeyString(int rowIdx, int columnIdx) {
      return resultArray.get(rowIdx).groupByColumnValues.get(columnIdx);
    }

    static class GroupByRowResult {

      List<String> groupByColumnValues;
      String value;

      GroupByRowResult(List<String> groupByColumnValues, String value) {
        Preconditions.checkNotNull(groupByColumnValues);
        Preconditions.checkNotNull(value);

        this.groupByColumnValues = groupByColumnValues;
        this.value = value;
      }
    }
  }

  private static class MockedSingleAggregationResultSet extends MockedAbstractResultSet {

    String functionName;
    String result;
    ColumnType columnType;

    MockedSingleAggregationResultSet(String functionName, ColumnType columnType, String result) {
      Preconditions.checkNotNull(functionName);
      Preconditions.checkNotNull(columnType);
      Preconditions.checkNotNull(result);

      this.functionName = functionName;
      this.result = result;
      this.columnType = columnType;
    }

    @Override
    public int getRowCount() {
      return 1;
    }

    @Override
    public int getColumnCount() {
      return 1;
    }

    @Override
    public String getColumnName(int i) {
      return functionName;
    }

    @Override
    public String getColumnDataType(final int columnIndex) {
      return columnType.getType().name();
    }

    @Override
    public String getString(int rowIdx, int columnIdx) {
      if (rowIdx != 0 || columnIdx != 0) {
        throw new IllegalArgumentException(
            "Row or column index has to be 0 for single aggregation result.");
      }
      return result;
    }

    @Override
    public int getGroupKeyLength() {
      return 0;
    }

    @Override
    public String getGroupKeyColumnName(int i) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getGroupKeyString(int rowIdx, int columnIdx) {
      throw new UnsupportedOperationException();
    }
  }

  private static class MockedSelectResultSet extends MockedAbstractResultSet {

    List<String> columnArray = Collections.emptyList();
    List<String[]> resultArray = Collections.emptyList();
    List<ColumnType> columnTypeArray;

    MockedSelectResultSet(List<String> columnArray, List<ColumnType> columnTypeArray,
        List<String[]> resultArray) {
      if (columnArray != null) {
        this.columnArray = columnArray;
      }
      if (resultArray != null) {
        if (resultArray.size() != 0) {
          int columnCount = resultArray.get(0).length;
          for (String[] aResultArray : resultArray) {
            int rowColumnCount = aResultArray.length;
            if (rowColumnCount != columnCount) {
              throw new IllegalArgumentException(
                  "Result array needs to have rows in the same length.");
            }
          }
        }
        this.resultArray = resultArray;
      }
      this.columnTypeArray = columnTypeArray;
    }

    @Override
    public int getRowCount() {
      return resultArray.size();
    }

    @Override
    public int getColumnCount() {
      return columnArray.size();
    }

    @Override
    public String getColumnName(int columnIdx) {
      return columnArray.get(columnIdx);
    }

    @Override
    public String getColumnDataType(final int columnIndex) {
      return columnTypeArray.get(columnIndex).getType().name();
    }

    @Override
    public String getString(int rowIdx, int columnIdx) {
      return resultArray.get(rowIdx)[columnIdx];
    }

    @Override
    public int getGroupKeyLength() {
      return 0;
    }

    @Override
    public String getGroupKeyColumnName(int i) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getGroupKeyString(int rowIdx, int columnIdx) {
      throw new UnsupportedOperationException();
    }
  }

  private static abstract class MockedAbstractResultSet implements ResultSet {

    @Override
    public int getInt(int rowIdx) {
      return getInt(rowIdx, 0);
    }

    @Override
    public long getLong(int rowIdx) {
      return getLong(rowIdx, 0);
    }

    @Override
    public float getFloat(int rowIdx) {
      return getFloat(rowIdx, 0);
    }

    @Override
    public double getDouble(int rowIdx) {
      return getDouble(rowIdx, 0);
    }

    @Override
    public String getString(int rowIdx) {
      return getString(rowIdx, 0);
    }

    @Override
    public int getInt(int rowIndex, int columnIndex) {
      return Integer.parseInt(this.getString(rowIndex, columnIndex));
    }

    @Override
    public long getLong(int rowIndex, int columnIndex) {
      return Long.parseLong(this.getString(rowIndex, columnIndex));
    }

    @Override
    public float getFloat(int rowIndex, int columnIndex) {
      return Float.parseFloat(this.getString(rowIndex, columnIndex));
    }

    @Override
    public double getDouble(int rowIndex, int columnIndex) {
      return Double.parseDouble(this.getString(rowIndex, columnIndex));
    }

    @Override
    public int getGroupKeyInt(int rowIndex, int groupKeyColumnIndex) {
      return Integer.parseInt(this.getGroupKeyString(rowIndex, groupKeyColumnIndex));
    }

    @Override
    public long getGroupKeyLong(int rowIndex, int groupKeyColumnIndex) {
      return Long.parseLong(this.getGroupKeyString(rowIndex, groupKeyColumnIndex));
    }

    @Override
    public float getGroupKeyFloat(int rowIndex, int groupKeyColumnIndex) {
      return Float.parseFloat(this.getGroupKeyString(rowIndex, groupKeyColumnIndex));
    }

    @Override
    public double getGroupKeyDouble(int rowIndex, int groupKeyColumnIndex) {
      return Double.parseDouble(this.getGroupKeyString(rowIndex, groupKeyColumnIndex));
    }
  }
}
