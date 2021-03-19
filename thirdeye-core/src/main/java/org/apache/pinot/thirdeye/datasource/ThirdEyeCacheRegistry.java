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

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MetricConfigDTO;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.datasource.cache.DatasetConfigCacheLoader;
import org.apache.pinot.thirdeye.datasource.cache.DatasetMaxDataTimeCacheLoader;
import org.apache.pinot.thirdeye.datasource.cache.MetricConfigCacheLoader;
import org.apache.pinot.thirdeye.datasource.cache.MetricDataset;
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

  // DAO to ThirdEye's data and meta-data storage.
  private final MetricConfigManager metricConfigManager;
  private final DatasetConfigManager datasetConfigManager;
  private final DataSourcesLoader dataSourcesLoader;
  private final CacheConfig cacheConfig;

  private DataSourceCache dataSourceCache;
  private TimeSeriesCache timeSeriesCache = null;

  // Meta-data caches
  private LoadingCache<String, DatasetConfigDTO> datasetConfigCache;
  private LoadingCache<MetricDataset, MetricConfigDTO> metricConfigCache;
  private LoadingCache<String, Long> datasetMaxDataTimeCache;

  @Inject
  public ThirdEyeCacheRegistry(
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final DataSourcesLoader dataSourcesLoader,
      final CacheConfig cacheConfig) {
    this.metricConfigManager = metricConfigManager;
    this.datasetConfigManager = datasetConfigManager;
    this.dataSourcesLoader = dataSourcesLoader;
    this.cacheConfig = cacheConfig;
  }

  /**
   * Use "default" cache settings, meaning
   */
  private void setupDefaultTimeSeriesCacheSettings() {
    final CentralizedCacheConfig cfg = new CentralizedCacheConfig();
    cfg.setMaxParallelInserts(1);

    cacheConfig.setUseCentralizedCache(false);
    cacheConfig.setUseInMemoryCache(false);
    cacheConfig.setCentralizedCacheConfig(cfg);
  }

  /**
   * Initializes the adaptor to data sources such as Pinot, MySQL, etc.
   */
  private void initDataSources() {
    // Initialize adaptors to time series databases.
    final DataSourceCache dataSourceCache = new DataSourceCache(dataSourcesLoader.getDataSourceMap());
    setDataSourceCache(dataSourceCache);
  }

  public TimeSeriesCache buildTimeSeriesCache(
      final CacheDAO cacheDAO,
      final int maxParallelInserts) {
    return new DefaultTimeSeriesCache(metricConfigManager,
        datasetConfigManager,
        dataSourceCache,
        cacheDAO,
        Executors.newFixedThreadPool(maxParallelInserts),
        this);
  }

  /**
   * Initializes data sources and caches.
   *
   */
  public void initializeCaches() {
    initDataSources();
    initMetaDataCaches();
    initCentralizedCache();
  }

  private void initCentralizedCache() {
    try {
      CacheDAO cacheDAO = null;
      if (cacheConfig.useCentralizedCache()) {
        cacheDAO = CacheConfigLoader.loadCacheDAO(cacheConfig);
      }

      if (getTimeSeriesCache() == null) {
        TimeSeriesCache timeSeriesCache = buildTimeSeriesCache(cacheDAO,
            cacheConfig.getCentralizedCacheConfig().getMaxParallelInserts());

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
    Preconditions.checkNotNull(dataSourceCache,
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
        .build(new DatasetMaxDataTimeCacheLoader(dataSourceCache, datasetConfigManager));
    registerDatasetMaxDataTimeCache(datasetMaxDataTimeCache);
  }

  public LoadingCache<String, Long> getDatasetMaxDataTimeCache() {
    return datasetMaxDataTimeCache;
  }

  public void registerDatasetMaxDataTimeCache(LoadingCache<String, Long> datasetMaxDataTimeCache) {
    this.datasetMaxDataTimeCache = datasetMaxDataTimeCache;
  }

  public DataSourceCache getDataSourceCache() {
    return dataSourceCache;
  }

  public void setDataSourceCache(DataSourceCache dataSourceCache) {
    this.dataSourceCache = dataSourceCache;
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
