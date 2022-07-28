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
package ai.startree.thirdeye.plugins.datasource.sql;

import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.metric.MetricAggFunction;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlUtils {

  private static final Logger LOG = LoggerFactory.getLogger(SqlUtils.class);

  /**
   * Insert a table to SQL database, currently only used by H2, that can be read by ThirdEye
   *
   * @param ds DataSource object
   * @param tableName table name
   * @param timeColumn time column name
   * @param metrics list of metrics
   * @param dimensions list of dimensions
   * @throws SQLException SQL exception if SQL failed
   */
  public static void createTableOverride(DataSource ds, String tableName,
      String timeColumn, List<String> metrics, List<String> dimensions) throws SQLException {
    StringBuilder sb = new StringBuilder();
    sb.append("drop table if exists ").append(tableName).append(";");
    sb.append("create table ").append(tableName).append(" (");

    for (String metric : metrics) {
      sb.append(metric).append(" decimal(50,3), ");
    }
    for (String dimension : dimensions) {
      sb.append(dimension).append(" varchar(50), ");
    }
    sb.append(timeColumn).append(" varchar(50) ) ENGINE=InnoDB;");

    String sql = sb.toString();

    LOG.info("Creating H2 table: " + sql);

    try (Connection connection = ds.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }

  /**
   * Run SQL query to insert a row in a CSV file to a datasource, for now only used by H2
   * initialization
   *
   * @param tableName table name
   * @param columnNames column names in CSV, separated by ,
   * @param items row items
   */
  public static void insertCSVRow(DataSource ds, String tableName, String columnNames,
      String[] items) throws SQLException {
    // Put quotes around values that contains spaces
    StringBuilder sb = new StringBuilder();
    String prefix = "";
    for (String item : items) {
      sb.append(prefix);
      prefix = ",";
      if (!StringUtils.isNumeric(item)) {
        sb.append('\'').append(item).append('\'');
      } else {
        sb.append(item);
      }
    }

    String sql = String
        .format("INSERT INTO %s(%s) VALUES(%s)", tableName, columnNames, sb);
    try (Connection connection = ds.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }

  /**
   * Onboard dataset config and metric config from SqlDataset object. If the current dataset/metric
   * exists,
   * just update them.
   *
   * @param dataset SqlDataset Object
   */
  public static void onBoardSqlDataset(SqlDataset dataset,
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager) {
    List<MetricConfigDTO> metricConfigs = new ArrayList<>();

    String datasetName = dataset.getTableName();
    List<String> sortedDimensions = dataset.getDimensions();
    Collections.sort(sortedDimensions);

    DatasetConfigDTO datasetConfig = datasetConfigManager.findByDataset(datasetName);

    if (datasetConfig == null) {
      datasetConfig = new DatasetConfigDTO();
    }

    datasetConfig.setDataset(datasetName);
    datasetConfig.setDataSource(SqlThirdEyeDataSource.class.getSimpleName());
    datasetConfig.setDimensions(sortedDimensions);
    datasetConfig.setTimezone(dataset.getTimezone());
    datasetConfig.setTimeColumn(dataset.getTimeColumn());
    datasetConfig.setTimeFormat(dataset.getTimeFormat());

    for (Map.Entry<String, MetricAggFunction> metric : dataset.getMetrics().entrySet()) {
      MetricConfigDTO metricConfig = metricConfigManager
          .findByMetricAndDataset(metric.getKey(), datasetName);
      if (metricConfig == null) {
        metricConfig = new MetricConfigDTO();
      }

      metricConfig.setName(metric.getKey());
      metricConfig.setDataset(datasetName);
      metricConfig.setAlias(String.format("%s::%s", datasetName, metric.getKey()));
      metricConfig.setDefaultAggFunction(metric.getValue().toString());
      metricConfigs.add(metricConfig);
    }

    for (MetricConfigDTO metricConfig : metricConfigs) {
      Long id = metricConfigManager.save(metricConfig);
      if (id != null) {
        LOG.info("Created metric '{}' with id {}", metricConfig.getAlias(), id);
      } else {
        String warning = String.format("Could not create metric %s", metricConfig.getAlias());
        LOG.warn(warning);
        throw new RuntimeException(warning);
      }
    }

    Long id = datasetConfigManager.save(datasetConfig);
    if (id != null) {
      LOG.info("Created dataset '{}' with id {}", datasetConfig.getDataset(), id);
    } else {
      String warning = String.format("Could not create dataset %s", datasetConfig.getDataset());
      LOG.warn(warning);
      throw new RuntimeException(warning);
    }
  }

  static String getMaxDataTimeSQL(String timeColumn, String tableName, String sourceName) {
    return "SELECT MAX(" + timeColumn + ") FROM " + tableName;
  }

  static String getDimensionFiltersSQL(String dimension, String tableName, String sourceName) {
    return "SELECT DISTINCT(" + dimension + ") FROM " + tableName;
  }

  static String computeSqlTableName(String datasetName) {
    String[] tableComponents = datasetName.split("\\.");
    return datasetName.substring(tableComponents[0].length() + tableComponents[1].length() + 2);
  }
}
