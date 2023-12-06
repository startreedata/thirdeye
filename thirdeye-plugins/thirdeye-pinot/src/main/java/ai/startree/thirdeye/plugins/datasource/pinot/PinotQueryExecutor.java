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

import ai.startree.thirdeye.plugins.datasource.pinot.resultset.ThirdEyeDataFrameResultSet;
import ai.startree.thirdeye.plugins.datasource.pinot.resultset.ThirdEyeResultSet;
import ai.startree.thirdeye.plugins.datasource.pinot.resultset.ThirdEyeResultSetGroup;
import ai.startree.thirdeye.plugins.datasource.pinot.resultset.ThirdEyeResultSetMetaData;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import ai.startree.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
import ai.startree.thirdeye.spi.util.Pair;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
public class PinotQueryExecutor extends CacheLoader<PinotQuery, ThirdEyeResultSetGroup> {

  private static final Logger LOG = LoggerFactory.getLogger(PinotQueryExecutor.class);

  private static final String SQL_QUERY_FORMAT = "sql";
  private static final String PQL_QUERY_FORMAT = "pql";
  private final PinotConnectionManager pinotConnectionManager;

  @Inject
  public PinotQueryExecutor(final PinotConnectionManager pinotConnectionManager) {
    this.pinotConnectionManager = pinotConnectionManager;
  }

