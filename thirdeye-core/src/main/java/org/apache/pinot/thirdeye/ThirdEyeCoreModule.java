package org.apache.pinot.thirdeye;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import io.dropwizard.auth.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.pinot.thirdeye.auth.ThirdEyeAuthenticatorDisabled;
import org.apache.pinot.thirdeye.auth.ThirdEyeCredentials;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.ThirdEyePersistenceModule;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.datasource.cache.QueryCache;
import org.apache.pinot.thirdeye.datasource.loader.AggregationLoader;
import org.apache.pinot.thirdeye.datasource.loader.DefaultAggregationLoader;
import org.apache.pinot.thirdeye.datasource.loader.DefaultTimeSeriesLoader;
import org.apache.pinot.thirdeye.datasource.loader.TimeSeriesLoader;
import org.apache.pinot.thirdeye.detection.DataProvider;
import org.apache.pinot.thirdeye.detection.DefaultDataProvider;
import org.apache.pinot.thirdeye.detection.cache.TimeSeriesCache;
import org.apache.pinot.thirdeye.detection.cache.builder.AnomaliesCacheBuilder;
import org.apache.pinot.thirdeye.detection.cache.builder.TimeSeriesCacheBuilder;
import org.apache.tomcat.jdbc.pool.DataSource;

public class ThirdEyeCoreModule extends AbstractModule {

  public static final String DATA_SOURCES_CONFIG_YML = "file:config/data-sources/data-sources-config.yml";
  private final DataSource dataSource;
  private final URL dataSourcesUrl;

  public ThirdEyeCoreModule(final DataSource dataSource) {
    try {
      this.dataSourcesUrl = new URL(DATA_SOURCES_CONFIG_YML);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    this.dataSource = dataSource;
  }

  @Override
  protected void configure() {
    install(new ThirdEyePersistenceModule(dataSource));

    bind(new TypeLiteral<Authenticator<ThirdEyeCredentials, ThirdEyePrincipal>>() {
    })
        .to(ThirdEyeAuthenticatorDisabled.class)
        .in(Scopes.SINGLETON);
    bind(DataProvider.class).to(DefaultDataProvider.class).in(Scopes.SINGLETON);
  }

  @Singleton
  @Provides
  public TimeSeriesLoader getTimeSeriesLoader(final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    return new DefaultTimeSeriesLoader(metricConfigManager, datasetConfigManager,
        ThirdEyeCacheRegistry.getInstance().getQueryCache(),
        ThirdEyeCacheRegistry.getInstance().getTimeSeriesCache());
  }

  @Singleton
  @Provides
  public AnomaliesCacheBuilder getAnomaliesCacheBuilder(
      MergedAnomalyResultManager mergedAnomalyResultManager) {
    return AnomaliesCacheBuilder.getInstance(mergedAnomalyResultManager);
  }

  @Singleton
  @Provides
  public TimeSeriesCache getTimeSeriesCache(
      final QueryCache queryCache,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    return ThirdEyeCacheRegistry.buildTimeSeriesCache(
        null,
        queryCache,
        metricConfigManager,
        datasetConfigManager,
        10);
  }

  @Singleton
  @Provides
  public QueryCache getQueryCache() {
    return ThirdEyeCacheRegistry.buildQueryCache(this.dataSourcesUrl);
  }

  @Singleton
  @Provides
  public TimeSeriesCacheBuilder getTimeSeriesCacheBuilder(
      DefaultTimeSeriesLoader defaultTimeSeriesLoader
  ) {
    return TimeSeriesCacheBuilder.getInstance(defaultTimeSeriesLoader);
  }

  @Singleton
  @Provides
  public AggregationLoader getAggregationLoader(
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager) {
    return
        new DefaultAggregationLoader(metricConfigManager, datasetConfigManager,
            ThirdEyeCacheRegistry.getInstance().getQueryCache(),
            ThirdEyeCacheRegistry.getInstance().getDatasetMaxDataTimeCache());
  }
}
