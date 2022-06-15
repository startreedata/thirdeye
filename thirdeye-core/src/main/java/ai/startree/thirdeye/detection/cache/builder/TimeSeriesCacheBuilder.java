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
