package org.apache.pinot.thirdeye.detection.v2.plan;

import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.detection.v2.operator.AnomalyDetectorOperator;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;

public class AnomalyDetectorPlanNode extends DetectionPipelinePlanNode {

  private static final String PROP_DETECTOR = "detector";
  private static final String PROP_METRIC_URN = "metricUrn";

  public AnomalyDetectorPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
  }

  @Override
  void setNestedProperties(final Map<String, Object> properties) {
    // inject detector to nested property if possible
    String detectorComponentRefKey = MapUtils.getString(planNodeApi.getParams(),
        PROP_DETECTOR);
    if (detectorComponentRefKey != null) {
      String detectorComponentName = DetectionUtils.getComponentKey(detectorComponentRefKey);
      properties.put(PROP_DETECTOR, detectorComponentRefKey);
    }

    // inject metricUrn to nested property if possible
    String nestedUrn = MapUtils.getString(planNodeApi.getParams(), PROP_METRIC_URN);
    if (nestedUrn != null) {
      properties.put(PROP_METRIC_URN, nestedUrn);
    }
  }

  @Override
  public String getType() {
    return "AnomalyDetector";
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Map<String, Object> getParams() {
    return planNodeApi.getParams();
  }

  @Override
  public Operator<DataTable> run() throws Exception {
    long startTime;
    try {
      startTime = Long.parseLong(getParams().get("startTime").toString());
    } catch (Exception e) {
      startTime = this.startTime;
    }
    long endTime;
    try {
      endTime = Long.parseLong(getParams().get("endTime").toString());
    } catch (Exception e) {
      endTime = this.endTime;
    }
    final AnomalyDetectorOperator anomalyDetectorOperator = new AnomalyDetectorOperator();
    anomalyDetectorOperator.init(new OperatorContext()
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setInputsMap(inputsMap)
        .setDetectionPlanApi(planNodeApi)
    );
    return anomalyDetectorOperator;
  }
}
