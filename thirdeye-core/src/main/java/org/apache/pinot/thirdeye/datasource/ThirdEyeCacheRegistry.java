/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.datasource;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.common.ThirdEyeConfiguration;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.datasource.cache.DatasetConfigCacheLoader;
import org.apache.pinot.thirdeye.datasource.cache.DatasetListCache;
import org.apache.pinot.thirdeye.datasource.cache.DatasetMaxDataTimeCacheLoader;
import org.apache.pinot.thirdeye.datasource.cache.DimensionFiltersCacheLoader;
import org.apache.pinot.thirdeye.datasource.cache.MetricConfigCacheLoader;
import org.apache.pinot.thirdeye.datasource.cache.MetricDataset;
import org.apache.pinot.thirdeye.datasource.cache.QueryCache;
import org.apache.pinot.thirdeye.detection.cache.CacheConfig;
import org.apache.pinot.thirdeye.detection.cache.CacheConfigLoader;
import org.apache.pinot.thirdeye.detection.cache.CacheDAO;
import org.apache.pinot.thirdeye.detection.cache.CentralizedCacheConfig;
import org.apache.pinot.thirdeye.detection.cache.DefaultTimeSeriesCache;
import org.apache.pinot.thirdeye.detection.cache.TimeSeriesCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ThirdEyeCacheRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeCacheRegistry.class);
  private static ThirdEyeCacheRegistry instance;

  // DAO to ThirdEye's data and meta-data storage.
  private final MetricConfigManager metricConfigManager;
  private final DatasetConfigManager datasetConfigManager;

  // TODO: Rename QueryCache to a name like DataSourceCache.
  private QueryCache queryCache;
  private TimeSeriesCache timeSeriesCache = null;

  // Meta-data caches
  private LoadingCache<String, DatasetConfigDTO> datasetConfigCache;
  private LoadingCache<MetricDataset, MetricConfigDTO> metricConfigCache;
  private LoadingCache<String, Long> datasetMaxDataTimeCache;
  private LoadingCache<String, String> dimensionFiltersCache;
  private DatasetListCache datasetsCache;

  @Inject
  public ThirdEyeCacheRegistry(
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager) {
    this.metricConfigManager = metricConfigManager;
    this.datasetConfigManager = datasetConfigManager;
  }

  public static synchronized ThirdEyeCacheRegistry getInstance() {
    return requireNonNull(instance, "ThirdEyeCacheRegistry not initialized");
  }

  public static void setInstance(final ThirdEyeCacheRegistry instance) {
    ThirdEyeCacheRegistry.instance = instance;
  }

  /**
   * Use "default" cache settings, meaning
   */
  private void setupDefaultTimeSeriesCacheSettings() {
    CentralizedCacheConfig cfg = new CentralizedCacheConfig();
    cfg.setMaxParallelInserts(1);

    CacheConfig.getInstance().setUseCentralizedCache(false);
    CacheConfig.getInstance().setUseInMemoryCache(false);
    CacheConfig.getInstance().setCentralizedCacheSettings(cfg);
  }

  /**
   * Initializes the adaptor to data sources such as Pinot, MySQL, etc.
   */
  private void initDataSources(final URL dataSourcesUrl) {
    try {
      // Initialize adaptors to time series databases.
      QueryCache queryCache = buildQueryCache(dataSourcesUrl);
      registerQueryCache(queryCache);
    } catch (Exception e) {
      LOG.info("Caught exception while initializing caches", e);
    }
  }

  public QueryCache buildQueryCache(final URL dataSourcesUrl) {
    final DataSourcesLoader loader = new DataSourcesLoader();
    final DataSources dataSources = requireNonNull(
        loader.fromDataSourcesUrl(dataSourcesUrl),
        "Could not create data sources from path " + dataSourcesUrl);

    // Query Cache
    final Map<String, ThirdEyeDataSource> thirdEyeDataSourcesMap = loader
        .getDataSourceMap(dataSources);
    return new QueryCache(thirdEyeDataSourcesMap, Executors.newCachedThreadPool());
  }

  public TimeSeriesCache buildTimeSeriesCache(
      final CacheDAO cacheDAO,
      final int maxParallelInserts) {
    return new DefaultTimeSeriesCache(metricConfigManager,
        datasetConfigManager,
        queryCache,
        cacheDAO,
        Executors.newFixedThreadPool(maxParallelInserts));
  }

  /**
   * Initializes data sources and caches.
   *
   * @param thirdeyeConfig ThirdEye's configurations.
   */
  public void initializeCaches(ThirdEyeConfiguration thirdeyeConfig) {
    initDataSources(thirdeyeConfig.getDataSourcesAsUrl());
    initMetaDataCaches();
    initCentralizedCache(thirdeyeConfig.getCacheConfigAsUrl());
  }

  private void initCentralizedCache(final URL cacheConfigUrl) {
    try {
      CacheConfig cacheConfig = CacheConfigLoader.fromCacheConfigUrl(cacheConfigUrl);
      if (cacheConfig == null) {
        LOG.error("Could not get cache config from path {} - reverting to default settings",
            cacheConfigUrl);
        setupDefaultTimeSeriesCacheSettings();
      }

      CacheDAO cacheDAO = null;
      if (cacheConfig.useCentralizedCache()) {
        cacheDAO = CacheConfigLoader.loadCacheDAO(cacheConfig);
      }

      if (instance.getTimeSeriesCache() == null) {
        TimeSeriesCache timeSeriesCache = buildTimeSeriesCache(cacheDAO,
            CacheConfig.getInstance().getCentralizedCacheSettings().getMaxParallelInserts());

        registerTimeSeriesCache(timeSeriesCache);
      }
    } catch (Exception e) {
      LOG.error(
          "Caught exception while initializing centralized cache - reverting to default settings",
          e);
      setupDefaultTimeSeriesCacheSettings();
    }
  }

  /**
   * Initialize the cache for meta data. This method has to be invoked after data sources are
   * connected.
   */
  public void initMetaDataCaches() {
    Preconditions.checkNotNull(queryCache,
        "Data sources are not initialized. Please invoke initDataSources() before this method.");

    // DatasetConfig cache
    // TODO deprecate. read from database directly
    LoadingCache<String, DatasetConfigDTO> datasetConfigCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build(new DatasetConfigCacheLoader(datasetConfigManager));
    registerDatasetConfigCache(datasetConfigCache);

    // MetricConfig cache
    // TODO deprecate. read from database directly
    LoadingCache<MetricDataset, MetricConfigDTO> metricConfigCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build(new MetricConfigCacheLoader(metricConfigManager));
    registerMetricConfigCache(metricConfigCache);

    // DatasetMaxDataTime Cache
    LoadingCache<String, Long> datasetMaxDataTimeCache = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build(new DatasetMaxDataTimeCacheLoader(queryCache, datasetConfigManager));
    registerDatasetMaxDataTimeCache(datasetMaxDataTimeCache);

    // Dimension Filter cache
    LoadingCache<String, String> dimensionFiltersCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build(new DimensionFiltersCacheLoader(queryCache, datasetConfigManager));
    registerDimensionFiltersCache(dimensionFiltersCache);

    // Dataset list
    DatasetListCache datasetListCache = new DatasetListCache(datasetConfigManager,
        TimeUnit.HOURS.toMillis(1));
    registerDatasetsCache(datasetListCache);
  }

  public LoadingCache<String, Long> getDatasetMaxDataTimeCache() {
    return datasetMaxDataTimeCache;
  }

  public void registerDatasetMaxDataTimeCache(LoadingCache<String, Long> datasetMaxDataTimeCache) {
    this.datasetMaxDataTimeCache = datasetMaxDataTimeCache;
  }

  public DatasetListCache getDatasetsCache() {
    return datasetsCache;
  }

  public void registerDatasetsCache(DatasetListCache collectionsCache) {
    this.datasetsCache = collectionsCache;
  }

  public LoadingCache<String, String> getDimensionFiltersCache() {
    return dimensionFiltersCache;
  }

  public void registerDimensionFiltersCache(LoadingCache<String, String> dimensionFiltersCache) {
    this.dimensionFiltersCache = dimensionFiltersCache;
  }

  public QueryCache getQueryCache() {
    return queryCache;
  }

  public void registerQueryCache(QueryCache queryCache) {
    this.queryCache = queryCache;
  }

  public TimeSeriesCache getTimeSeriesCache() {
    return timeSeriesCache;
  }

  public void registerTimeSeriesCache(TimeSeriesCache timeSeriesCache) {
    this.timeSeriesCache = timeSeriesCache;
  }

  public LoadingCache<String, DatasetConfigDTO> getDatasetConfigCache() {
    return datasetConfigCache;
  }

  public void registerDatasetConfigCache(
      LoadingCache<String, DatasetConfigDTO> datasetConfigCache) {
    this.datasetConfigCache = datasetConfigCache;
  }

  public LoadingCache<MetricDataset, MetricConfigDTO> getMetricConfigCache() {
    return metricConfigCache;
  }

  public void registerMetricConfigCache(
      LoadingCache<MetricDataset, MetricConfigDTO> metricConfigCache) {
    this.metricConfigCache = metricConfigCache;
  }
}
