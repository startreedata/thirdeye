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
