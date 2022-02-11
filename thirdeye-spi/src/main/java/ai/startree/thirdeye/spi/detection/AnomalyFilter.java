/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;

public interface AnomalyFilter<T extends AbstractSpec> extends BaseComponent<T> {

  /**
   * Check if an anomaly is qualified to pass the filter
   *
   * @return a boolean value to suggest if the anomaly should be filtered
   */
  boolean isQualified(MergedAnomalyResultDTO anomaly);
}
