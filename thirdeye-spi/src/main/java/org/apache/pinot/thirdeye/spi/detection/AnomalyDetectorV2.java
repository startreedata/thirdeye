package org.apache.pinot.thirdeye.spi.detection;

import java.util.Map;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.joda.time.Interval;

public interface AnomalyDetectorV2<T extends AbstractSpec> extends BaseComponent<T> {

  // Keys available in Map<String, DataTable> timeSeriesMap
  String KEY_CURRENT = "current";
  String KEY_BASELINE = "baseline";

  String COL_ANOMALY = "anomaly";

  /**
   * Run detection for a given interval with provided baseline and current DataTable. Then returns
   * the detection result.
   *
   * @return the detection result which contains anomalies.
   */
  DetectionPipelineResult runDetection(Interval interval, Map<String, DataTable> timeSeriesMap)
      throws DetectorException;

  /**
   * Set TimeConverter to allow AnomalyDetectorV2 change the timeSeries from any format to millis
   * epoch LongSeries.
   *
   * @param timeConverter
   */
  void setTimeConverter(TimeConverter timeConverter);
}
