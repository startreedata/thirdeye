/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.cache.builder;

import ai.startree.thirdeye.datasource.loader.DefaultTimeSeriesLoader;
import ai.startree.thirdeye.detection.cache.CacheConfig;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A fetcher for fetching time-series from cache/datasource.
 * The cache holds time-series information per Metric Slices
 */
@Singleton
public class TimeSeriesCacheBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesCacheBuilder.class);

  private static final long TIMEOUT = 60000;

  private final CacheConfig cacheConfig;
  private final ExecutorService executor;
  private final LoadingCache<MetricSlice, DataFrame> cache;
  private final DefaultTimeSeriesLoader timeseriesLoader;

  @Inject
  public TimeSeriesCacheBuilder(
      final CacheConfig cacheConfig,
      final DefaultTimeSeriesLoader timeseriesLoader) {
    this.cacheConfig = cacheConfig;
    this.timeseriesLoader = timeseriesLoader;
    this.cache = initCache();
    executor = Executors.newCachedThreadPool();
  }

  private LoadingCache<MetricSlice, DataFrame> initCache() {
    // don't use more than one third of memory for detection time series
    long cacheSize = Runtime.getRuntime().freeMemory() / 3;
    LOG.info("initializing detection timeseries cache with {} bytes", cacheSize);
    return CacheBuilder
        .newBuilder()
        .maximumWeight(cacheSize)
        // Estimate that most detection tasks will complete within 15 minutes
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .weigher(
            (Weigher<MetricSlice, DataFrame>) (slice, dataFrame) -> dataFrame.size() * (Long.BYTES
                + Double.BYTES))
        .build(new CacheLoader<MetricSlice, DataFrame>() {
          // load single slice
          @Override
          public DataFrame load(MetricSlice slice) {
            return loadTimeseries(Collections.singleton(slice)).get(slice);
          }

          // bulk loading time series slice in parallel
          @Override
          public Map<MetricSlice, DataFrame> loadAll(Iterable<? extends MetricSlice> slices) {
            return loadTimeseries(Lists.newArrayList(slices));
          }
        });
  }

  public Map<MetricSlice, DataFrame> fetchSlices(Collection<MetricSlice> slices)
      throws ExecutionException {
    if (cacheConfig.useInMemoryCache()) {
      return this.cache.getAll(slices);
    } else {
      return loadTimeseries(slices);
    }
  }

  /**
   * Loads time-series data for the given slices. Fetch order:
   * a. Check if the time-series is already available in the cache and return
   * b. If cache-miss, load the information from data source and return
   */
  private Map<MetricSlice, DataFrame> loadTimeseries(Collection<MetricSlice> slices) {
    Map<MetricSlice, DataFrame> output = new HashMap<>();

    try {
      long ts = System.nanoTime();

      // if the time series slice is already in cache, return directly
      if (cacheConfig.useInMemoryCache()) {
        for (MetricSlice slice : slices) {
          for (Map.Entry<MetricSlice, DataFrame> entry : this.cache.asMap().entrySet()) {
            // current slice potentially contained in cache
            if (entry.getKey().containSlice(slice)) {
              DataFrame df = entry.getValue()
                  .filter(entry.getValue().getLongs(DataFrame.COL_TIME)
                      .between(slice.getStartMillis(), slice.getEndMillis()))
                  .dropNull(DataFrame.COL_TIME);
              // double check if it is cache hit
              if (df.getLongs(DataFrame.COL_TIME).size() > 0) {
                output.put(slice, df);
                break;
              }
            }
          }
        }
      }

      // if not in cache, fetch from data source
      Map<MetricSlice, Future<DataFrame>> futures = new HashMap<>();
      for (final MetricSlice slice : slices) {
        if (!output.containsKey(slice)) {
          futures.put(slice, this.executor.submit(() -> timeseriesLoader.load(slice)));
        }
      }
      //LOG.info("Fetching {} slices of timeseries, {} cache hit, {} cache miss", slices.size(), output.size(), futures.size());
      for (MetricSlice slice : slices) {
        if (!output.containsKey(slice)) {
          output.put(slice, futures.get(slice).get(TIMEOUT, TimeUnit.MILLISECONDS));
        }
      }
      LOG.info("Fetching {} slices used {} milliseconds", slices.size(), (System.nanoTime() - ts) / 1000000);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return output;
  }

  public void cleanCache() {
    if (this.cache != null) {
      this.cache.cleanUp();
    }
  }
}
