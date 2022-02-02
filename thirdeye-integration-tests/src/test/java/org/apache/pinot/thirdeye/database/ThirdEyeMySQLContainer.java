package org.apache.pinot.thirdeye.database;

import static org.apache.pinot.thirdeye.database.ThirdEyeH2DatabaseServer.CREATE_SCHEMA_SQL;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2.store.fs.FileUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

public class ThirdEyeMySQLContainer extends MySQLContainer<ThirdEyeMySQLContainer> {

  public ThirdEyeMySQLContainer(String dockerImageName) {
    super(dockerImageName);
  }

  @Override
  public void start() {
    super.start();
    try {
      addTables();
    } catch (Exception e) {
      throw new RuntimeException(String.format("Could not launch Thirdeye mysql db: %s", e));
    }
  }

  private void addTables() throws IOException, SQLException {
    final InputStream in = FileUtils.newInputStream(CREATE_SCHEMA_SQL);
    String query = IOUtils.toString(in, StandardCharsets.UTF_8);
    performQuery(query);
  }

  protected ResultSet performQuery(String sql) throws SQLException {
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    statement.execute(sql);

    return statement.getResultSet();
  }

  private Connection getConnection() {
    try {
      return DriverManager.getConnection(
          getJdbcUrl() + "?allowMultiQueries=true",
          getUsername(),
          getPassword()
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
