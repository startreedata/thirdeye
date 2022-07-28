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
package ai.startree.thirdeye.util;

import ai.startree.thirdeye.rootcause.entity.MetricEntity;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable.SimpleDataTableBuilder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ThirdEyeUtils {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeUtils.class);
  private static final String PROP_METRIC_URNS_KEY = "metricUrn";
  private static final String PROP_NESTED_METRIC_URNS_KEY = "nestedMetricUrns";
  private static final String PROP_NESTED_PROPERTIES_KEY = "nested";

  @Deprecated
  public static List<DatasetConfigDTO> getDatasetConfigsFromMetricUrn(String metricUrn,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    MetricEntity me = MetricEntity.fromURN(metricUrn);
    MetricConfigDTO metricConfig = metricConfigManager.findById(me.getId());
    if (metricConfig == null) {
      return new ArrayList<>();
    }
    return Collections.singletonList(datasetConfigManager.findByDataset(metricConfig.getDataset()));
  }

  /**
   * Get rounded double value, according to the value of the double.
   * Max rounding will be up to 4 decimals
   * For values >= 0.1, use 2 decimals (eg. 123, 2.5, 1.26, 0.5, 0.162)
   * For values < 0.1, use 3 decimals (eg. 0.08, 0.071, 0.0123)
   *
   * @param value any double value
   * @return the rounded double value
   */
  public static Double getRoundedDouble(Double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      return Double.NaN;
    }
    if (value >= 0.1) {
      return Math.round(value * (Math.pow(10, 2))) / (Math.pow(10, 2));
    } else {
      return Math.round(value * (Math.pow(10, 3))) / (Math.pow(10, 3));
    }
  }

  /**
   * Get rounded double value, according to the value of the double.
   * Max rounding will be upto 4 decimals
   * For values gte 0.1, use ##.## (eg. 123, 2.5, 1.26, 0.5, 0.162)
   * For values lt 0.1 and gte 0.01, use ##.### (eg. 0.08, 0.071, 0.0123)
   * For values lt 0.01 and gte 0.001, use ##.#### (eg. 0.001, 0.00367)
   * This function ensures we don't prematurely round off double values to a fixed format, and make
   * it 0.00 or lose out information
   */
  public static String getRoundedValue(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      if (Double.isNaN(value)) {
        return Double.toString(Double.NaN);
      }
      if (value > 0) {
        return Double.toString(Double.POSITIVE_INFINITY);
      } else {
        return Double.toString(Double.NEGATIVE_INFINITY);
      }
    }
    StringBuffer decimalFormatBuffer = new StringBuffer(Constants.TWO_DECIMALS_FORMAT);
    double compareValue = 0.1;
    while (value > 0 && value < compareValue && !decimalFormatBuffer.toString().equals(
        Constants.MAX_DECIMALS_FORMAT)) {
      decimalFormatBuffer.append(Constants.DECIMALS_FORMAT_TOKEN);
      compareValue = compareValue * 0.1;
    }
    DecimalFormat decimalFormat = new DecimalFormat(decimalFormatBuffer.toString());

    return decimalFormat.format(value);
  }

  /**
   * Prints messages and stack traces of the given list of exceptions in a string.
   *
   * @param exceptions the list of exceptions to be printed.
   * @param maxWordCount the length limitation of the string; set to 0 to remove the limitation.
   * @return the string that contains the messages and stack traces of the given exceptions.
   */
  public static String exceptionsToString(List<Exception> exceptions, int maxWordCount) {
    String message = "";
    if (CollectionUtils.isNotEmpty(exceptions)) {
      StringBuilder sb = new StringBuilder();
      for (Exception exception : exceptions) {
        sb.append(ExceptionUtils.getStackTrace(exception));
        if (maxWordCount > 0 && sb.length() > maxWordCount) {
          message = sb.substring(0, maxWordCount) + "\n...";
          break;
        }
      }
      if (message.equals("")) {
        message = sb.toString();
      }
    }
    return message;
  }

  /**
   * Parse job name to get the detection id
   */
  public static long getDetectionIdFromJobName(String jobName) {
    String[] parts = jobName.split("_");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Invalid job name: " + jobName);
    }
    return Long.parseLong(parts[1]);
  }

  /**
   * Extract the list of metric urns in the detection config properties
   *
   * @param properties the detection config properties
   * @return the list of metric urns
   */
  public static Set<String> extractMetricUrnsFromProperties(Map<String, Object> properties) {
    Set<String> metricUrns = new HashSet<>();
    if (properties == null) {
      return metricUrns;
    }
    if (properties.containsKey(PROP_METRIC_URNS_KEY)) {
      metricUrns.add((String) properties.get(PROP_METRIC_URNS_KEY));
    }
    if (properties.containsKey(PROP_NESTED_METRIC_URNS_KEY)) {
      metricUrns.addAll(ConfigUtils.getList(properties.get(PROP_NESTED_METRIC_URNS_KEY)));
    }
    List<Map<String, Object>> nestedProperties = ConfigUtils
        .getList(properties.get(PROP_NESTED_PROPERTIES_KEY));
    // extract the metric urns recursively from the nested properties
    for (Map<String, Object> nestedProperty : nestedProperties) {
      metricUrns.addAll(extractMetricUrnsFromProperties(nestedProperty));
    }
    return metricUrns;
  }

  public static DataTable getDataTableFromResultSet(final ResultSet resultSet) throws SQLException {
    final List<String> columns = new ArrayList<>();
    final List<ColumnType> columnTypes = new ArrayList<>();
    final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
    final int columnCount = resultSetMetaData.getColumnCount();
    for (int i = 0; i < columnCount; i++) {
      columns.add(resultSetMetaData.getColumnLabel(i + 1));
      columnTypes.add(ColumnType.jdbcTypeToColumnType(resultSetMetaData.getColumnType(i + 1)));
    }
    final SimpleDataTableBuilder simpleDataTableBuilder = new SimpleDataTableBuilder(columns,
        columnTypes);
    while (resultSet.next()) {
      final Object[] rowData = simpleDataTableBuilder.newRow();
      for (int i = 0; i < columnCount; i++) {
        final ColumnType columnType = columnTypes.get(i);
        if (columnType.isArray()) {
          rowData[i] = resultSet.getArray(i + 1);
          continue;
        }
        switch (columnType.getType()) {
          case INT:
            rowData[i] = resultSet.getInt(i + 1);
            continue;
          case LONG:
            rowData[i] = resultSet.getLong(i + 1);
            continue;
          case DOUBLE:
            rowData[i] = resultSet.getDouble(i + 1);
            continue;
          case STRING:
            rowData[i] = resultSet.getString(i + 1);
            continue;
          case DATE:
            // todo cyril datetime is parsed as date - precision loss - use timestamp instead?
            rowData[i] = resultSet.getDate(i + 1);
            continue;
          case BOOLEAN:
            rowData[i] = resultSet.getBoolean(i + 1);
            continue;
          case BYTES:
            rowData[i] = resultSet.getBytes(i + 1);
            continue;
          default:
            throw new RuntimeException("Unrecognized data type - " + columnTypes.get(i + 1));
        }
      }
    }
    return simpleDataTableBuilder.build();
  }
}
