/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.detection.anomaly.alert.grouping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.pinot.thirdeye.spi.datalayer.dto.GroupedAnomalyResultsDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.dimension.DimensionMap;

/**
 * This class simply merges the new GroupedAnomaly with recent GroupedAnomaly regardless the gap of
 * time between them.
 */
public class SimpleGroupedAnomalyMerger {

  public static Map<DimensionMap, GroupedAnomalyResultsDTO> timeBasedMergeGroupedAnomalyResults(
      Map<DimensionMap, GroupedAnomalyResultsDTO> recentGroupedAnomaly,
      Map<DimensionMap, GroupedAnomalyResultsDTO> newGroupedAnomalies) {
    Map<DimensionMap, GroupedAnomalyResultsDTO> updatedGroupedAnomalies = new HashMap<>();
    for (Map.Entry<DimensionMap, GroupedAnomalyResultsDTO> groupedAnomalyEntry : newGroupedAnomalies
        .entrySet()) {
      // Retrieve the most recent anomaly for the same dimension
      GroupedAnomalyResultsDTO mostRecentGroupedAnomaly = recentGroupedAnomaly
          .get(groupedAnomalyEntry.getKey());

      // Merge grouped anomalies
      GroupedAnomalyResultsDTO updatedGroupedAnomaly;
      if (mostRecentGroupedAnomaly != null) {
        List<MergedAnomalyResultDTO> mergedAnomalyResults = mostRecentGroupedAnomaly
            .getAnomalyResults();
        Set<Long> existingMergedAnomalyId = new HashSet<>();
        for (MergedAnomalyResultDTO mergedAnomalyResult : mergedAnomalyResults) {
          existingMergedAnomalyId.add(mergedAnomalyResult.getId());
        }
        for (MergedAnomalyResultDTO newMergedAnomaly : groupedAnomalyEntry.getValue()
            .getAnomalyResults()) {
          if (!existingMergedAnomalyId.contains(newMergedAnomaly.getId())) {
            mergedAnomalyResults.add(newMergedAnomaly);
            existingMergedAnomalyId.add(newMergedAnomaly.getId());
          }
        }
        updatedGroupedAnomaly = mostRecentGroupedAnomaly;
      } else {
        updatedGroupedAnomaly = groupedAnomalyEntry.getValue();
      }
      updatedGroupedAnomalies.put(groupedAnomalyEntry.getKey(), updatedGroupedAnomaly);
    }
    return updatedGroupedAnomalies;
  }
}
