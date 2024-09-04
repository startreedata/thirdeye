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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DatabaseClient {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseClient.class);

  private final DataSource dataSource;
  private final Counter dbTransactionCounterOfSuccess;
  private final Counter dbTransactionCounterOfException;

  @Inject
  public DatabaseClient(final DataSource dataSource) {
    this.dataSource = dataSource;
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

  // admin methods below should be disabled in prod - as of today they are disabled with the proxy
  public List<String> adminGetTables() throws SQLException {
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

  private void runOnAllTables(final String command) throws SQLException {
    try (Connection connection = dataSource.getConnection()) {
      String databaseName = connection.getCatalog();
      for (String table : adminGetTables()) {
        connection.createStatement()
            .executeUpdate(String.format("%s %s.%s", command, databaseName, table));
      }
    }
  }

  public void adminTruncateTables() throws SQLException {
    runOnAllTables("TRUNCATE TABLE");
  }

  public void adminDropTables() throws SQLException {
    runOnAllTables("DROP TABLE");
  }

  public void adminCreateAllTables() throws SQLException {
    migrateDatabase(dataSource);
  }

  public interface DBOperation<T> {

    T handle(Connection connection) throws Exception;
  }
}
