package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.detection.v2.DataTable;
import java.util.Map;
import org.joda.time.Interval;

public interface AnomalyDetectorV2<T extends AbstractSpec> extends BaseComponent<T> {

  // Keys available in Map<String, DataTable> timeSeriesMap
  String KEY_CURRENT = "current";
  String KEY_BASELINE = "baseline";

  /**
   * Run detection for a given interval with provided baseline and current DataTable.
   */
  AnomalyDetectorV2Result runDetection(Interval interval, Map<String, DataTable> timeSeriesMap)
      throws DetectorException;
}
