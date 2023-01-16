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
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;

public class MySqlTestDatabase {

  private static final Logger log = LoggerFactory.getLogger(MySqlTestDatabase.class);
  private static final AtomicInteger counter = new AtomicInteger(0);
  private static final String MYSQL_DOCKER_IMAGE = "mysql:8.0";

  public static final String USERNAME = "root";
  public static final String PASSWORD = "test";
  public static String jdbcUrl = null;
  private static String defaultDatabaseName = null;

  private static MySQLContainer<?> persistenceDbContainer = null;
  private static DatabaseConfiguration sharedConfiguration = null;

  public static DatabaseConfiguration sharedDatabaseConfiguration() {
    if (sharedConfiguration == null) {
      sharedConfiguration = newDatabaseConfiguration();
    }
    return sharedConfiguration;
  }

  public static void cleanSharedDatabase() {
    if (sharedConfiguration == null) {
      return;
    }
    try {
      final Connection connection = DriverManager.getConnection(sharedConfiguration.getUrl(),
          sharedConfiguration.getUser(),
          sharedConfiguration.getPassword());
      final DatabaseMetaData metaData = connection.getMetaData();
      ResultSet rs = metaData.getTables("test0", null, "%", null);
      while (rs.next()) {
        final String tableName = rs.getString(3);
        if (tableName.equals("flyway_schema_history")) {
          continue;
        }
        connection.createStatement().execute("DELETE FROM " + tableName + ";");
      }
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static DatabaseConfiguration newDatabaseConfiguration() {
    if (persistenceDbContainer == null) {
      // init docker container
      persistenceDbContainer = new MySQLContainer<>(MYSQL_DOCKER_IMAGE).withPassword(PASSWORD);
      persistenceDbContainer.start();
      jdbcUrl = persistenceDbContainer.getJdbcUrl();
      String[] elements = jdbcUrl.split("/");
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
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return new DatabaseConfiguration()
        .setUrl(jdbcUrl.replace(defaultDatabaseName, databaseName)
            + "?autoReconnect=true&allowPublicKeyRetrieval=true&sslMode=DISABLED")
        .setUser(USERNAME)
        .setPassword(PASSWORD)
        .setDriver(persistenceDbContainer.getDriverClassName());
  }

  public static DataSource newDataSource(final DatabaseConfiguration dbConfig) throws Exception {

    final DataSource ds = buildDataSource(dbConfig);

    // Create tables
    new DatabaseAdministrator(ds).createAllTables();

    return ds;
  }

  private static DataSource buildDataSource(final DatabaseConfiguration dbConfig) {
    final DataSource dataSource = new DataSource();
    dataSource.setUrl(dbConfig.getUrl());
    log.debug("Creating db with connection url : " + dataSource.getUrl());
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
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
