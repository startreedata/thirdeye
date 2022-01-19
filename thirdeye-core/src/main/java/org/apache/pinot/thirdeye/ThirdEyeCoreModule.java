package org.apache.pinot.thirdeye;

import static org.apache.pinot.thirdeye.detection.cache.CacheConfigLoader.loadCacheDAO;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import javax.annotation.Nullable;
import org.apache.pinot.thirdeye.auth.OAuthManager;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfigurationModule;
import org.apache.pinot.thirdeye.datalayer.ThirdEyePersistenceModule;
import org.apache.pinot.thirdeye.datasource.loader.DefaultAggregationLoader;
import org.apache.pinot.thirdeye.datasource.loader.DefaultTimeSeriesLoader;
import org.apache.pinot.thirdeye.detection.DefaultDataProvider;
import org.apache.pinot.thirdeye.detection.cache.CacheConfig;
import org.apache.pinot.thirdeye.detection.cache.CacheDAO;
import org.apache.pinot.thirdeye.detection.cache.DefaultTimeSeriesCache;
import org.apache.pinot.thirdeye.detection.cache.TimeSeriesCache;
import org.apache.pinot.thirdeye.spi.datasource.loader.AggregationLoader;
import org.apache.pinot.thirdeye.spi.datasource.loader.TimeSeriesLoader;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
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
  public CacheDAO getCacheDAO(CacheConfig config) throws Exception {
    return config.useCentralizedCache() ? loadCacheDAO(config) : null;
  }
}
