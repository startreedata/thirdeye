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
import ai.startree.thirdeye.spi.datasource.DataSourceUtils;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeDataFrameResultSet;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSet;
import ai.startree.thirdeye.spi.datasource.resultset.ThirdEyeResultSetGroup;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheLoader;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a CacheLoader which issue queries to Presto or MySQL
 * It contains connection pools(DataSource) for each Presto or MySQL database configured in
 * data-sources-configs
 */
public class SqlResponseCacheLoader extends CacheLoader<SqlQuery, ThirdEyeResultSetGroup> {

  public static final int INIT_CONNECTIONS = 20;
  public static final String DATASETS = "datasets";
  public static final String H2 = "H2";
  public static final String USER = "user";
  public static final String DB = "db";
  public static final String PASSWORD = "password";
  public static final String DRIVER = "driver";
  public static final int ABANDONED_TIMEOUT = 60000;
  private static final Logger LOG = LoggerFactory.getLogger(SqlResponseCacheLoader.class);
  private static final String PRESTO = "Presto";
  private static final String MYSQL = "MySQL";
  private static final String VERTICA = "Vertica";
  private static final String BIGQUERY = "BigQuery";
  private static final Map<String, String> prestoDBNameToURLMap = new HashMap<>();
  private static final Map<String, String> mysqlDBNameToURLMap = new HashMap<>();
  private static final Map<String, String> verticaDBNameToURLMap = new HashMap<>();
  private static final Map<String, String> BigQueryDBNameToURLMap = new HashMap<>();
  public static int MAX_CONNECTIONS = 50;
  private static String h2Url;
  private final Map<String, DataSource> prestoDBNameToDataSourceMap = new HashMap<>();
  private final Map<String, DataSource> mysqlDBNameToDataSourceMap = new HashMap<>();
  private final Map<String, DataSource> verticaDBNameToDataSourceMap = new HashMap<>();
  private final Map<String, DataSource> BigQueryDBNameToDataSourceMap = new HashMap<>();
  DataSource h2DataSource;

