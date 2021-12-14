package org.apache.pinot.thirdeye.spi.detection;

import java.util.Map;
import org.apache.pinot.thirdeye.spi.dataframe.DataFrame;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.joda.time.Interval;
import org.joda.time.Period;

public interface AnomalyDetectorV2<T extends AbstractSpec> extends BaseComponent<T> {

  // Keys available in Map<String, DataTable> timeSeriesMap
  String KEY_CURRENT = "current";
  String KEY_BASELINE = "baseline";

  /**
   * Run detection for a given interval with provided baseline and current DataTable.
   * Returns a DataFrame with columns:
   * {@value DataFrame#COL_TIME}: timestamp in epoch milliseconds,
   * {@value DataFrame#COL_ANOMALY}: boolean series: whether the observation is an anomaly,
   * {@value DataFrame#COL_CURRENT}: current value,
   * {@value DataFrame#COL_VALUE}: baseline value,
   * {@value DataFrame#COL_UPPER_BOUND}: baseline upper bound,
   * {@value DataFrame#COL_LOWER_BOUND}: baseline lower bound.
   */
  DataFrame runDetection(Interval interval, Map<String, DataTable> timeSeriesMap)
      throws DetectorException;

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