  /**
   * Constructs a ThirdEyeResultSetGroup from Pinot's {@link ResultSetGroup}.
   *
   * @param resultSetGroup a {@link ResultSetGroup} from Pinot.
   * @return a converted {@link ThirdEyeResultSetGroup}.
   */
  private static ThirdEyeResultSetGroup toThirdEyeResultSetGroup(
      final ResultSetGroup resultSetGroup) {
    final List<ResultSet> resultSets = new ArrayList<>();
    for (int i = 0; i < resultSetGroup.getResultSetCount(); i++) {
      resultSets.add(resultSetGroup.getResultSet(i));
    }
    // Convert Pinot's ResultSet to ThirdEyeResultSet
    final List<ThirdEyeResultSet> thirdEyeResultSets = new ArrayList<>();
    for (final ResultSet resultSet : resultSets) {
      final ThirdEyeResultSet thirdEyeResultSet = fromPinotResultSet(resultSet);
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
        } catch (final Throwable e) {
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
  private static ThirdEyeDataFrameResultSet fromPinotResultSet(final ResultSet resultSet) {
    // Build the meta data of this result set
    final List<String> groupKeyColumnNames = new ArrayList<>();
    final List<ColumnType> groupKeyColumnTypes = new ArrayList<>();
    int groupByColumnCount = 0;
    try {
      groupByColumnCount = resultSet.getGroupKeyLength();
    } catch (final Exception e) {
      // Only happens when result set is GroupByResultSet type and contains empty result.
      // In this case, we have to use brutal force to count the number of group by columns.
      while (true) {
        try {
          resultSet.getGroupKeyColumnName(groupByColumnCount);
          ++groupByColumnCount;
        } catch (final Exception breakSignal) {
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
    final List<String> metricColumnNames = new ArrayList<>();
    final List<ColumnType> metricColumnTypes = new ArrayList<>();
    for (int columnIdx = 0; columnIdx < resultSet.getColumnCount(); columnIdx++) {
      final String columnName = resultSet.getColumnName(columnIdx);
      metricColumnNames.add(columnName);
      metricColumnTypes.add(getColumnTypeFromPinotResultSet(resultSet, columnName));
    }
    final ThirdEyeResultSetMetaData thirdEyeResultSetMetaData =
        new ThirdEyeResultSetMetaData(groupKeyColumnNames,
            metricColumnNames,
            groupKeyColumnTypes,
            metricColumnTypes);

    // Build the DataFrame - FIXME CYRIL - this is not necessary to build a DataFrame at this point
    final List<String> columnNameWithDataType = new ArrayList<>();
    //   Always cast dimension values to STRING type
    for (final String groupColumnName : thirdEyeResultSetMetaData.getGroupKeyColumnNames()) {
      columnNameWithDataType.add(groupColumnName + ":STRING");
    }
    columnNameWithDataType.addAll(thirdEyeResultSetMetaData.getMetricColumnNames());
    final DataFrame.Builder dfBuilder = DataFrame.builder(columnNameWithDataType);
    final int rowCount = resultSet.getRowCount();
    final int metricColumnCount = resultSet.getColumnCount();
    final int totalColumnCount = groupByColumnCount + metricColumnCount;
    // Dump the values in ResultSet to the DataFrame
    for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
      final String[] columnsOfTheRow = new String[totalColumnCount];
      // GroupBy column value(i.e., dimension values)
      for (int groupByColumnIdx = 0; groupByColumnIdx < groupByColumnCount; groupByColumnIdx++) {
        String valueString = null;
        try {
          valueString = resultSet.getGroupKeyString(rowIdx, groupByColumnIdx);
        } catch (final Exception e) {
          // Do nothing and subsequently insert a null value to the current series.
        }
        columnsOfTheRow[groupByColumnIdx] = valueString;
      }
      // Metric column's value
      for (int metricColumnIdx = 0; metricColumnIdx < metricColumnCount; metricColumnIdx++) {
        String valueString = null;
        try {
          valueString = resultSet.getString(rowIdx, metricColumnIdx);
        } catch (final Exception e) {
          // Do nothing and subsequently insert a null value to the current series.
        }
        columnsOfTheRow[metricColumnIdx + groupByColumnCount] = valueString;
      }
      dfBuilder.append(columnsOfTheRow);
    }
    final DataFrame dataFrame = dfBuilder.build();
    // Build ThirdEye's result set
    return new ThirdEyeDataFrameResultSet(thirdEyeResultSetMetaData, dataFrame);
  }

  private static List<Pair<Integer, Integer>> rowColCounts(final ResultSetGroup resultSetGroup) {
    final int resultSetCount = resultSetGroup.getResultSetCount();
    final List<Pair<Integer, Integer>> rowColCounts = new ArrayList<>(resultSetCount);
    for (int i = 0; i < resultSetCount; ++i) {
      final ResultSet resultSet = resultSetGroup.getResultSet(i);
      rowColCounts.add(Pair.pair(resultSet.getRowCount(), resultSet.getColumnCount()));
    }
    return rowColCounts;
  }

  private static String toString(final List<Pair<Integer, Integer>> pairs) {
    return pairs.stream()
        .map(p -> String.format("(%d, %d)", p.getFirst(), p.getSecond()))
        .collect(Collectors.joining(", "));
  }

  @Override
  public ThirdEyeResultSetGroup load(final PinotQuery pinotQuery) {
    final String queryWithOptions = buildQueryWithOptions(pinotQuery);
    try {
      final Connection connection = pinotConnectionManager.get();
      final long start = System.nanoTime();
      final String queryFormat = pinotQuery.isUseSql() ? SQL_QUERY_FORMAT : PQL_QUERY_FORMAT;
      final ResultSetGroup resultSetGroup = connection.execute(
          pinotQuery.getTableName(),
          new Request(queryFormat, queryWithOptions)
      );

      final long end = System.nanoTime();
      final long durationMillis = (end - start) / TimeUnit.MILLISECONDS.toNanos(1);
      LOG.info("Query:{} time:{}ms result stats(rows, cols): {}",
          queryWithOptions.replace('\n', ' '),
          durationMillis,
          toString(rowColCounts(resultSetGroup)));

      return toThirdEyeResultSetGroup(resultSetGroup);
    } catch (final PinotClientException cause) {
      LOG.error("Error when running SQL:" + queryWithOptions, cause);
      throw new PinotClientException("Error when running SQL:" + queryWithOptions, cause);
    }
  }

  @VisibleForTesting
  protected static String buildQueryWithOptions(final PinotQuery pinotQuery) {
    final StringBuilder optionsStatements = new StringBuilder();
    for (final Entry<String, String> option : pinotQuery.getOptions().entrySet()) {
      // SET optionKey = optionValue;
      optionsStatements.append("SET ");
      optionsStatements.append(option.getKey());
      optionsStatements.append(" = ");
      optionsStatements.append(option.getValue());
      optionsStatements.append(";");
    }
    return optionsStatements + pinotQuery.getQuery();
  }
}
