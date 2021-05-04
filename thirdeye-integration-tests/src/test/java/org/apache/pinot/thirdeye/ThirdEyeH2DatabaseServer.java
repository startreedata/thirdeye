package org.apache.pinot.thirdeye;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.h2.tools.RunScript.execute;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration;
import org.h2.tools.Server;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeH2DatabaseServer {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyeH2DatabaseServer.class);
  private String dbName = "thirdeye";
  private String dbHost = "localhost";
  private Integer dbPort = 9124;
  private DatabaseConfiguration dbConfig;
  public static final String BASE_DIR = "../";
  public static final String CREATE_SCHEMA_SQL =
      BASE_DIR + "thirdeye-core/src/main/resources/schema/create-schema.sql";
  private final Server server;
  private Connection conn;

  public ThirdEyeH2DatabaseServer(String host, Integer port, String dbName){
    if(host != null) {
      this.dbHost = host;
    }
    if(port != null) {
      this.dbPort = port;
    }
    if(dbName != null) {
      this.dbName = dbName;
    }
    try {
      dbConfig= new DatabaseConfiguration()
              .setUrl(String.format("jdbc:h2:tcp:%s:%s/mem:%s;DB_CLOSE_DELAY=-1", dbHost, dbPort, dbName))
              .setUser("user")
              .setPassword("password")
              .setDriver("org.h2.Driver");
      this.server = Server.createTcpServer("-baseDir", BASE_DIR, "-tcpPort", Integer.toString(dbPort));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    conn = null;
  }

  public static JSONArray toJsonArray(ResultSet rs) throws SQLException {
    JSONArray json = new JSONArray();
    ResultSetMetaData rsmd = rs.getMetaData();
    while (rs.next()) {
      int numColumns = rsmd.getColumnCount();
      JSONObject obj = new JSONObject();
      for (int i = 1; i <= numColumns; i++) {
        String column_name = rsmd.getColumnName(i);
        obj.put(column_name, rs.getObject(column_name));
      }
      json.put(obj);
    }
    return json;
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
  public JSONArray executeSql(String query) {
    try {
      return toJsonArray(execute(requireNonNull(conn), new StringReader(query)));
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

  public DatabaseConfiguration getDbConfig() {
    return dbConfig;
  }
}
