/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.dataframe.DataFrame;

public interface AnomalyDetectorResult {

  /**
  * Returns a DataFrame with columns:
   * {@value DataFrame#COL_TIME}: timestamp in epoch milliseconds,
   * {@value DataFrame#COL_ANOMALY}: boolean series: whether the observation is an anomaly,
   * {@value DataFrame#COL_CURRENT}: current value,
   * {@value DataFrame#COL_VALUE}: baseline value,
   * {@value DataFrame#COL_UPPER_BOUND}: baseline upper bound,
   * {@value DataFrame#COL_LOWER_BOUND}: baseline lower bound.
   */
  DataFrame getDataFrame();
}
