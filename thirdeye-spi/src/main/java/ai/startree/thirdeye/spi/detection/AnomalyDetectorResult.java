/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.DataFrame;

public interface AnomalyDetectorResult {

  /**
  * Returns a DataFrame with columns:
   * {@value Constants#COL_TIME}: timestamp in epoch milliseconds,
   * {@value Constants#COL_ANOMALY}: boolean series: whether the observation is an anomaly,
   * {@value Constants#COL_CURRENT}: current value,
   * {@value Constants#COL_VALUE}: baseline value,
   * {@value Constants#COL_UPPER_BOUND}: baseline upper bound,
   * {@value Constants#COL_LOWER_BOUND}: baseline lower bound.
   */
  DataFrame getDataFrame();
}
