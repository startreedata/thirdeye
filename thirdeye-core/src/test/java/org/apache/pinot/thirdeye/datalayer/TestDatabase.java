package org.apache.pinot.thirdeye.datalayer;

import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import org.apache.commons.io.output.NullWriter;
import org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration;
import org.apache.pinot.thirdeye.datalayer.util.PersistenceConfig;
import org.apache.pinot.thirdeye.util.DeprecatedInjectorUtil;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDatabase {

  private static final Logger log = LoggerFactory.getLogger(TestDatabase.class);
  private static int counter = 0;

  public void cleanup() {
    /* tmp file gets deleted automatically */
  }

  public PersistenceConfig testPersistenceConfig() {
    final DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration()
        .setUrl(String.format("jdbc:h2:mem:testdb%d;DB_CLOSE_DELAY=-1", counter++))
        .setUser("ignoreUser")
        .setPassword("ignorePassword")
        .setDriver("org.h2.Driver");

    return new PersistenceConfig().setDatabaseConfiguration(databaseConfiguration);
  }

  public DataSource createDataSource(PersistenceConfig config) throws Exception {
    final DatabaseConfiguration dbConfig = config.getDatabaseConfiguration();

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

    final Connection conn = ds.getConnection();
    // create schema
    final URL createSchemaUrl = getClass().getResource("/schema/create-schema.sql");
    final ScriptRunner scriptRunner = new ScriptRunner(conn, true);
    scriptRunner.setDelimiter(";");
    scriptRunner.setLogWriter(new PrintWriter(new NullWriter()));
    scriptRunner.runScript(new FileReader(createSchemaUrl.getFile()));
    return ds;
  }

  public void init() {
    try {
      final PersistenceConfig configuration = testPersistenceConfig();
      final DataSource dataSource = createDataSource(configuration);

      DeprecatedInjectorUtil.init(dataSource);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