  public SqlResponseCacheLoader(Map<String, Object> properties,
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager) {

    // Init Presto datasources
    if (properties.containsKey(PRESTO)) {
      List<Map<String, Object>> prestoMapList = ConfigUtils.getList(properties.get(PRESTO));
      for (Map<String, Object> objMap : prestoMapList) {
        Map<String, String> dbNameToURLMap = (Map) objMap.get(DB);
        String prestoUser = (String) objMap.get(USER);
        String prestoPassword = getPassword(objMap);

        for (Map.Entry<String, String> entry : dbNameToURLMap.entrySet()) {
          DataSource dataSource = new DataSource();
          dataSource.setInitialSize(INIT_CONNECTIONS);
          dataSource.setMaxActive(MAX_CONNECTIONS);
          dataSource.setUsername(prestoUser);
          dataSource.setPassword(prestoPassword);
          dataSource.setUrl(entry.getValue());

          // Timeout before an abandoned(in use) connection can be removed.
          dataSource.setRemoveAbandonedTimeout(ABANDONED_TIMEOUT);
          dataSource.setRemoveAbandoned(true);

          prestoDBNameToDataSourceMap.put(entry.getKey(), dataSource);
          prestoDBNameToURLMap.putAll(dbNameToURLMap);
        }
      }
    }

    // Init MySQL datasources
    if (properties.containsKey(MYSQL)) {
      List<Map<String, Object>> mysqlMapList = ConfigUtils.getList(properties.get(MYSQL));
      for (Map<String, Object> objMap : mysqlMapList) {
        Map<String, String> dbNameToURLMap = (Map) objMap.get(DB);
        String mysqlUser = (String) objMap.get(USER);
        String mysqlPassword = getPassword(objMap);

        for (Map.Entry<String, String> entry : dbNameToURLMap.entrySet()) {
          DataSource dataSource = new DataSource();
          dataSource.setInitialSize(INIT_CONNECTIONS);
          dataSource.setMaxActive(MAX_CONNECTIONS);
          dataSource.setUsername(mysqlUser);
          dataSource.setPassword(mysqlPassword);
          dataSource.setUrl(entry.getValue());

          // Timeout before an abandoned(in use) connection can be removed.
          dataSource.setRemoveAbandonedTimeout(ABANDONED_TIMEOUT);
          dataSource.setRemoveAbandoned(true);

          mysqlDBNameToDataSourceMap.put(entry.getKey(), dataSource);
          mysqlDBNameToURLMap.putAll(dbNameToURLMap);
        }
      }
    }

    // Init Vertica datasources
    if (properties.containsKey(VERTICA)) {
      List<Map<String, Object>> verticaMapList = ConfigUtils.getList(properties.get(VERTICA));
      for (Map<String, Object> objMap : verticaMapList) {
        Map<String, String> dbNameToURLMap = (Map) objMap.get(DB);
        String verticaUser = (String) objMap.get(USER);
        String verticaPassword = getPassword(objMap);
        String verticaDriver = (String) objMap.get(DRIVER);

        for (Map.Entry<String, String> entry : dbNameToURLMap.entrySet()) {
          DataSource dataSource = new DataSource();
          dataSource.setInitialSize(INIT_CONNECTIONS);
          dataSource.setMaxActive(MAX_CONNECTIONS);
          dataSource.setUsername(verticaUser);
          dataSource.setPassword(verticaPassword);
          dataSource.setDriverClassName(verticaDriver);
          dataSource.setUrl(entry.getValue());

          // Timeout before an abandoned(in use) connection can be removed.
          dataSource.setRemoveAbandonedTimeout(ABANDONED_TIMEOUT);
          dataSource.setRemoveAbandoned(true);

          verticaDBNameToDataSourceMap.put(entry.getKey(), dataSource);
          verticaDBNameToURLMap.putAll(dbNameToURLMap);
        }
      }
    }

    // Init BigQuery datasources
    if (properties.containsKey(BIGQUERY)) {
      List<Map<String, Object>> bigQueryMapList = ConfigUtils.getList(properties.get(BIGQUERY));
      for (Map<String, Object> objMap : bigQueryMapList) {
        System.out.println(bigQueryMapList);
        Map<String, String> dbNameToURLMap = (Map) objMap.get(DB);
        String bigQueryDriver = (String) objMap.get(DRIVER);

        for (Map.Entry<String, String> entry : dbNameToURLMap.entrySet()) {
          DataSource dataSource = new DataSource();
          dataSource.setInitialSize(INIT_CONNECTIONS);
          dataSource.setMaxActive(MAX_CONNECTIONS);
          dataSource.setDriverClassName(bigQueryDriver);
          dataSource.setUrl(entry.getValue());

          // Timeout before an abandoned(in use) connection can be removed.
          dataSource.setRemoveAbandonedTimeout(ABANDONED_TIMEOUT);
          dataSource.setRemoveAbandoned(true);

          BigQueryDBNameToDataSourceMap.put(entry.getKey(), dataSource);
          BigQueryDBNameToURLMap.putAll(dbNameToURLMap);
        }
      }
    }

    // Init H2 datasource
    if (properties.containsKey(H2)) {
      h2DataSource = new DataSource();
      Map<String, Object> objMap = ConfigUtils.getMap(properties.get(H2));

      h2DataSource.setInitialSize(INIT_CONNECTIONS);
      h2DataSource.setMaxActive(MAX_CONNECTIONS);
      String h2User = (String) objMap.get(USER);
      String h2Password = getPassword(objMap);
      h2Url = (String) objMap.get(DB);
      h2DataSource.setUsername(h2User);
      h2DataSource.setPassword(h2Password);
      h2DataSource.setUrl(h2Url);

      // Timeout before an abandoned(in use) connection can be removed.
      h2DataSource.setRemoveAbandonedTimeout(ABANDONED_TIMEOUT);
      h2DataSource.setRemoveAbandoned(true);

      if (objMap.containsKey(DATASETS)) {
        try {
          ObjectMapper mapper = new ObjectMapper();
          List<Object> objs = (List) objMap.get(DATASETS);
          for (Object obj : objs) {
            SqlDataset dataset = mapper.convertValue(obj, SqlDataset.class);

            String[] tableNameSplit = dataset.getTableName().split("\\.");
            String tableName = tableNameSplit[tableNameSplit.length - 1];

            List<String> metrics = new ArrayList<>(dataset.getMetrics().keySet());

            SqlUtils.createTableOverride(h2DataSource, tableName, dataset.getTimeColumn(), metrics,
                dataset.getDimensions());
            SqlUtils.onBoardSqlDataset(dataset,
                metricConfigManager,
                datasetConfigManager);

            DateTimeFormatter fmt = DateTimeFormat.forPattern(dataset.getTimeFormat())
                .withZone(DateTimeZone.forID(dataset.getTimezone()));

            if (dataset.getDataFile().length() > 0) {
              String thirdEyeConfigDir = System.getProperty("dw.rootDir");
              String fileURI = thirdEyeConfigDir + "/data/" + dataset.getDataFile();
              File file = new File(fileURI);
              try (Scanner scanner = new Scanner(file)) {
                String columnNames = scanner.nextLine();
                while (scanner.hasNextLine()) {
                  String line = scanner.nextLine();
                  String[] columnValues = line.split(",");
                  columnValues[0] = fmt.print(DateTime.parse(columnValues[0], fmt));
                  SqlUtils.insertCSVRow(h2DataSource, tableName, columnNames, columnValues);
                }
              }
            }
          }
        } catch (Exception e) {
          LOG.error(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    }
  }

  private String getPassword(Map<String, Object> objMap) {
    String password = (String) objMap.get(PASSWORD);
    password = (password == null) ? "" : password;
    return password;
  }

  /**
   * Returns the max time in millis for dataset in presto
   *
   * @return max date time in millis
   */
  public long getMaxDataTime(final DatasetConfigDTO datasetConfig) throws Exception {
    String dataset = datasetConfig.getDataset();
    LOG.info("Getting max data time for " + dataset);
    TimeSpec timeSpec = DataSourceUtils.getTimestampTimeSpecFromDatasetConfig(datasetConfig);
    DateTimeZone timeZone = SpiUtils.getDateTimeZone(datasetConfig);
    long maxTime = 0;

    String sourceName = dataset.split("\\.")[0];
    String tableName = SqlUtils.computeSqlTableName(dataset);
    DataSource dataSource = getDataSourceFromDataset(dataset);

    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            SqlUtils.getMaxDataTimeSQL(timeSpec.getColumnName(), tableName, sourceName))) {
      if (rs.next()) {
        String maxTimeString = rs.getString(1);
        if (maxTimeString.indexOf('.') >= 0) {
          maxTimeString = maxTimeString.substring(0, maxTimeString.indexOf('.'));
        }

        String timeFormat = timeSpec.getFormat();

        if (StringUtils.isBlank(timeFormat) || TimeSpec.SINCE_EPOCH_FORMAT.equals(timeFormat)) {
          maxTime = timeSpec.getDataGranularity()
              .toMillis(Long.valueOf(maxTimeString) - 1, timeZone);
        } else {
          DateTimeFormatter inputDataDateTimeFormatter =
              DateTimeFormat.forPattern(timeFormat).withZone(timeZone);
          DateTime endDateTime = DateTime.parse(maxTimeString, inputDataDateTimeFormatter);
          Period oneBucket = datasetConfig.bucketTimeGranularity().toPeriod();
          maxTime = endDateTime.plus(oneBucket).getMillis() - 1;
        }
      }
    } catch (Exception e) {
      throw e;
    }
    return maxTime;
  }

  @Override
  public ThirdEyeResultSetGroup load(SqlQuery SQLQuery) throws Exception {
    String sourceName = SQLQuery.getSourceName();
    DataSource dataSource = null;
    if (sourceName.equals(PRESTO)) {
      dataSource = prestoDBNameToDataSourceMap.get(SQLQuery.getDbName());
    } else if (sourceName.equals(MYSQL)) {
      dataSource = mysqlDBNameToDataSourceMap.get(SQLQuery.getDbName());
    } else if (sourceName.equals(VERTICA)) {
      dataSource = verticaDBNameToDataSourceMap.get(SQLQuery.getDbName());
    } else if (sourceName.equals(BIGQUERY)) {
      dataSource = BigQueryDBNameToDataSourceMap.get(SQLQuery.getDbName());
    } else {
      dataSource = h2DataSource;
    }

    String sqlQuery = SQLQuery.getQuery();
    LOG.info("Running SQL: " + sqlQuery);
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sqlQuery)) {

      ThirdEyeResultSet resultSet = ThirdEyeDataFrameResultSet.fromSQLResultSet(rs,
          SQLQuery.getMetric(),
          SQLQuery.getGroupByKeys(),
          SQLQuery.getGranularity(),
          SQLQuery.getTimeSpec());

      List<ThirdEyeResultSet> thirdEyeResultSets = new ArrayList<>();
      thirdEyeResultSets.add(resultSet);
      return new ThirdEyeResultSetGroup(thirdEyeResultSets);
    } catch (Exception e) {
      throw e;
    }
  }

  /**
   * Helper method that return a DataSource object corresponding to the dataset
   *
   * @param dataset name of dataset
   * @return DataSource object: datasource for the dataset
   */
  private DataSource getDataSourceFromDataset(String dataset) {
    String[] tableComponents = dataset.split("\\.");
    String sourceName = tableComponents[0];
    String dbName = tableComponents[1];

    if (sourceName.equals(PRESTO)) {
      return prestoDBNameToDataSourceMap.get(dbName);
    } else if (sourceName.equals(MYSQL)) {
      return mysqlDBNameToDataSourceMap.get(dbName);
    } else if (sourceName.equals(VERTICA)) {
      return verticaDBNameToDataSourceMap.get(dbName);
    } else if (sourceName.equals(BIGQUERY)) {
      return BigQueryDBNameToDataSourceMap.get(dbName);
    } else {
      return h2DataSource;
    }
  }
}
