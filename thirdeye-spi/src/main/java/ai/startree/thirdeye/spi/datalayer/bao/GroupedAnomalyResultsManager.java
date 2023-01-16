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
package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.GroupedAnomalyResultsDTO;

public interface GroupedAnomalyResultsManager extends AbstractManager<GroupedAnomalyResultsDTO> {

  /**
   * Returns the GroupedAnomalyResults, which has the largest end time, in the specified time
   * window.
   *
   * @param alertConfigId the alert config id of the grouped anomaly results.
   * @param dimensions the dimension map of the grouped anomaly results.
   * @param windowStart the start time in milliseconds of the time range.
   * @param windowEnd the end time in milliseconds of the time range.
   * @return the GroupedAnomalyResults, which has the largest end time, in the specified time window.
   */
  GroupedAnomalyResultsDTO findMostRecentInTimeWindow(long alertConfigId, String dimensions,
      long windowStart,
      long windowEnd);
}
