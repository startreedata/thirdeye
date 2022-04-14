/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.loader;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detection.cache.CacheConfig;
import ai.startree.thirdeye.detection.cache.TimeSeriesCache;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.loader.TimeSeriesLoader;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.util.DataFrameUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DefaultTimeSeriesLoader implements TimeSeriesLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultTimeSeriesLoader.class);

  private final DatasetConfigManager datasetDAO;
  private final CacheConfig cacheConfig;
  private final TimeSeriesCache timeSeriesCache;
  private final DataSourceCache dataSourceCache;

  @Inject
  public DefaultTimeSeriesLoader(DatasetConfigManager datasetDAO,
      final CacheConfig cacheConfig,
      final TimeSeriesCache timeSeriesCache,
      final DataSourceCache dataSourceCache) {
    this.datasetDAO = datasetDAO;
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

    ThirdEyeRequest thirdEyeRequest = DataFrameUtils
        .makeTimeSeriesRequestAligned(slice, "ref", this.datasetDAO);
    ThirdEyeResponse response;
    if (cacheConfig.useCentralizedCache()) {
      response = timeSeriesCache.fetchTimeSeries(thirdEyeRequest);
    } else {
      response = dataSourceCache.getQueryResult(thirdEyeRequest);
    }

    return DataFrameUtils.evaluateResponse(response, thirdEyeRequest.getMetricFunctions().get(0));
  }
}
