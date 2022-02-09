package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorV2Result;

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
