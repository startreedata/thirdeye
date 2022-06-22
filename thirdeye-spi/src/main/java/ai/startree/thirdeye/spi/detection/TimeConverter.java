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
package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.Series;
import ai.startree.thirdeye.spi.dataframe.Series.ObjectFunction;

public interface TimeConverter {

  /**
   * Convert incoming time value string to milliseconds epoch value.
   *
   * @return milliseconds epoch value
   */
  long convert(String timeValue);

  /**
   * Convert back millis to String
   *
   * @param time
   * @return
   */
  String convertMillis(long time);

  /**
   * Convert incoming time series value to milliseconds epoch long series.
   *
   * @return milliseconds epoch long series.
   */
  default LongSeries convertSeries(final Series series) {
    return series.map((ObjectFunction) values -> convert(String.valueOf(values[0]))).getLongs();
  }
}
