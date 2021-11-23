package org.apache.pinot.thirdeye;

import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.pinot.thirdeye.auth.AuthConfiguration;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.datalayer.TestDatabase;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.resources.RootResource;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.testng.annotations.Test;

public class ThirdEyeServerModuleTest {

  @Test
  public void testRootResourceInjection() throws Exception {
    TestDatabase db = new TestDatabase();
    final DataSource dataSource = db.createDataSource(db.testDatabaseConfiguration());

    final ThirdEyeServerConfiguration configuration = new ThirdEyeServerConfiguration()
        .setAuthConfiguration(new AuthConfiguration())
        .setConfigPath("../config");

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
