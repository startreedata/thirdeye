package org.apache.pinot.thirdeye.worker;

import com.google.inject.AbstractModule;
import org.apache.pinot.thirdeye.ThirdEyeCoreModule;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeWorkerModule extends AbstractModule {

  private final DataSource dataSource;

  public ThirdEyeWorkerModule(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  protected void configure() {
    install(new ThirdEyeCoreModule(dataSource));
  }
}
