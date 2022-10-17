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
package ai.startree.thirdeye.plugins.datasource.pinot;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeDataFrameResultSet;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetMetaData;
import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import ai.startree.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
import com.google.common.cache.CacheLoader;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.pinot.client.Connection;
import org.apache.pinot.client.PinotClientException;
import org.apache.pinot.client.Request;
import org.apache.pinot.client.ResultSet;
import org.apache.pinot.client.ResultSetGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PinotResponseCacheLoader extends CacheLoader<PinotQuery, ThirdEyeResultSetGroup> {

  private static final Logger LOG = LoggerFactory.getLogger(PinotResponseCacheLoader.class);

  private static final String SQL_QUERY_FORMAT = "sql";
  private static final String PQL_QUERY_FORMAT = "pql";
  private final PinotConnectionManager pinotConnectionManager;

  @Inject
  public PinotResponseCacheLoader(final PinotConnectionManager pinotConnectionManager) {
    this.pinotConnectionManager = pinotConnectionManager;
  }

  /**
   * Constructs a ThirdEyeResultSetGroup from Pinot's {@link ResultSetGroup}.
   *
   * @param resultSetGroup a {@link ResultSetGroup} from Pinot.
   * @return a converted {@link ThirdEyeResultSetGroup}.
   */
  private static ThirdEyeResultSetGroup toThirdEyeResultSetGroup(ResultSetGroup resultSetGroup) {
    List<ResultSet> resultSets = new ArrayList<>();
    for (int i = 0; i < resultSetGroup.getResultSetCount(); i++) {
      resultSets.add(resultSetGroup.getResultSet(i));
    }
    // Convert Pinot's ResultSet to ThirdEyeResultSet
    List<ThirdEyeResultSet> thirdEyeResultSets = new ArrayList<>();
    for (ResultSet resultSet : resultSets) {
      ThirdEyeResultSet thirdEyeResultSet = fromPinotResultSet(resultSet);
      thirdEyeResultSets.add(thirdEyeResultSet);
    }

    return new ThirdEyeResultSetGroup(thirdEyeResultSets);
  }

  private static ColumnType getColumnTypeFromPinotResultSet(final ResultSet resultSet,
      final String columnName) {
    for (int i = 0; i < resultSet.getColumnCount(); i++) {
      if (columnName.equalsIgnoreCase(resultSet.getColumnName(i))) {
        try {
          final ColumnType columnType = ColumnType.pinotTypeToColumnType(
              resultSet.getColumnDataType(i));
          // todo cyril - remove this - temporary adding log to understand when a client has a FLOAT column type
          if (columnType.getType().equals(ColumnDataType.FLOAT)) {
            LOG.info(
                "\"" + resultSet.getColumnName(i) + "\" column returned by Pinot is of type FLOAT");
          }
          return columnType;
        } catch (Throwable e) {
          // Pinot client doesn't provide type for pql, so default to DOUBLE type for metric column.
          return new ColumnType(ColumnDataType.DOUBLE);
        }
      }
    }
    // Pinot client doesn't provide type for groupKeys, so default to String type.
    return new ColumnType(ColumnDataType.STRING);
  }

  /**
   * Constructs a {@link ThirdEyeDataFrameResultSet} from any Pinot's {@link ResultSet}.
   *
   * @param resultSet A result set from Pinot.
   * @return an unified {@link ThirdEyeDataFrameResultSet}.
   */
  private static ThirdEyeDataFrameResultSet fromPinotResultSet(ResultSet resultSet) {
    // Build the meta data of this result set
    List<String> groupKeyColumnNames = new ArrayList<>();
    List<ColumnType> groupKeyColumnTypes = new ArrayList<>();
    int groupByColumnCount = 0;
    try {
      groupByColumnCount = resultSet.getGroupKeyLength();
    } catch (Exception e) {
      // Only happens when result set is GroupByResultSet type and contains empty result.
      // In this case, we have to use brutal force to count the number of group by columns.
      while (true) {
        try {
          resultSet.getGroupKeyColumnName(groupByColumnCount);
          ++groupByColumnCount;
        } catch (Exception breakSignal) {
          break;
        }
      }
    }
    for (int groupKeyColumnIdx = 0; groupKeyColumnIdx < groupByColumnCount; groupKeyColumnIdx++) {
      final String columnName = resultSet.getGroupKeyColumnName(groupKeyColumnIdx);
      groupKeyColumnNames.add(columnName);
      // Default to String type for all groupKeys
      groupKeyColumnTypes.add(new ColumnType(ColumnDataType.STRING));
    }
    List<String> metricColumnNames = new ArrayList<>();
    List<ColumnType> metricColumnTypes = new ArrayList<>();
    for (int columnIdx = 0; columnIdx < resultSet.getColumnCount(); columnIdx++) {
      String columnName = resultSet.getColumnName(columnIdx);
      metricColumnNames.add(columnName);
      metricColumnTypes.add(getColumnTypeFromPinotResultSet(resultSet, columnName));
    }
    ThirdEyeResultSetMetaData thirdEyeResultSetMetaData =
        new ThirdEyeResultSetMetaData(groupKeyColumnNames,
            metricColumnNames,
            groupKeyColumnTypes,
            metricColumnTypes);

    // Build the DataFrame
    List<String> columnNameWithDataType = new ArrayList<>();
    //   Always cast dimension values to STRING type
    for (String groupColumnName : thirdEyeResultSetMetaData.getGroupKeyColumnNames()) {
      columnNameWithDataType.add(groupColumnName + ":STRING");
    }
    columnNameWithDataType.addAll(thirdEyeResultSetMetaData.getMetricColumnNames());
    DataFrame.Builder dfBuilder = DataFrame.builder(columnNameWithDataType);
    int rowCount = resultSet.getRowCount();
    int metricColumnCount = resultSet.getColumnCount();
    int totalColumnCount = groupByColumnCount + metricColumnCount;
    // Dump the values in ResultSet to the DataFrame
    for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
      String[] columnsOfTheRow = new String[totalColumnCount];
      // GroupBy column value(i.e., dimension values)
      for (int groupByColumnIdx = 0; groupByColumnIdx < groupByColumnCount; groupByColumnIdx++) {
        String valueString = null;
        try {
          valueString = resultSet.getGroupKeyString(rowIdx, groupByColumnIdx);
        } catch (Exception e) {
          // Do nothing and subsequently insert a null value to the current series.
        }
        columnsOfTheRow[groupByColumnIdx] = valueString;
      }
      // Metric column's value
      for (int metricColumnIdx = 0; metricColumnIdx < metricColumnCount; metricColumnIdx++) {
        String valueString = null;
        try {
          valueString = resultSet.getString(rowIdx, metricColumnIdx);
        } catch (Exception e) {
          // Do nothing and subsequently insert a null value to the current series.
        }
        columnsOfTheRow[metricColumnIdx + groupByColumnCount] = valueString;
      }
      dfBuilder.append(columnsOfTheRow);
    }
    DataFrame dataFrame = dfBuilder.build();
    // Build ThirdEye's result set
    return new ThirdEyeDataFrameResultSet(thirdEyeResultSetMetaData, dataFrame);
  }

  @Override
  public ThirdEyeResultSetGroup load(final PinotQuery pinotQuery) {
    try {
      final Connection connection = pinotConnectionManager.get();
      final long start = System.currentTimeMillis();
      final String queryFormat = pinotQuery.isUseSql() ? SQL_QUERY_FORMAT : PQL_QUERY_FORMAT;
      final ResultSetGroup resultSetGroup = connection.execute(
          pinotQuery.getTableName(),
          new Request(queryFormat, pinotQuery.getQuery())
      );

      /* Log slow queries. anything greater than 1s */
      final long end = System.currentTimeMillis();
      final long duration = end - start;
      if (duration > 1000) {
        LOG.info("Query:{}  took:{}ms",
            pinotQuery.getQuery().replace('\n', ' '), duration);
      }

      return toThirdEyeResultSetGroup(resultSetGroup);
    } catch (final PinotClientException cause) {
      LOG.error("Error when running pql:" + pinotQuery.getQuery(), cause);
      throw new PinotClientException("Error when running pql:" + pinotQuery.getQuery(), cause);
    }
  }
}
