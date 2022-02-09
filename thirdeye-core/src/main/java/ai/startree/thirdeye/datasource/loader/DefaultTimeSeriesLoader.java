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

package ai.startree.thirdeye.datasource.loader;

import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detection.cache.CacheConfig;
import ai.startree.thirdeye.detection.cache.TimeSeriesCache;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.loader.TimeSeriesLoader;
import ai.startree.thirdeye.util.DataFrameUtils;
import ai.startree.thirdeye.util.TimeSeriesRequestContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DefaultTimeSeriesLoader implements TimeSeriesLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultTimeSeriesLoader.class);

  private final MetricConfigManager metricDAO;
  private final DatasetConfigManager datasetDAO;
  private final ThirdEyeCacheRegistry thirdEyeCacheRegistry;
  private final CacheConfig cacheConfig;
  private final TimeSeriesCache timeSeriesCache;
  private final DataSourceCache dataSourceCache;

  @Inject
  public DefaultTimeSeriesLoader(MetricConfigManager metricDAO,
      DatasetConfigManager datasetDAO,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry,
      final CacheConfig cacheConfig,
      final TimeSeriesCache timeSeriesCache,
      final DataSourceCache dataSourceCache) {
    this.metricDAO = metricDAO;
    this.datasetDAO = datasetDAO;
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
    this.cacheConfig = cacheConfig;
    this.timeSeriesCache = timeSeriesCache;
    this.dataSourceCache = dataSourceCache;
  }

  /**
   * Default implementation using metricDAO, datasetDAO, and TimeSeriesCache
   *
   * @param slice metric slice to fetch
   * @return DataFrame with timestamps and metric values
   */
  @Override
  public DataFrame load(MetricSlice slice) throws Exception {
    LOG.info("Loading time series for '{}'", slice);

    TimeSeriesRequestContainer rc = DataFrameUtils
        .makeTimeSeriesRequestAligned(slice, "ref", this.metricDAO, this.datasetDAO,
            thirdEyeCacheRegistry);
    ThirdEyeResponse response;
    if (cacheConfig.useCentralizedCache()) {
      response = timeSeriesCache.fetchTimeSeries(rc.getRequest());
    } else {
      response = dataSourceCache.getQueryResult(rc.getRequest());
    }

    return DataFrameUtils.evaluateResponse(response, rc, thirdEyeCacheRegistry);
  }
}
