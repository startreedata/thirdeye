package org.apache.pinot.thirdeye.detection.components;

import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2Result;
import org.joda.time.Period;

/**
 * Simple dataclass for AnomalyDetectorV2Result.
 * Does not perform protective copies.
 * */
public class SimpleAnomalyDetectorV2Result implements AnomalyDetectorV2Result {

  private final DataFrame dataFrame;
  private final String timeZone;
  private final Period period;

  public SimpleAnomalyDetectorV2Result(
      final DataFrame dataFrame, final String timeZone, final Period period) {
    this.dataFrame = dataFrame;
    this.period = period;
    this.timeZone = timeZone;
  }

  @Override
  public DataFrame getDataFrame() {
    return dataFrame;
  }

  @Override
  public String getTimeZone() {
    return timeZone;
  }

  @Override
  public Period getMonitoringGranularityPeriod() {
    return period;
  }
}
