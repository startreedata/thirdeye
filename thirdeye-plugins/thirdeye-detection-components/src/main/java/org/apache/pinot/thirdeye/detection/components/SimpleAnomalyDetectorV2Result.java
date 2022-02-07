package org.apache.pinot.thirdeye.detection.components;

import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Result;

/**
 * Simple dataclass for AnomalyDetectorV2Result.
 * Does not perform protective copies.
 * */
public class SimpleAnomalyDetectorV2Result implements AnomalyDetectorV2Result {

  private final DataFrame dataFrame;

  public SimpleAnomalyDetectorV2Result(
      final DataFrame dataFrame) {
    this.dataFrame = dataFrame;
  }

  @Override
  public DataFrame getDataFrame() {
    return dataFrame;
  }
}
