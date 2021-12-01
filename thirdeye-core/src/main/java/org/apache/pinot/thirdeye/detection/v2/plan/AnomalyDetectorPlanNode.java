package org.apache.pinot.thirdeye.detection.v2.plan;

import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.operator.AnomalyDetectorOperator;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;

public class AnomalyDetectorPlanNode extends DetectionPipelinePlanNode {

  public static final String TYPE = "AnomalyDetector";
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
  public String getType() {
    return TYPE;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Map<String, Object> getParams() {
    return planNodeBean.getParams();
  }

  @Override
  public Operator buildOperator() throws Exception {
    final AnomalyDetectorOperator anomalyDetectorOperator = new AnomalyDetectorOperator();
    anomalyDetectorOperator.init(new OperatorContext()
        .setStartTime(String.valueOf(this.startTime))
        .setEndTime(String.valueOf(this.endTime))
        .setTimeFormat(getParams()
            .getOrDefault("timeFormat", OperatorContext.DEFAULT_TIME_FORMAT)
            .toString())
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean)
    );
    return anomalyDetectorOperator;
  }
}
