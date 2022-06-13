/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.alert.grouping;

import ai.startree.thirdeye.spi.datalayer.dto.GroupedAnomalyResultsDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
