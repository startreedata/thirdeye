/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.cache;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.rootcause.entity.MetricEntity;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.datasource.RelationalThirdEyeResponse;
import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import ai.startree.thirdeye.spi.metric.MetricSlice;
import ai.startree.thirdeye.spi.util.SpiUtils;
import ai.startree.thirdeye.util.DataFrameUtils;
import ai.startree.thirdeye.util.ThirdEyeUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of TimeSeriesCache, which is used to attempt fetching
 * data from centralized cache as an alternative to directly fetching from the
 * data source each time.
 */
@Singleton
public class DefaultTimeSeriesCache implements TimeSeriesCache {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultTimeSeriesCache.class);

  private final DatasetConfigManager datasetDAO;
  private final DataSourceCache dataSourceCache;
  private final CacheDAO cacheDAO;
  private final ExecutorService executor;
  private final CacheConfig cacheConfig;

  @Inject
  public DefaultTimeSeriesCache(final DatasetConfigManager datasetDAO,
      @Nullable final CacheDAO cacheDAO,
      final CacheConfig cacheConfig,
      final DataSourceCache dataSourceCache) {
    this.cacheConfig = cacheConfig;
    this.datasetDAO = datasetDAO;
    this.dataSourceCache = dataSourceCache;
    this.cacheDAO = cacheDAO;

    int maxParallelInserts = cacheConfig.getCentralizedCacheConfig().getMaxParallelInserts();
    this.executor = Executors.newFixedThreadPool(maxParallelInserts);
  }

  /**
   * Given a ThirdEyeRequest object, builds the CacheRequest object and attempts to fetch data
   * from the cache. If the requested slice of data is not in the cache or is only partially in
   * the cache, will fetch the missing slices and build the complete timeseries.
   *
   * @param thirdEyeRequest ThirdEyeRequest built from aligned metricSlice request.
   * @return ThirdEyeResponse, the full response for the request
   * @throws Exception if fetch from original data source was not successful.
   */
  public ThirdEyeResponse fetchTimeSeries(ThirdEyeRequest thirdEyeRequest) throws Exception {
    if (!cacheConfig.useCentralizedCache()) {
      return this.dataSourceCache.getQueryResult(thirdEyeRequest);
    }

    ThirdEyeCacheResponse cacheResponse = cacheDAO
        .tryFetchExistingTimeSeries(ThirdEyeCacheRequest.from(thirdEyeRequest));

    DateTime sliceStart = thirdEyeRequest.getStartTimeInclusive();
    DateTime sliceEnd = thirdEyeRequest.getEndTimeExclusive();

    if (cacheResponse.isMissingSlice(sliceStart.getMillis(), sliceEnd.getMillis())) {
      fetchMissingSlices(cacheResponse);
    }

    return buildThirdEyeResponseFromCacheResponse(cacheResponse);
  }

  /**
   * Used if cache had partial or no data for the requested time slice. Checks for which
   * slices are missing and fetches them, then adds them to the response.
   *
   * @param cacheResponse cache response object
   * @throws Exception if fetch to data source throws error.
   */
  private void fetchMissingSlices(ThirdEyeCacheResponse cacheResponse) throws Exception {

    ThirdEyeRequest request = cacheResponse.getCacheRequest().getRequest();

    ThirdEyeResponse result;
    MetricSlice slice;

    if (cacheResponse.hasNoRows()) {
      result = this.dataSourceCache.getQueryResult(cacheResponse.getCacheRequest().getRequest());
      insertTimeSeriesIntoCache(result);
      cacheResponse.mergeSliceIntoRows(result);
    } else {

      long metricId = request.getMetricFunction().getMetricId();
      long requestSliceStart = request.getStartTimeInclusive().getMillis();
      long requestSliceEnd = request.getEndTimeExclusive().getMillis();

      if (cacheResponse.isMissingStartSlice(requestSliceStart)) {
        slice = MetricSlice.from(metricId, requestSliceStart, cacheResponse.getFirstTimestamp(),
            request.getFilterSet(),
            request.getGroupByTimeGranularity());
        result = fetchSliceFromSource(slice);
        insertTimeSeriesIntoCache(result);
        cacheResponse.mergeSliceIntoRows(result);
      }

      if (cacheResponse.isMissingEndSlice(requestSliceEnd)) {
        // we add one time granularity to start because the start is inclusive.
        slice = MetricSlice.from(metricId,
            cacheResponse.getLastTimestamp() + request.getGroupByTimeGranularity().toMillis(),
            requestSliceEnd,
            request.getFilterSet(), request.getGroupByTimeGranularity());
        result = fetchSliceFromSource(slice);
        insertTimeSeriesIntoCache(result);
        cacheResponse.mergeSliceIntoRows(result);
      }
    }
  }

  /**
   * Shorthand to call queryCache to fetch data from the data source.
   *
   * @param slice MetricSlice used to build ThirdEyeRequest object for queryCache to load data
   * @return ThirdEyeResponse, the data for the given slice
   * @throws Exception if fetching from data source had an exception somewhere
   */
  private ThirdEyeResponse fetchSliceFromSource(MetricSlice slice) throws Exception {
    ThirdEyeRequest thirdEyeRequest = DataFrameUtils.makeTimeSeriesRequestAligned(slice, "ref");
    return this.dataSourceCache.getQueryResult(thirdEyeRequest);
  }

  /**
   * Parses cache response object and builds a ThirdEyeResponse object from it.
   *
   * @param cacheResponse cache response
   * @return ThirdEyeResponse object
   */
  private ThirdEyeResponse buildThirdEyeResponseFromCacheResponse(
      ThirdEyeCacheResponse cacheResponse) {

    List<String[]> rows = new ArrayList<>();
    ThirdEyeRequest request = cacheResponse.getCacheRequest().getRequest();

    String dataset = request.getMetricFunction().getDataset();
    DatasetConfigDTO datasetDTO = datasetDAO.findByDataset(dataset);
    TimeSpec timeSpec = ThirdEyeUtils.getTimeSpecFromDatasetConfig(datasetDTO);
    DateTimeZone timeZone = DateTimeZone.forID(datasetDTO.getTimezone());

    for (TimeSeriesDataPoint dataPoint : cacheResponse.getTimeSeriesRows()) {
      int timeBucketIndex = SpiUtils.computeBucketIndex(
          request.getGroupByTimeGranularity(),
          request.getStartTimeInclusive(),
          new DateTime(dataPoint.getTimestamp(), timeZone));

      String[] row = new String[2];
      row[0] = String.valueOf(timeBucketIndex);
      row[1] = dataPoint.getDataValue();

      rows.add(row);
    }

    return new RelationalThirdEyeResponse(request, rows, timeSpec);
  }

  /**
   * Takes a ThirdEyeResponse time-series and inserts the data points individually,
   * in parallel. Alternatively, for dimension exploration jobs, updates the
   * corresponding document in the cache with a new value for the current dimension
   * combination if the document already exists in the cache.
   *
   * @param response a object containing the time-series to be inserted
   */
  public void insertTimeSeriesIntoCache(ThirdEyeResponse response) {
    // insert points in parallel
    for (MetricFunction metric : response.getMetricFunctions()) {
      String metricUrn = MetricEntity
          .fromMetric(response.getRequest().getFilterSet().asMap(), metric.getMetricId()).getUrn();
      for (int i = 0; i < response.getNumRowsFor(metric); i++) {
        Map<String, String> row = response.getRow(metric, i);
        TimeSeriesDataPoint dp = new TimeSeriesDataPoint(metricUrn,
            Long.parseLong(row.get(Constants.TIMESTAMP)), metric.getMetricId(),
            row.get(metric.toString()));
        executor.execute(() -> this.cacheDAO.insertTimeSeriesDataPoint(dp));
      }
    }
  }
}
