/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.DataFrame;

public interface AnomalyDetectorResult {

  /**
  * Returns a DataFrame with columns:
   * {@value Constants#COL_TIME}: timestamp in epoch milliseconds,
   * {@value Constants#COL_ANOMALY}: boolean series: whether the observation is an anomaly,
   * {@value Constants#COL_CURRENT}: current value,
   * {@value Constants#COL_VALUE}: baseline value,
   * {@value Constants#COL_UPPER_BOUND}: baseline upper bound,
   * {@value Constants#COL_LOWER_BOUND}: baseline lower bound.
   */
  DataFrame getDataFrame();
}
