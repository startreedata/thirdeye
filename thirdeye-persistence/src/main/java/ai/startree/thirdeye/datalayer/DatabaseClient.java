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

import static ai.startree.thirdeye.datalayer.DataSourceBuilder.migrateDatabase;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import io.micrometer.core.instrument.Metrics;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DatabaseClient {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseClient.class);

  private final DataSource dataSource;
  @Deprecated
  private final Counter dbExceptionCounter;
  @Deprecated
  private final Counter dbCallCounter;
  private final io.micrometer.core.instrument.Counter dbTransactionCounterOfSuccess;
  private final io.micrometer.core.instrument.Counter dbTransactionCounterOfException;
  private final AdministratorClient admin;

  @Inject
  public DatabaseClient(final DataSource dataSource, final MetricRegistry metricRegistry) {
    this.dataSource = dataSource;
    // should be disabled in a production environment
    this.admin = new AdministratorClient();

    // deprecated - use thirdeye_persistence_transaction_total
    dbExceptionCounter = metricRegistry.counter("dbExceptionCounter");
    // deprecated - use thirdeye_persistence_transaction_total
    dbCallCounter = metricRegistry.counter("dbCallCounter");
    this.dbTransactionCounterOfSuccess = io.micrometer.core.instrument.Counter.builder(
            "thirdeye_persistence_transaction_total")
        .tag("exception", "false")
        .register(Metrics.globalRegistry);
    this.dbTransactionCounterOfException = io.micrometer.core.instrument.Counter.builder(
            "thirdeye_persistence_transaction_total")
        .tag("exception", "true")
        .register(Metrics.globalRegistry);
  }

  public <T> T executeTransaction(final DBOperation<T> operation, final T defaultReturn)
      throws SQLException {
    dbCallCounter.inc();
    try (Connection connection = dataSource.getConnection()) {
      try {
        connection.setAutoCommit(false);
        final T t = operation.handle(connection);
        connection.commit();
        dbTransactionCounterOfSuccess.increment();
        return t;
      } catch (final Exception e) {
        LOG.error("Exception while executing query task", e);
        dbTransactionCounterOfException.increment();
        dbExceptionCounter.inc();
        // Rollback transaction in case json table is updated but index table isn't due to any errors (duplicate key, etc.)
        if (connection != null) {
          try {
            connection.rollback();
          } catch (final SQLException e1) {
            LOG.error("Failed to rollback SQL execution", e);
          }
        }
        return defaultReturn;
      }
    }
  }

  public boolean validate() {
    try (final ResultSet resultSet = executeQuery("SELECT 1")) {
      return resultSet.next();
    } catch (SQLException | RuntimeException e) {
      LOG.error("Exception while performing database validation.", e);
    }
    return false;
  }

  private ResultSet executeQuery(final String sql) {
    try (Connection connection = dataSource.getConnection()) {
      final Statement statement = connection.createStatement();
      return statement.executeQuery(sql);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public AdministratorClient admin() {
    return admin;
  }

  public interface DBOperation<T> {

    T handle(Connection connection) throws Exception;
  }

  public class AdministratorClient {

    public List<String> getTables() throws SQLException {
      try (Connection connection = dataSource.getConnection()) {
        DatabaseMetaData md = connection.getMetaData();
        ResultSet rs = md.getTables(null, null, "%", null);

        List<String> tables = new ArrayList<>();
        while (rs.next()) {
          tables.add(rs.getString(3));
        }
        return tables;
      }
    }

    public void truncateTables() throws SQLException {
      runOnAllTables("TRUNCATE TABLE");
    }

    public void dropTables() throws SQLException {
      runOnAllTables("DROP TABLE");
    }

    private void runOnAllTables(final String command) throws SQLException {
      try (Connection connection = dataSource.getConnection()) {
        String databaseName = connection.getCatalog();
        for (String table : getTables()) {
          connection.createStatement()
              .executeUpdate(String.format("%s %s.%s", command, databaseName, table));
        }
      }
    }

    public void createAllTables() throws SQLException {
      migrateDatabase(dataSource);
    }
  }
}
