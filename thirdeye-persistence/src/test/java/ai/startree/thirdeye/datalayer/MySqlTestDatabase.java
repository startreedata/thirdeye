/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.datalayer;

import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;

public class MySqlTestDatabase {

  private static final Logger log = LoggerFactory.getLogger(MySqlTestDatabase.class);
  private static final String SYS_PROP_LOCAL_MYSQL_INSTANCE = "thirdeye.test.useLocalMysqlInstance";
  private static final AtomicInteger counter = new AtomicInteger(0);
  private static final String MYSQL_DOCKER_IMAGE = "mysql:8.0";

  private static final String USERNAME = "root";
  private static final String PASSWORD = "test";
  private static String jdbcUrl = null;
  private static String defaultDatabaseName = null;

  private static MySQLContainer<?> persistenceDbContainer = null;
  private static DatabaseConfiguration sharedConfiguration = null;

  public static DatabaseConfiguration sharedDatabaseConfiguration() {
    if (useLocalMysqlInstance()) {
      log.warn("Using local mysql instance for testing!");
      return localMysqlDatabaseConfiguration();
    }

    if (sharedConfiguration == null) {
      sharedConfiguration = newDatabaseConfiguration();
    }
    return sharedConfiguration;
  }

  public static boolean useLocalMysqlInstance() {
    final String property = System.getProperty(SYS_PROP_LOCAL_MYSQL_INSTANCE);
    return property != null;
  }

  public static String databaseName(final String jdbcUrl) {
    final String regex = ".*/([^/?]+)(\\?.*)?";
    final Matcher matcher = Pattern.compile(regex).matcher(jdbcUrl);
    if (matcher.find()) {
      // Return the captured group, which is the database name
      return matcher.group(1);
    }
    return null;
  }

  public static void cleanSharedDatabase() {
    DatabaseConfiguration dbConfig = null;
    if (useLocalMysqlInstance()) {
      dbConfig = localMysqlDatabaseConfiguration();
    } else if (sharedConfiguration != null) {
      dbConfig = sharedConfiguration;
    }

    if (dbConfig == null) {
      return;
    }
    try {
      final Connection connection = DriverManager.getConnection(dbConfig.getUrl(),
          dbConfig.getUser(),
          dbConfig.getPassword());
      final DatabaseMetaData metaData = connection.getMetaData();

      final String dbName = databaseName(dbConfig.getUrl());
      final ResultSet rs = metaData.getTables(dbName, null, "%", null);
      while (rs.next()) {
        final String tableName = rs.getString(3);
        if (tableName.equals("flyway_schema_history")) {
          continue;
        }
        connection.createStatement().execute("DELETE FROM " + tableName + ";");
      }
      connection.close();
    } catch (final SQLException e) {
      log.error("Failed to clean shared database", e);
    }
  }

  public static synchronized DatabaseConfiguration newDatabaseConfiguration() {
    if (persistenceDbContainer == null) {
      // init docker container
      persistenceDbContainer = new MySQLContainer<>(MYSQL_DOCKER_IMAGE).withPassword(PASSWORD);
      persistenceDbContainer.start();
      jdbcUrl = persistenceDbContainer.getJdbcUrl();
      final String[] elements = jdbcUrl.split("/");
      defaultDatabaseName = elements[elements.length - 1];
    }

    final String databaseName = defaultDatabaseName + counter.getAndIncrement();
    try {
      final Connection connection = DriverManager.getConnection(persistenceDbContainer.getJdbcUrl(),
          USERNAME,
          PASSWORD);
      connection.createStatement().execute("CREATE DATABASE " + databaseName + ";");
      connection.createStatement().execute("SET GLOBAL max_connections = 300;");
      connection.close();
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }

    return new DatabaseConfiguration()
        .setUrl(jdbcUrl.replace(defaultDatabaseName, databaseName)
            + "?autoReconnect=true&allowPublicKeyRetrieval=true&sslMode=DISABLED")
        .setUser(USERNAME)
        .setPassword(PASSWORD)
        .setDriver(persistenceDbContainer.getDriverClassName());
  }

  private static DatabaseConfiguration localMysqlDatabaseConfiguration() {
    final String host = "localhost";
    final int port = 3306;
    final String dbName = "thirdeye_integration_test";

    return new DatabaseConfiguration()
        .setUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName
            + "?autoReconnect=true&allowPublicKeyRetrieval=true&sslMode=DISABLED")
        .setUser("test_user")
        .setPassword("pass")
        .setDriver("com.mysql.cj.jdbc.Driver");
  }

  public static DataSource newDataSource(final DatabaseConfiguration dbConfig) throws Exception {

    final DataSource ds = buildDataSource(dbConfig);
    // Create tables
    new DatabaseClient(ds).adminCreateAllTables();

    return ds;
  }

  private static DataSource buildDataSource(final DatabaseConfiguration dbConfig) {
    final DataSource dataSource = new DataSource();
    dataSource.setUrl(dbConfig.getUrl());
    log.debug("Creating db with connection url: {}", dataSource.getUrl());
    dataSource.setPassword(dbConfig.getPassword());
    dataSource.setUsername(dbConfig.getUser());
    dataSource.setDriverClassName(dbConfig.getDriver());

    // todo cyril use the same method for this test configuration and the server configuration
    // pool size configurations
    dataSource.setInitialSize(10);
    dataSource.setDefaultAutoCommit(false);
    dataSource.setMaxActive(100);

    dataSource.setValidationQuery("select 1");
    dataSource.setTestWhileIdle(true);
    dataSource.setTestOnBorrow(true);
    // when returning connection to pool
    dataSource.setTestOnReturn(true);
    dataSource.setRollbackOnReturn(true);

    // Timeout before an abandoned(in use) connection can be removed.
    dataSource.setRemoveAbandonedTimeout(600_000);
    dataSource.setRemoveAbandoned(true);

    return dataSource;
  }

  public static Injector sharedInjector() {
    return buildInjector(sharedDatabaseConfiguration());
  }

  public static Injector newInjector() {
    final DatabaseConfiguration configuration = newDatabaseConfiguration();

    return buildInjector(configuration);
  }

  private static Injector buildInjector(final DatabaseConfiguration configuration) {
    try {
      final DataSource dataSource = newDataSource(configuration);
      return Guice.createInjector(new ThirdEyePersistenceModule(dataSource));
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
