/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye;

import static ai.startree.thirdeye.DropwizardTestUtils.buildClient;
import static ai.startree.thirdeye.DropwizardTestUtils.buildSupport;
import static ai.startree.thirdeye.datalayer.MySqlTestDatabase.useLocalMysqlInstance;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.datalayer.util.DatabaseConfiguration;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import com.google.inject.Injector;
import io.dropwizard.testing.DropwizardTestSupport;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.ws.rs.client.Client;
import org.apache.pinot.testcontainer.PinotContainer.PinotVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeIntegrationTestSupport {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyeIntegrationTestSupport.class);
  private DropwizardTestSupport<ThirdEyeServerConfiguration> support;
  private ThirdEyeTestClient client;
  private Future<DataSourceApi> pinotDataSourceFuture;
  private DatabaseConfiguration dbConfiguration;
  private final String serverConfigPath;

  public ThirdEyeIntegrationTestSupport(String serverConfigPath) {
    this.serverConfigPath = serverConfigPath;
  }

  public void setup() throws Exception {
    pinotDataSourceFuture = PinotDataSourceManager.getPinotDataSourceApi(PinotVersion.recommendedVersion());
    dbConfiguration = MySqlTestDatabase.sharedDatabaseConfiguration();

    if (useLocalMysqlInstance()) {
      MySqlTestDatabase.cleanSharedDatabase();
    }

    // Setup plugins dir so ThirdEye can load it
    IntegrationTestUtils.setupPluginsDirAbsolutePath();

    support = buildSupport(dbConfiguration, serverConfigPath);
    support.before();

    final Client c = buildClient("test-client", support);
    client = new ThirdEyeTestClient(c, support.getLocalPort());
  }

  public ThirdEyeTestClient getClient() {
    return client;
  }

  public Injector getInjector() {
    return ((ThirdEyeServer) support.getApplication()).getInjector();
  }

  public void tearDown() {
    log.info("Stopping Thirdeye at port: {}", support.getLocalPort());
    support.after();
    MySqlTestDatabase.cleanSharedDatabase();
  }

  public DataSourceApi getPinotDataSourceApi()
      throws ExecutionException, InterruptedException {
    return pinotDataSourceFuture.get();
  }

  public DatabaseConfiguration getDbConfiguration() {
    return dbConfiguration;
  }
}
