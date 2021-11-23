package org.apache.pinot.thirdeye.detection.v2.plan;

import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.operator.TimeIndexFillerOperator;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;

public class IndexFillerPlanNode extends DetectionPipelinePlanNode {

  @Override
  void setNestedProperties(final Map<String, Object> properties) {
  }

  @Override
  public String getType() {
    return "TimeIndexFiller";
  }

  @Override
  public Operator run() throws Exception {
    final TimeIndexFillerOperator timeIndexFillerOperator = new TimeIndexFillerOperator();
    timeIndexFillerOperator.init(new OperatorContext()
        .setStartTime(getParams().getOrDefault("startTime", String.valueOf(this.startTime)).toString())
        .setEndTime(getParams().getOrDefault("endTime", String.valueOf(this.endTime)).toString())
        .setPlanNode(planNodeBean)
        .setInputsMap(inputsMap)
    );
    return timeIndexFillerOperator;
  }
}
