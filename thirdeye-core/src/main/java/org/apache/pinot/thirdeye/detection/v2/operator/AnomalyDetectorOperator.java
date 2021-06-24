package org.apache.pinot.thirdeye.detection.v2.operator;

import java.util.Collections;
import java.util.List;
import org.apache.pinot.thirdeye.spi.detection.AnomalyDetectorV2;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.joda.time.Interval;

public class AnomalyDetectorOperator extends DetectionPipelineOperator<DataTable> {

  private static String DEFAULT_OUTPUT_KEY = "AnomalyDetectorResult";
  private static String CURRENT_KEY = "current";
  private static String BASELINE_KEY = "baseline";

  public AnomalyDetectorOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
  }

  @Override
  public void execute() throws Exception {
    // The last exception of the detection windows. It will be thrown out to upper level.
    for (Object key : this.getComponents().keySet()) {
      final BaseComponent component = this.getComponents().get(key);
      if (component instanceof AnomalyDetectorV2) {
        for (Interval interval : getMonitoringWindows()) {
          DetectionPipelineResult detectionResult = ((AnomalyDetectorV2) component)
              .runDetection(interval,
                  (DataTable) inputMap.get(BASELINE_KEY),
                  (DataTable) inputMap.get(CURRENT_KEY));
          String outputKey = key + "_" + DEFAULT_OUTPUT_KEY;
          setOutput(outputKey, detectionResult);
        }
      }
    }
  }

  private List<Interval> getMonitoringWindows() {
    return Collections.singletonList(new Interval(startTime, endTime));
  }

  @Override
  public String getOperatorName() {
    return "AnomalyDetectorOperator";
  }
}
