package org.apache.pinot.thirdeye;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.resultSetToMap;
import static org.h2.tools.RunScript.execute;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeH2DatabaseServer {

  public static final String BASE_DIR = "../";
  public static final String CREATE_SCHEMA_SQL =
      BASE_DIR + "thirdeye-persistence/src/main/resources/db/create-schema.sql";
  private static final Logger log = LoggerFactory.getLogger(ThirdEyeH2DatabaseServer.class);

  private final Server server;
  private String dbName = "thirdeye";
  private String dbHost = "localhost";
  private Integer dbPort = 9124;
  private DatabaseConfiguration dbConfig;
  private Connection conn;

  public ThirdEyeH2DatabaseServer(String host, Integer port, String dbName) {
    if (host != null) {
      this.dbHost = host;
    }
    if (port != null) {
      this.dbPort = port;
    }
    if (dbName != null) {
      this.dbName = dbName;
    }
    try {
      dbConfig = new DatabaseConfiguration()
          .setUrl(String.format("jdbc:h2:tcp:%s:%s/mem:%s;DB_CLOSE_DELAY=-1",
              dbHost,
              dbPort,
              dbName))
          .setUser("user")
          .setPassword("password")
          .setDriver("org.h2.Driver");
      this.server = Server.createTcpServer("-baseDir",
          BASE_DIR,
          "-tcpPort",
          Integer.toString(dbPort));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    conn = null;
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

      execute(
          dbConfig.getUrl(),
          dbConfig.getUser(),
          dbConfig.getPassword(),
          CREATE_SCHEMA_SQL,
          StandardCharsets.UTF_8,
          true
      );
      conn = createConnection();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Utility to execute query and get results in json
   *
   * @param query SQL query
   * @return JSONArray
   */
  public List<Map<String, Object>> executeSql(String query) {
    try {
      return resultSetToMap(execute(requireNonNull(conn), new StringReader(query)));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        // ignored
      }
    }
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
