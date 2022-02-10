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

package ai.startree.thirdeye.datalayer.bao;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datalayer.DatabaseAdministrator;
import ai.startree.thirdeye.datalayer.ThirdEyePersistenceModule;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import ai.startree.thirdeye.datasource.DAORegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.tomcat.jdbc.pool.DataSource;

public class TestDbEnv {

  private static int counter;
  private static Injector injectorStatic;
  private final Injector injector;

  public TestDbEnv() {
    final DataSource dataSource = createDataSource();
    injector = Guice.createInjector(new ThirdEyePersistenceModule(dataSource));

    // TODO suvodeep legacy code. To be cleaned up.
    injectorStatic = injector;
  }

  /**
   * Legacy. To be deleted
   *
   * @return the singleton instance maintained by the {@link TestDbEnv} class
   */
  @Deprecated
  public static DAORegistry getInstance() {
    return getInstance(DAORegistry.class);
  }

  @Deprecated
  public static <T> T getInstance(Class<T> c) {
    return requireNonNull(injectorStatic, "Injector not initialized").getInstance(c);
  }

  private DataSource createDataSource() {
    final DatabaseConfiguration dbConfig = new DatabaseConfiguration()
        .setUrl(String.format("jdbc:h2:mem:testdb%d;DB_CLOSE_DELAY=-1", counter++))
        .setUser("ignoreUser")
        .setPassword("ignorePassword")
        .setDriver("org.h2.Driver");
    final DataSource dataSource = createDataSource(dbConfig);
    try {
      new DatabaseAdministrator(dataSource).createAllTables();
    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
    return dataSource;
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
    if(dbConfig.getProperties()
        .get("hibernate.connection.driver_class") != null) {
      dataSource.setDriverClassName(dbConfig.getProperties()
          .get("hibernate.connection.driver_class"));
    } else {
      dataSource.setDriverClassName(dbConfig.getDriver());
    }

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

  public Injector getInjector() {
    return injector;
  }
}
