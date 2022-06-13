/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.cache;

import ai.startree.thirdeye.spi.datasource.ThirdEyeRequest;
import ai.startree.thirdeye.spi.datasource.ThirdEyeResponse;

/**
 * Loads data from either the data source or the centralized cache.
 */

public interface TimeSeriesCache {

  /**
   * Returns a ThirdEyeResponse object that contains the desired time-series data associated
   * with the request. Attempts to first pull the desired data from the centralized cache, and if
   * it is not in the cache, will request it from the original data source.
   *
   * @param request ThirdEyeRequest object that contains all the info to build a query
   * @return ThirdEyeResponse with time-series rows
   */
  ThirdEyeResponse fetchTimeSeries(ThirdEyeRequest request) throws Exception;

  /**
   * Takes in a time-series pulled from the original data source, and stores
   * it into the centralized cache.
   *
   * @param response a response object containing the timeseries to be inserted
   */
  void insertTimeSeriesIntoCache(ThirdEyeResponse response);
}
