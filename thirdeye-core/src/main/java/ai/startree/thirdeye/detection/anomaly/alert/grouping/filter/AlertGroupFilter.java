/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.alert.grouping.filter;

import ai.startree.thirdeye.spi.datalayer.dto.GroupedAnomalyResultsDTO;
import java.util.Map;

// TODO: Unify merged and grouped anomaly. Afterwards, unify their alert filter.

/**
 * A filter for determining if a given grouped anomaly is qualified for sending an alert.
 */
public interface AlertGroupFilter {

  /**
   * Sets the properties of this grouper.
   *
   * @param props the properties for this grouper.
   */
  void setParameters(Map<String, String> props);

  /**
   * Returns if the given grouped anomaly is qualified for passing through the filter.
   *
   * @param groupedAnomaly the given grouped anomaly.
   * @return true if the given grouped anomaly passes through the filter.
   */
  boolean isQualified(GroupedAnomalyResultsDTO groupedAnomaly);
}
