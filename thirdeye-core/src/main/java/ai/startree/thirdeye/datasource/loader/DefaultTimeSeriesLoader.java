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
package ai.startree.thirdeye.datasource.loader;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detection.cache.CacheConfig;
import ai.startree.thirdeye.detection.cache.TimeSeriesCache;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.util.DataFrameUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DefaultTimeSeriesLoader implements TimeSeriesLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultTimeSeriesLoader.class);

  private final CacheConfig cacheConfig;
  private final TimeSeriesCache timeSeriesCache;
  private final DataSourceCache dataSourceCache;

  @Inject
  public DefaultTimeSeriesLoader(final CacheConfig cacheConfig,
      final TimeSeriesCache timeSeriesCache,
      final DataSourceCache dataSourceCache) {
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

    ThirdEyeRequest thirdEyeRequest = DataFrameUtils.makeTimeSeriesRequestAligned(slice, "ref");
    ThirdEyeResponse response;
    if (cacheConfig.useCentralizedCache()) {
      response = timeSeriesCache.fetchTimeSeries(thirdEyeRequest);
    } else {
      response = dataSourceCache.getQueryResult(thirdEyeRequest);
    }

    return DataFrameUtils.evaluateResponse(response);
  }
}
