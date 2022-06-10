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
