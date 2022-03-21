package org.apache.pinot.thirdeye.datalayer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDatabase {

  private static final Logger log = LoggerFactory.getLogger(TestDatabase.class);
  private static int counter = 0;

  public void cleanup() {
    /* tmp file gets deleted automatically */
  }

  public DatabaseConfiguration testDatabaseConfiguration() {
    return new DatabaseConfiguration()
        .setUrl(String.format("jdbc:h2:mem:testdb%d;DB_CLOSE_DELAY=-1", counter++))
        .setUser("ignoreUser")
        .setPassword("ignorePassword")
        .setDriver("org.h2.Driver");
  }

  public DataSource createDataSource(final DatabaseConfiguration dbConfig) throws Exception {

    final DataSource ds = buildDataSource(dbConfig);

    // Create tables
    new DatabaseAdministrator(ds).createAllTables();

    return ds;
  }

  private DataSource buildDataSource(final DatabaseConfiguration dbConfig) {
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

    return ds;
  }

  public Injector createInjector() {
    try {
      final DatabaseConfiguration configuration = testDatabaseConfiguration();
      final DataSource dataSource = createDataSource(configuration);

      return Guice.createInjector(new ThirdEyePersistenceModule(dataSource));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
