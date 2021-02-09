/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pinot.thirdeye.datalayer.bao;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.io.output.NullWriter;
import org.apache.pinot.thirdeye.datalayer.ScriptRunner;
import org.apache.pinot.thirdeye.datalayer.ThirdEyePersistenceModule;
import org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration;
import org.apache.pinot.thirdeye.util.DeprecatedInjectorUtil;
import org.apache.tomcat.jdbc.pool.DataSource;

public class DAOTestBase {

  private static int counter;

  private DAOTestBase() {
    init();
  }

  public static DAOTestBase getInstance() {
    return new DAOTestBase();
  }

  protected void init() {
    final DatabaseConfiguration dbConfig = new DatabaseConfiguration()
        .setUrl(String.format("jdbc:h2:mem:testdb%d;DB_CLOSE_DELAY=-1", counter++))
        .setUser("ignoreUser")
        .setPassword("ignorePassword")
        .setDriver("org.h2.Driver");
    final DataSource dataSource = createDataSource(dbConfig);
    try {
      setupSchema(dataSource);
    } catch (SQLException  | IOException e) {
      throw new RuntimeException(e);
    }
    final Injector injector = Guice.createInjector(new ThirdEyePersistenceModule(dataSource));
    DeprecatedInjectorUtil.setInjector(injector);
  }

  public void cleanup() {
    // using in memory DB. Nothing to cleanup here.
  }

  private DataSource createDataSource(final DatabaseConfiguration dbConfig) {
    final DataSource dataSource = new DataSource();
    dataSource.setUrl(dbConfig.getUrl());
    System.out.println("Creating db with connection url : " + dataSource.getUrl());
    dataSource.setPassword(dbConfig.getPassword());
    dataSource.setUsername(dbConfig.getUser());
    dataSource.setDriverClassName(dbConfig.getProperties()
        .get("hibernate.connection.driver_class"));

    // pool size configurations
    dataSource.setMaxActive(200);
    dataSource.setMinIdle(10);
    dataSource.setInitialSize(10);

    // when returning connection to pool
    dataSource.setTestOnReturn(true);
    dataSource.setRollbackOnReturn(true);

    // Timeout before an abandoned(in use) connection can be removed.
    dataSource.setRemoveAbandonedTimeout(600_000);
    dataSource.setRemoveAbandoned(true);
    return dataSource;
  }

  private void setupSchema(final DataSource ds) throws SQLException, IOException {
    final Connection conn = ds.getConnection();

    // create schema
    final URL createSchemaUrl = getClass().getResource("/schema/create-schema.sql");
    final ScriptRunner scriptRunner = new ScriptRunner(conn, true);
    scriptRunner.setDelimiter(";");
    scriptRunner.setLogWriter(new PrintWriter(new NullWriter()));
    scriptRunner.runScript(new FileReader(createSchemaUrl.getFile()));
  }
}
