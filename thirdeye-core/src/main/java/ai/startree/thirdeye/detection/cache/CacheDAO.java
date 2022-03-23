/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.cache;

/**
 * interface for cache DAO. Should handle all read/write access to the data store of choice.
 */
public interface CacheDAO {

  /**
   * Tries to fetch the data for a given ThirdEyeCacheRequest and returns it.
   *
   * @param request ThirdEyeCacheRequest
   * @return ThirdEyeCacheResponse containing a list of {@link TimeSeriesDataPoint}
   * @throws Exception Only thrown if query errored out, NOT if no data was found!
   */
  ThirdEyeCacheResponse tryFetchExistingTimeSeries(ThirdEyeCacheRequest request) throws Exception;

  /**
   * Insert a TimeSeriesDataPoint into data store. Schema design is up to the user, although we
   * show
   * an example
   * schema that we use for Couchbase in {@link CouchbaseCacheDAO#insertTimeSeriesDataPoint(TimeSeriesDataPoint)}
   *
   * @param point TimeSeriesDataPoint
   */
  void insertTimeSeriesDataPoint(TimeSeriesDataPoint point);
}
