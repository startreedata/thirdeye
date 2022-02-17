/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer;

import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
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
public class DatabaseAdministrator {

  private static final Logger LOG = LoggerFactory.getLogger(DatabaseAdministrator.class);
  private final DataSource dataSource;

  @Inject
  public DatabaseAdministrator(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public ResultSet executeQuery(String sql) {
    try (Connection connection = dataSource.getConnection()) {
      final Statement statement = connection.createStatement();
      return statement.executeQuery(sql);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

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
        connection
            .createStatement()
            .executeUpdate(String.format("%s %s.%s", command, databaseName, table));
      }
    }
  }

  public void createAllTables() throws SQLException, IOException {
    try (Connection connection = dataSource.getConnection()) {
      // create schema
      final URL createSchemaUrl = requireNonNull(
          getClass().getResource("/db/create-schema.sql"),
          "failed to load createSchemaUrl");

      final ScriptRunner scriptRunner = new ScriptRunner(connection, true);
      scriptRunner.runScript(new FileReader(createSchemaUrl.getFile()));
    }
  }

  public boolean validate() {
    try {
      return executeQuery("SELECT 1").next();
    } catch (SQLException | RuntimeException e) {
      LOG.error("Exception while performing database validation.", e);
    }
    return false;
  }
}
