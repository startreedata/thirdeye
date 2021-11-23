package org.apache.pinot.thirdeye.detection.v2.plan;

import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.operator.EchoOperator;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;

public class EchoPlanNode extends DetectionPipelinePlanNode {

  public static final String TYPE = "Echo";

  public EchoPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
  }

  @Override
  void setNestedProperties(final Map<String, Object> properties) {
    // inject detector to nested property if possible
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
  public Operator run() throws Exception {
    final EchoOperator operator = new EchoOperator();
    operator.init(new OperatorContext()
        .setStartTime(getParams().getOrDefault("startTime", String.valueOf(this.startTime)).toString())
        .setEndTime(getParams().getOrDefault("endTime", String.valueOf(this.endTime)).toString())
        .setTimeFormat(getParams().getOrDefault("timeFormat", OperatorContext.DEFAULT_TIME_FORMAT).toString())
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean)
    );
    return operator;
  }
}
