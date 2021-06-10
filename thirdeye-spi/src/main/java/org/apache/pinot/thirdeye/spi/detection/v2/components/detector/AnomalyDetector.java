package org.apache.pinot.thirdeye.spi.detection.v2.components.detector;

import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.DetectorException;
import org.apache.pinot.thirdeye.spi.detection.v2.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.joda.time.Interval;

public interface AnomalyDetector<T extends AbstractSpec> extends BaseComponent<T> {

  /**
   * Run detection for a given interval with provided baseline and current DataTable. Then returns
   * the detection result.
   *
   * @return the detection result which contains anomalies.
   */
  DetectionPipelineResult runDetection(Interval interval, DataTable baseline, DataTable current)
      throws DetectorException;
}
