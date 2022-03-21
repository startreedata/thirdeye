/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.alert.grouping;

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
