package org.apache.pinot.thirdeye.worker;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.pinot.thirdeye.ThirdEyeCoreModule;
import org.apache.pinot.thirdeye.anomaly.ThirdEyeWorkerConfiguration;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeWorkerModule extends AbstractModule {

  private final DataSource dataSource;
  private final ThirdEyeWorkerConfiguration configuration;

  public ThirdEyeWorkerModule(final DataSource dataSource,
      final ThirdEyeWorkerConfiguration configuration) {
    this.dataSource = dataSource;
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    install(new ThirdEyeCoreModule(dataSource));
  }

  @Singleton
  @Provides
  public ThirdEyeWorkerConfiguration getThirdEyeWorkerConfiguration() {
    return configuration;
  }
}
