/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
