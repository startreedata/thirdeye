/*
 * Copyright 2022 StarTree Inc
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

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.resources.RootResource;
import ai.startree.thirdeye.task.TaskDriverConfiguration;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.testng.annotations.Test;

public class ThirdEyeServerModuleTest {

  @Test
  public void testRootResourceInjection() throws Exception {
    final TestDatabase db = new TestDatabase();
    final DataSource dataSource = db.createDataSource(db.testDatabaseConfiguration());

    final ThirdEyeServerConfiguration configuration = new ThirdEyeServerConfiguration()
        .setAuthConfiguration(new AuthConfiguration())
        .setTaskDriverConfiguration(new TaskDriverConfiguration().setId(0L))
        ;

    final Injector injector = Guice.createInjector(new ThirdEyeServerModule(
        configuration,
        dataSource,
        new MetricRegistry()));

    injector
        .getInstance(ThirdEyeCacheRegistry.class)
        .initializeCaches();

    assertThat(injector.getInstance(RootResource.class)).isNotNull();
  }
}
