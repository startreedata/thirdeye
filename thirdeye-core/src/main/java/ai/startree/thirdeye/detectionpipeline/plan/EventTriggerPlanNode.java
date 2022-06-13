/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.plan;

import ai.startree.thirdeye.detectionpipeline.operator.EventTriggerOperator;
import ai.startree.thirdeye.spi.detection.v2.Operator;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.PlanNodeContext;
import java.util.Map;

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
