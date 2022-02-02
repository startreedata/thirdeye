package org.apache.pinot.thirdeye.database;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.util.ResourceUtils.resultSetToMap;
import static org.h2.tools.RunScript.execute;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration;
import org.h2.engine.Constants;
import org.h2.store.fs.FileUtils;
import org.h2.tools.Server;
import org.h2.util.ScriptReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeH2DatabaseServer {

  public static final String BASE_DIR = "../";
  public static final String CREATE_SCHEMA_SQL =
      BASE_DIR + "thirdeye-persistence/src/main/resources/db/create-schema.sql";
  private static final Logger log = LoggerFactory.getLogger(ThirdEyeH2DatabaseServer.class);

  private final Server server;
  private final DatabaseConfiguration dbConfig;

  public ThirdEyeH2DatabaseServer(String dbHost, Integer dbPort, String dbName) {
    try {
      dbConfig = buildDbConfig(dbHost, dbPort, dbName);
      this.server = Server.createTcpServer("-baseDir",
          BASE_DIR,
          "-tcpPort",
          Integer.toString(dbPort),
          "-ifNotExists");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static DatabaseConfiguration buildDbConfig(final String dbHost, final Integer dbPort,
      final String dbName) {
    return new DatabaseConfiguration()
        .setUrl(buildConnectionUrl(dbHost, dbPort, dbName))
        .setUser("user")
        .setPassword("password")
        .setDriver("org.h2.Driver");
  }

  private static String buildConnectionUrl(final String dbHost, final Integer dbPort,
      final String dbName) {
    return String.format("jdbc:h2:tcp:%s:%s/mem:%s;DB_CLOSE_DELAY=-1",
        requireNonNull(dbHost),
        requireNonNull(dbPort),
        requireNonNull(dbName)
    );
  }

  private Connection createConnection() {
    try {
      org.h2.Driver.load();
      return DriverManager.getConnection(
          dbConfig.getUrl(),
          dbConfig.getUser(),
          dbConfig.getPassword()
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void start() {
    log.info(String.format("Working Dir: %s", System.getProperty("user.dir")));
    try {
      server.start();
      checkState(new File(CREATE_SCHEMA_SQL).canRead());
      checkState(server.isRunning(true));

      try (
          final Connection connection = createConnection();
          final InputStream in = FileUtils.newInputStream(CREATE_SCHEMA_SQL);
          final Reader reader = new InputStreamReader(
              new BufferedInputStream(in, Constants.IO_BUFFER_SIZE),
              StandardCharsets.UTF_8)
      ) {
        final ScriptReader r = new ScriptReader(reader);
        String sql;
        while ((sql = r.readStatement()) != null) {
          process(connection, sql);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void process(final Connection connection, final String sql) throws SQLException {
    final String sqlProcessed = sql
        .replaceAll("\\s+", " ")
        .replaceAll("ENGINE\\s*=\\s*InnoDB", "")
        .trim();
    if (sqlProcessed.isEmpty()) {
      return;
    }

    final Statement statement = connection.createStatement();
    statement.executeUpdate(sqlProcessed);
  }

  /**
   * Utility to execute query and get results in json
   *
   * @param query SQL query
   * @return JSONArray
   */
  public List<Map<String, Object>> executeSql(String query) {
    try (final Connection connection = createConnection()) {
      return resultSetToMap(execute(requireNonNull(connection), new StringReader(query)));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  public void truncateAllTables() throws SQLException {
    try (Connection connection = createConnection();
        PreparedStatement setChecks = connection.prepareStatement(
            "SET FOREIGN_KEY_CHECKS = ?");
        PreparedStatement getTables = connection.prepareStatement(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = SCHEMA()")) {
      try (ResultSet tablesRes = getTables.executeQuery()) {
        setChecks.setBoolean(1, false);
        setChecks.executeUpdate();
        while (tablesRes.next()) {
          String table = tablesRes.getString(1);
          try (PreparedStatement truncateTable = connection.prepareStatement(
              "TRUNCATE TABLE " + table + " RESTART IDENTITY")) {
            truncateTable.executeUpdate();
          }
        }
      } finally {
        setChecks.setBoolean(1, true);
        setChecks.executeUpdate();
      }
    }
  }

  public DatabaseConfiguration getDbConfig() {
    return dbConfig;
  }
}
