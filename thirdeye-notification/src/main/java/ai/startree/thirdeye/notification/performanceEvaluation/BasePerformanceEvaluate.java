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
package ai.startree.thirdeye.notification.performanceEvaluation;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

public abstract class BasePerformanceEvaluate implements PerformanceEvaluate {

  /**
   * convert merge anomalies to a dimension-intervals map
   */
  public Map<DimensionMap, List<Interval>> mergedAnomalyResultsToIntervalMap(
      List<MergedAnomalyResultDTO> mergedAnomalyResultDTOList) {
    Map<DimensionMap, List<Interval>> anomalyIntervals = new HashMap<>();
    for (MergedAnomalyResultDTO mergedAnomaly : mergedAnomalyResultDTOList) {
      if (!anomalyIntervals.containsKey(mergedAnomaly.getDimensions())) {
        anomalyIntervals.put(mergedAnomaly.getDimensions(), new ArrayList<Interval>());
      }
      anomalyIntervals.get(mergedAnomaly.getDimensions()).add(
          new Interval(mergedAnomaly.getStartTime(), mergedAnomaly.getEndTime(), DateTimeZone.UTC));
    }
    return anomalyIntervals;
  }

  /**
   * convert merge anomalies to interval list without considering the dimension
   */
  public List<Interval> mergedAnomalyResultsToIntervals(
      List<MergedAnomalyResultDTO> mergedAnomalyResultDTOList) {
    List<Interval> anomalyIntervals = new ArrayList<>();
    for (MergedAnomalyResultDTO mergedAnomaly : mergedAnomalyResultDTOList) {
      anomalyIntervals.add(new Interval(mergedAnomaly.getStartTime(), mergedAnomaly.getEndTime(),DateTimeZone.UTC));
    }
    return anomalyIntervals;
  }
}
