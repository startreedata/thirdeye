/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.datasource.cache.DatasetConfigCacheLoader;
import ai.startree.thirdeye.datasource.cache.DatasetMaxDataTimeCacheLoader;
import ai.startree.thirdeye.datasource.cache.MetricConfigCacheLoader;
import ai.startree.thirdeye.datasource.cache.MetricDataset;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class ThirdEyeCacheRegistry {

  private final MetricConfigManager metricConfigManager;
  private final DatasetConfigManager datasetConfigManager;
  private final DataSourceCache dataSourceCache;

  // Meta-data caches
  private LoadingCache<String, DatasetConfigDTO> datasetConfigCache;
  private LoadingCache<MetricDataset, MetricConfigDTO> metricConfigCache;
  private LoadingCache<String, Long> datasetMaxDataTimeCache;

  @Inject
  public ThirdEyeCacheRegistry(
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager,
      final DataSourceCache dataSourceCache) {
    this.metricConfigManager = metricConfigManager;
    this.datasetConfigManager = datasetConfigManager;
    this.dataSourceCache = dataSourceCache;
  }

  /**
   * Initializes data sources and caches.
   *
   */
  public void initializeCaches() {
    initMetaDataCaches();
  }

  /**
   * Initialize the cache for meta data. This method has to be invoked after data sources are
   * connected.
   */
  private void initMetaDataCaches() {
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
