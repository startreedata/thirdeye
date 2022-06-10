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

import static ai.startree.thirdeye.detection.cache.CacheConfigLoader.loadCacheDAO;

import ai.startree.thirdeye.config.ThirdEyeServerConfiguration;
import ai.startree.thirdeye.config.ThirdEyeServerConfigurationModule;
import ai.startree.thirdeye.datalayer.ThirdEyePersistenceModule;
import ai.startree.thirdeye.datasource.loader.AggregationLoader;
import ai.startree.thirdeye.datasource.loader.DefaultAggregationLoader;
import ai.startree.thirdeye.datasource.loader.DefaultTimeSeriesLoader;
import ai.startree.thirdeye.datasource.loader.TimeSeriesLoader;
import ai.startree.thirdeye.detection.DefaultDataProvider;
import ai.startree.thirdeye.detection.cache.CacheConfig;
import ai.startree.thirdeye.detection.cache.CacheDAO;
import ai.startree.thirdeye.detection.cache.DefaultTimeSeriesCache;
import ai.startree.thirdeye.detection.cache.TimeSeriesCache;
import ai.startree.thirdeye.spi.detection.DataProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import javax.annotation.Nullable;
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
    bind(TimeSeriesLoader.class).to(DefaultTimeSeriesLoader.class).in(Scopes.SINGLETON);
    bind(TimeSeriesCache.class).to(DefaultTimeSeriesCache.class).in(Scopes.SINGLETON);
    bind(AggregationLoader.class).to(DefaultAggregationLoader.class).in(Scopes.SINGLETON);
  }

  @Singleton
  @Provides
  @Nullable
  public CacheDAO getCacheDAO(final CacheConfig config) throws Exception {
    return config.useCentralizedCache() ? loadCacheDAO(config) : null;
  }
}
