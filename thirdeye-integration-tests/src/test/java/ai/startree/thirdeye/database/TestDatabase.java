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
package ai.startree.thirdeye.database;

import ai.startree.thirdeye.datalayer.DatabaseAdministrator;
import ai.startree.thirdeye.datalayer.ThirdEyePersistenceModule;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;

// fixme cyril duplicated from thirdeye-pesistence - share the test resource to only use one
public class TestDatabase {

  private static final Logger log = LoggerFactory.getLogger(TestDatabase.class);
  private static AtomicInteger counter = new AtomicInteger(0);
  private static final String MYSQL_DOCKER_IMAGE = "mysql:8.0";

  public static final String USERNAME = "root";
  public static final String PASSWORD = "test";
  private static final MySQLContainer<?> persistenceDbContainer = new MySQLContainer<>(
      MYSQL_DOCKER_IMAGE).withPassword(PASSWORD);
  private static final String DEFAULT_DATABASE_NAME;

  static {
    persistenceDbContainer.start();
    String jdbUrl = persistenceDbContainer.getJdbcUrl();
    String[] elements = jdbUrl.split("/");
    DEFAULT_DATABASE_NAME = elements[elements.length - 1];
  }

  public DatabaseConfiguration testDatabaseConfiguration() {
    final String databaseName = DEFAULT_DATABASE_NAME + counter.getAndIncrement();
    try {
      final Connection connection = DriverManager.getConnection(persistenceDbContainer.getJdbcUrl(),
          USERNAME,
          PASSWORD);
      connection.createStatement().execute("CREATE DATABASE " + databaseName + ";");
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    return new DatabaseConfiguration()
        .setUrl(persistenceDbContainer.getJdbcUrl().replace(DEFAULT_DATABASE_NAME, databaseName)
            + "?autoReconnect=true&allowPublicKeyRetrieval=true&sslMode=DISABLED")
        .setUser(USERNAME)
        .setPassword(PASSWORD)
        .setDriver(persistenceDbContainer.getDriverClassName());
  }

  public DataSource createDataSource(final DatabaseConfiguration dbConfig) throws Exception {

    final DataSource ds = buildDataSource(dbConfig);

    // Create tables
    new DatabaseAdministrator(ds).createAllTables();

    return ds;
  }

  private DataSource buildDataSource(final DatabaseConfiguration dbConfig) {
    final DataSource ds = new DataSource();
    ds.setUrl(dbConfig.getUrl());
    log.debug("Creating db with connection url : " + ds.getUrl());
    ds.setPassword(dbConfig.getPassword());
    ds.setUsername(dbConfig.getUser());
    ds.setDriverClassName(dbConfig.getProperties().get("hibernate.connection.driver_class"));

    // pool size configurations
    ds.setMaxActive(200);
    ds.setMinIdle(10);
    ds.setInitialSize(10);

    // when returning connection to pool
    ds.setTestOnReturn(true);
    ds.setRollbackOnReturn(true);

    // Timeout before an abandoned(in use) connection can be removed.
    ds.setRemoveAbandonedTimeout(600_000);
    ds.setRemoveAbandoned(true);

    return ds;
  }

  public Injector createInjector() {
    try {
      final DatabaseConfiguration configuration = testDatabaseConfiguration();
      final DataSource dataSource = createDataSource(configuration);

      return Guice.createInjector(new ThirdEyePersistenceModule(dataSource));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
