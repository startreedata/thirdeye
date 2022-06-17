/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.config.ThirdEyeServerConfigurationModule;
import ai.startree.thirdeye.datalayer.ThirdEyePersistenceModule;
import ai.startree.thirdeye.datasource.loader.DefaultAggregationLoader;
import ai.startree.thirdeye.detection.DefaultDataProvider;
import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.detection.DataProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeCoreModule extends AbstractModule {

  private final DataSource dataSource;
  private final ThirdEyeServerConfiguration configuration;

  public ThirdEyeCoreModule(final DataSource dataSource,
      final ThirdEyeServerConfiguration configuration) {
    this.dataSource = dataSource;
    this.configuration = configuration;
  }

  @Override
  protected void configure() {
    install(new ThirdEyePersistenceModule(dataSource));
    install(new ThirdEyeServerConfigurationModule(configuration));

    bind(DataProvider.class).to(DefaultDataProvider.class).in(Scopes.SINGLETON);
    bind(AggregationLoader.class).to(DefaultAggregationLoader.class).in(Scopes.SINGLETON);
  }
}
