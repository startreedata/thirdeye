package org.apache.pinot.thirdeye.detection.v2.plan;

import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.detection.v2.operator.EventTriggerOperator;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.v2.DataTable;
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
  void setNestedProperties(final Map<String, Object> properties) {
    // inject trigger to nested property if possible
    String triggerComponentRefKey = MapUtils.getString(planNodeBean.getParams(),
        PROP_TRIGGER);
    if (triggerComponentRefKey != null) {
      String triggerComponentName = DetectionUtils.getComponentKey(triggerComponentRefKey);
      properties.put(triggerComponentName, triggerComponentRefKey);
    }
  }

  @Override
  public String getType() {
    return "EventTrigger";
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
  public Operator<DataTable> run() throws Exception {
    final EventTriggerOperator eventTriggerOperator = new EventTriggerOperator();
    eventTriggerOperator.init(new OperatorContext()
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean)
    );
    return eventTriggerOperator;
  }
}
