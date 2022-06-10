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
package ai.startree.thirdeye.datalayer;

import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import java.sql.SQLException;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourceBuilder.class);
  private static final String MYSQL_FLYWAY_PATH = "classpath:db/migration/mysql";
  private static final String H2_FLYWAY_PATH = "classpath:db/migration/h2";
  private static final String FLYWAY_BASELINE_VERSION = "1.40.0";

  public DataSource build(final DatabaseConfiguration dbConfig) {
    final DataSource dataSource = createDataSource(dbConfig);

    try {
      migrateDatabase(dataSource);
    } catch (SQLException e) {
      throw new RuntimeException("Failed creating/migrating database schema", e);
    }

    return dataSource;
  }

  protected static void migrateDatabase(final javax.sql.DataSource dataSource) throws SQLException {
    String flywayLocations = getSqlScriptsLocations(dataSource.getConnection()
        .getMetaData()
        .getURL());

    Flyway flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations(flywayLocations)
        // below is necessary for migration of existing databases.
        // Flyway will not run sql scripts <= FLYWAY_BASELINE_VERSION for existing database without flyway metadata
        // See https://flywaydb.org/documentation/configuration/parameters/baselineOnMigrate
        .baselineOnMigrate(true)
        .baselineVersion(FLYWAY_BASELINE_VERSION).load();
    flyway.migrate();
  }

  /**
   * Returns the flyway locations with the sql scripts corresponding to the database.
   * Supports h2 and MySql.
   */
  private static String getSqlScriptsLocations(final String url) {
    if (url.startsWith("jdbc:h2")) {
      return H2_FLYWAY_PATH;
    }
    return MYSQL_FLYWAY_PATH;
  }

  private DataSource createDataSource(final DatabaseConfiguration dbConfig) {
    final DataSource dataSource = new DataSource();
    dataSource.setInitialSize(10);
    dataSource.setDefaultAutoCommit(false);
    dataSource.setMaxActive(100);
    dataSource.setUsername(dbConfig.getUser());
    dataSource.setPassword(dbConfig.getPassword());
    dataSource.setUrl(dbConfig.getUrl());
    dataSource.setDriverClassName(dbConfig.getDriver());

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
}
