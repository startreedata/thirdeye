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

import ai.startree.thirdeye.config.CacheConfig;
import ai.startree.thirdeye.config.TimeConfiguration;
import ai.startree.thirdeye.config.UiConfiguration;
import ai.startree.thirdeye.datalayer.ThirdEyePersistenceModule;
import ai.startree.thirdeye.datasource.loader.DefaultAggregationLoader;
import ai.startree.thirdeye.rootcause.configuration.RcaConfiguration;
import ai.startree.thirdeye.spi.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.spi.detection.DataProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeCoreModule extends AbstractModule {

  private final DataSource dataSource;
  private final CacheConfig cacheConfig;
  private final RcaConfiguration rcaConfiguration;
  private final UiConfiguration uiConfiguration;
  private final TimeConfiguration timeConfiguration;

  public ThirdEyeCoreModule(final DataSource dataSource,
      final CacheConfig cacheConfig,
      final RcaConfiguration rcaConfiguration,
      final UiConfiguration uiConfiguration,
      final TimeConfiguration timeConfiguration) {
    this.dataSource = dataSource;

    this.cacheConfig = cacheConfig;
    this.rcaConfiguration = rcaConfiguration;
    this.uiConfiguration = uiConfiguration;
    this.timeConfiguration = timeConfiguration;
  }

  @Override
  protected void configure() {
    install(new ThirdEyePersistenceModule(dataSource));

    bind(DataProvider.class).to(DefaultDataProvider.class).in(Scopes.SINGLETON);
    bind(AggregationLoader.class).to(DefaultAggregationLoader.class).in(Scopes.SINGLETON);

    bind(CacheConfig.class).toInstance(cacheConfig);
    bind(RcaConfiguration.class).toInstance(rcaConfiguration);
    bind(UiConfiguration.class).toInstance(uiConfiguration);
    bind(TimeConfiguration.class).toInstance(timeConfiguration);
  }
}
