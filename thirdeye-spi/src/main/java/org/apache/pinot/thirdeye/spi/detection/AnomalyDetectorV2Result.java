package org.apache.pinot.thirdeye.spi.detection;

import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.joda.time.Period;

public interface AnomalyDetectorV2Result {

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

  /**
   * Returns the TimeZone string of the detector.
   * Used to infer the end time of the anomaly if the last point is an anomaly.
   */
  String getTimeZone();

  /**
   * Returns the monitoring granularity of the detector.
   * Used to infer the end time of the anomaly if the last point is an anomaly.
   */
  Period getMonitoringGranularityPeriod();
}
