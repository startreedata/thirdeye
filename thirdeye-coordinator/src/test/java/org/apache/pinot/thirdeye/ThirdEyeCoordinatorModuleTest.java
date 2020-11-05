package org.apache.pinot.thirdeye;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.pinot.thirdeye.auth.AuthConfiguration;
import org.apache.pinot.thirdeye.auth.JwtConfiguration;
import org.apache.pinot.thirdeye.common.ThirdEyeConfiguration;
import org.apache.pinot.thirdeye.datalayer.TestDatabase;
import org.apache.pinot.thirdeye.datalayer.util.DaoProviderUtil;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.resources.RootResource;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.testng.annotations.Test;

public class ThirdEyeCoordinatorModuleTest {

  @Test
  public void testRootResourceInjection() throws Exception {
    TestDatabase db = new TestDatabase();
    final DataSource dataSource = db.createDataSource(db.testPersistenceConfig());

    final ThirdEyeCoordinatorConfiguration configuration = new ThirdEyeCoordinatorConfiguration()
        .setAuthConfiguration(new AuthConfiguration()
            .setJwtConfiguration(new JwtConfiguration()
                .setSigningKey("abcd")
                .setIssuer("issuer")));

    final Injector injector = Guice.createInjector(new ThirdEyeCoordinatorModule(
        configuration,
        dataSource
    ));
    DaoProviderUtil.setInjector(injector);

    injector
        .getInstance(ThirdEyeCacheRegistry.class)
        .initializeCaches(new ThirdEyeConfiguration().setRootDir("../config"));

    assertThat(injector.getInstance(RootResource.class)).isNotNull();
  }
}
