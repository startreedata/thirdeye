/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Config file for cache-related stuff.
 * Mapped from cache-config.yml
 */
public class CacheConfig {

  public static CacheConfig INSTANCE = new CacheConfig();

  @JsonProperty("centralizedCacheSettings")
  private CentralizedCacheConfig centralizedCacheConfig = new CentralizedCacheConfig();

  private boolean useInMemoryCache = true;
  private boolean useCentralizedCache = false;

  /**
   * Deprecated in favor of using an injected instance. This will be removed.
   *
   * @return singleton instance of {@link CacheConfig}
   */
  @Deprecated
  public static CacheConfig getInstance() {
    return INSTANCE;
  }

  public static void setINSTANCE(final CacheConfig INSTANCE) {
    CacheConfig.INSTANCE = INSTANCE;
  }

  public boolean useCentralizedCache() {
    return useCentralizedCache;
  }

  public boolean useInMemoryCache() {
    return useInMemoryCache;
  }

  public CentralizedCacheConfig getCentralizedCacheConfig() {
    return centralizedCacheConfig;
  }

  public CacheConfig setCentralizedCacheConfig(CentralizedCacheConfig centralizedCacheConfig) {
    this.centralizedCacheConfig = centralizedCacheConfig;
    return this;
  }

  public CacheConfig setUseCentralizedCache(boolean useCentralizedCache) {
    this.useCentralizedCache = useCentralizedCache;
    return this;
  }

  public CacheConfig setUseInMemoryCache(boolean useInMemoryCache) {
    this.useInMemoryCache = useInMemoryCache;
    return this;
  }
}
