package org.apache.pinot.thirdeye;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.pinot.thirdeye.datalayer.TestDatabase;
import org.apache.pinot.thirdeye.datalayer.util.PersistenceConfig;
import org.apache.pinot.thirdeye.resources.RootResource;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.testng.annotations.Test;

public class ThirdEyeServerModuleTest {

  @Test
  public void testRootResourceInjection() throws Exception {
    TestDatabase db = new TestDatabase();
    final PersistenceConfig configuration = db.testPersistenceConfig();
    final DataSource dataSource = db.createDataSource(configuration);

    final Injector injector = Guice.createInjector(new ThirdEyeServerModule(
        mock(ThirdEyeServerConfiguration.class),
        dataSource,
        mock(MetricRegistry.class)
    ));
    assertThat(injector.getInstance(RootResource.class)).isNotNull();
  }
}
