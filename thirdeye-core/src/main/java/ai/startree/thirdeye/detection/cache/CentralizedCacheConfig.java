/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * settings for centralized caches
 */
@Deprecated // cache needs reimplementation with v2 query system
public class CentralizedCacheConfig {

  /**
   * time-to-live for documents in the cache before they naturally expire
   */
  private int ttl;

  /**
   * if inserting documents in parallel, the number of threads to spawn for inserting them
   */
  private int maxParallelInserts = 10;

  /**
   * name of the single data store we are choosing to use from the data sources provided
   */
  private String cacheDataStoreName;

  /**
   * Map of data sources available for us to choose from
   */
  private Map<String, CacheDataSource> cacheDataSources = new HashMap<>();

  // left blank
  public CentralizedCacheConfig() {
  }

  public int getTTL() {
    return ttl;
  }

  public int getMaxParallelInserts() {
    return maxParallelInserts;
  }

  public String getCacheDataStoreName() {
    return cacheDataStoreName;
  }

  public Map<String, CacheDataSource> getCacheDataSources() {
    return cacheDataSources;
  }

  /**
   * shorthand to get the config for the single data source specified in the config file
   *
   * @return CacheDataSource with auth info
   */
  public CacheDataSource getDataSourceConfig() {
    return cacheDataSources.get(cacheDataStoreName);
  }

  public void setTTL(int ttl) {
    this.ttl = ttl;
  }

  public void setMaxParallelInserts(int maxParallelInserts) {
    this.maxParallelInserts = maxParallelInserts;
  }

  public void setCacheDataStoreName(String cacheDataStoreName) {
    this.cacheDataStoreName = cacheDataStoreName;
  }

  public void setCacheDataSources(Map<String, CacheDataSource> cacheDataSources) {
    this.cacheDataSources = cacheDataSources;
  }
}
