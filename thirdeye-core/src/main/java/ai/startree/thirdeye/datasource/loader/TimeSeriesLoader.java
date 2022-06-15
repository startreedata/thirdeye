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

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.metric.MetricSlice;

/**
 * Loader for metric series based on slice and an (optinal) granularity. Must be thread safe.
 */
public interface TimeSeriesLoader {

  /**
   * Returns the metric time series for a given time range and filter set, with a specified
   * time granularity. If the underlying time series resolution does not correspond to the desired
   * time granularity, it is up-sampled (via forward fill) or down-sampled (via sum if additive, or
   * last value otherwise) transparently.
   *
   * <br/><b>NOTE:</b> if the start timestamp does not align with the time
   * resolution, it is aligned with the nearest lower time stamp.
   *
   * @param slice metric slice to fetch
   * @return dataframe with aligned timestamps and values
   */
  DataFrame load(MetricSlice slice) throws Exception;
}
