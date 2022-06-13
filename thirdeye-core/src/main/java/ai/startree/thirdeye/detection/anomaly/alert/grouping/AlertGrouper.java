/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.alert.grouping;

import ai.startree.thirdeye.spi.datalayer.dto.GroupedAnomalyResultsDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.List;
import java.util.Map;

public interface AlertGrouper {

  /**
   * Sets the properties of this grouper.
   *
   * @param props the properties for this grouper.
   */
  void setParameters(Map<String, String> props);

  /**
   * Given a list of anomaly results, this method returns groups of anomaly results such that each
   * group should be
   * sent in the same alert.
   *
   * @return groups of anomaly results.
   */
  Map<DimensionMap, GroupedAnomalyResultsDTO> group(List<MergedAnomalyResultDTO> anomalyResults);
}
