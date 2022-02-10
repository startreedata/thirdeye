package ai.startree.thirdeye.datalayer;

import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(DataSourceBuilder.class);
  private static final String DEFAULT_DATABASE_PATH = "jdbc:h2:./config/h2db";
  private static final String DEFAULT_DATABASE_FILE = "./config/h2db.mv.db";

  public DataSource build(final DatabaseConfiguration dbConfig) {
    final DataSource dataSource = createDataSource(dbConfig);

    // create schema for default database
    createSchemaIfReqd(dataSource, dbConfig);
    return dataSource;
  }

  private void createSchemaIfReqd(
      final DataSource dataSource,
      final DatabaseConfiguration dbConfig) {
    if (dbConfig.getUrl().equals(DEFAULT_DATABASE_PATH)
        && !new File(DEFAULT_DATABASE_FILE).exists()) {
      try {
        LOG.info("Creating database schema for default URL '{}'", DEFAULT_DATABASE_PATH);
        Connection conn = dataSource.getConnection();
        final ScriptRunner scriptRunner = new ScriptRunner(conn, false);
        scriptRunner.setDelimiter(";");

        InputStream createSchema = this.getClass()
            .getResourceAsStream("/db/create-schema.sql");
        scriptRunner.runScript(new InputStreamReader(createSchema));
      } catch (Exception e) {
        LOG.error("Could not create database schema. Attempting to use existing.", e);
      }
    } else {
      LOG.info("Using existing database at '{}'", dbConfig.getUrl());
    }
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
