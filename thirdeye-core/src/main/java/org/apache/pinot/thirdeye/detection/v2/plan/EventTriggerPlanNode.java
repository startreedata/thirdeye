package org.apache.pinot.thirdeye.detection.v2.plan;

import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.operator.EventTriggerOperator;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;

public class EventTriggerPlanNode extends DetectionPipelinePlanNode {

  private static final String PROP_TRIGGER = "trigger";

  public EventTriggerPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
  }

  @Override
  public String getType() {
    return "EventTrigger";
  }

  @Override
  public Map<String, Object> getParams() {
    return planNodeBean.getParams();
  }

  @Override
  public Operator buildOperator() throws Exception {
    final EventTriggerOperator eventTriggerOperator = new EventTriggerOperator();
    eventTriggerOperator.init(new OperatorContext()
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean)
    );
    return eventTriggerOperator;
  }
}
