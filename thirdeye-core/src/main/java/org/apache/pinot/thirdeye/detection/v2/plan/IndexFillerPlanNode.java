package org.apache.pinot.thirdeye.detection.v2.plan;

import org.apache.pinot.thirdeye.detection.v2.operator.TimeIndexFillerOperator;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;

public class IndexFillerPlanNode extends DetectionPipelinePlanNode {

  @Override
  public String getType() {
    return "TimeIndexFiller";
  }

  @Override
  public Operator buildOperator() throws Exception {
    final TimeIndexFillerOperator timeIndexFillerOperator = new TimeIndexFillerOperator();
    timeIndexFillerOperator.init(new OperatorContext()
        .setStartTime(this.startTime)
        .setEndTime(this.endTime)
        .setPlanNode(planNodeBean)
        .setInputsMap(inputsMap)
    );
    return timeIndexFillerOperator;
  }
}
