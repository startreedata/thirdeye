/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorResult;

/**
 * Simple dataclass for AnomalyDetectorResult.
 * Does not perform protective copies.
 * */
public class SimpleAnomalyDetectorResult implements AnomalyDetectorResult {

  private final DataFrame dataFrame;

  public SimpleAnomalyDetectorResult(
      final DataFrame dataFrame) {
    this.dataFrame = dataFrame;
  }

  @Override
  public DataFrame getDataFrame() {
    return dataFrame;
  }
}
