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
package ai.startree.thirdeye.anomaly.grouping;

import ai.startree.thirdeye.spi.datalayer.dto.GroupedAnomalyResultsDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns a group that contains all input anomalies. Additionally, this class always returns empty
 * auxiliary email
 * recipients.
 */
public class DummyAlertGrouper extends BaseAlertGrouper {

  @Override
  public Map<DimensionMap, GroupedAnomalyResultsDTO> group(
      List<MergedAnomalyResultDTO> anomalyResults) {
    Map<DimensionMap, GroupedAnomalyResultsDTO> groupMap = new HashMap<>();
    GroupedAnomalyResultsDTO groupedAnomalyResults = new GroupedAnomalyResultsDTO();
    groupedAnomalyResults.setAnomalyResults(anomalyResults);
    groupMap.put(new DimensionMap(), groupedAnomalyResults);
    return groupMap;
  }
}
