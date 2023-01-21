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
package ai.startree.thirdeye.spi.detection.v2;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Operator result.
 *
 * Note:
 * Can be output by any operator, so it does not necessarily contain detection results.
 * */
public interface OperatorResult {

  /**
   * If implemented, returns the last timestamp observed in the data. Can be different from the last
   * processed timestamp.
   */
  default long getLastTimestamp() {
    return optional(getTimeseries())
        .map(TimeSeries::getTime)
        .filter(longSeries -> longSeries.size() > 0)
        .map(longSeries -> longSeries.get(longSeries.size() - 1))
        .orElse(-1L);
  }

  default @Nullable List<AnomalyDTO> getAnomalies() {
    return null;
  }

  default @Nullable EnumerationItemDTO getEnumerationItem() {
    return null;
  }

  default @Nullable Map<String, List> getRawData() {
    return null;
  }

  default @Nullable TimeSeries getTimeseries() {
    return null;
  }
}
