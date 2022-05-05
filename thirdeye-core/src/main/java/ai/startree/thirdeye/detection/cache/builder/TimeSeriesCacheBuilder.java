/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.cache.builder;

import ai.startree.thirdeye.datasource.loader.DefaultTimeSeriesLoader;
import ai.startree.thirdeye.detection.cache.CacheConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A fetcher for fetching time-series from cache/datasource.
 * The cache holds time-series information per Metric Slices
 */
@Singleton
public class TimeSeriesCacheBuilder {

  private static final long TIMEOUT = 60000;

  private final CacheConfig cacheConfig;
  private final ExecutorService executor;
  private final DefaultTimeSeriesLoader timeseriesLoader;

  @Inject
  public TimeSeriesCacheBuilder(
      final CacheConfig cacheConfig,
      final DefaultTimeSeriesLoader timeseriesLoader) {
    this.cacheConfig = cacheConfig;
    this.timeseriesLoader = timeseriesLoader;
    executor = Executors.newCachedThreadPool();
  }

}
