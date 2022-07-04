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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeDataFrameResultSet;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetMetaData;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import ai.startree.thirdeye.spi.detection.v2.ColumnType.ColumnDataType;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.pinot.client.ResultSet;
import org.apache.pinot.client.ResultSetGroup;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class PinotThirdEyeDataSourceUtils {

  public static PinotThirdEyeDataSourceConfig buildConfig(
      final Map<String, Object> properties) {
    final PinotThirdEyeDataSourceConfig config = new ObjectMapper()
        .convertValue(properties, PinotThirdEyeDataSourceConfig.class);

    requireNonNull(config.getControllerHost(), "Controller Host is not set.");
    checkArgument(config.getControllerPort() >= 0, "Controller Portis not set");
    requireNonNull(config.getClusterName(), "Cluster Name is not set.");
    checkArgument(Set.of(PinotThirdEyeDataSource.HTTP_SCHEME, PinotThirdEyeDataSource.HTTPS_SCHEME)
            .contains(config.getControllerConnectionScheme()),
        "Controller scheme must be  either 'http' or 'https'");

    return config;
  }

  /**
   * Constructs a ThirdEyeResultSetGroup from Pinot's {@link ResultSetGroup}.
   *
   * @param resultSetGroup a {@link ResultSetGroup} from Pinot.
   * @return a converted {@link ThirdEyeResultSetGroup}.
   */
  public static ThirdEyeResultSetGroup toThirdEyeResultSetGroup(ResultSetGroup resultSetGroup) {
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
          return ColumnType.pinotTypeToColumnType(resultSet.getColumnDataType(i));
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
  public static ThirdEyeDataFrameResultSet fromPinotResultSet(ResultSet resultSet) {
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
    ThirdEyeDataFrameResultSet thirdEyeDataFrameResultSet =
        new ThirdEyeDataFrameResultSet(thirdEyeResultSetMetaData, dataFrame);
    return thirdEyeDataFrameResultSet;
  }

  @Deprecated
  public static String getBetweenClause(DateTime start, DateTime endExclusive, TimeSpec timeSpec,
      final DatasetConfigDTO datasetConfig) {
    TimeGranularity dataGranularity = timeSpec.getDataGranularity();
    long dataGranularityMillis = dataGranularity.toMillis();

    String timeField = timeSpec.getColumnName();
    String timeFormat = timeSpec.getFormat();

    // epoch case
    if (timeFormat == null || TimeSpec.SINCE_EPOCH_FORMAT.equals(timeFormat)) {
      long startUnits = (long) Math.ceil(start.getMillis() / (double) dataGranularityMillis);
      long endUnits = (long) Math.ceil(endExclusive.getMillis() / (double) dataGranularityMillis);

      // point query
      if (startUnits == endUnits) {
        return String.format(" %s = %d", timeField, startUnits);
      }

      return String.format(" %s >= %d AND %s < %d", timeField, startUnits, timeField, endUnits);
    }

    // NOTE:
    // this is crazy. epoch rounds up, but timeFormat down
    // we maintain this behavior for backward compatibility.

    DateTimeFormatter inputDataDateTimeFormatter = DateTimeFormat.forPattern(timeFormat)
        .withZone(SpiUtils.getDateTimeZone(datasetConfig));
    String startUnits = inputDataDateTimeFormatter.print(start);
    String endUnits = inputDataDateTimeFormatter.print(endExclusive);

    // point query
    if (Objects.equals(startUnits, endUnits)) {
      return String.format(" %s = %s", timeField, startUnits);
    }

    return String.format(" %s >= %s AND %s < %s", timeField, startUnits, timeField, endUnits);
  }
}
