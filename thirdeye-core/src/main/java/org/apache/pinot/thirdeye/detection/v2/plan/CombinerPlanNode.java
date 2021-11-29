package org.apache.pinot.thirdeye.detection.v2.plan;

import static java.util.Collections.emptyMap;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.operator.CombinerOperator;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;

public class CombinerPlanNode extends DetectionPipelinePlanNode {

  public static final String TYPE = "Combiner";
  private Map<String, Object> params;

  public CombinerPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
    params = optional(planNodeBean.getParams()).orElse(emptyMap());
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
    return params;
  }

  @Override
  public Operator run() throws Exception {
    final CombinerOperator operator = new CombinerOperator();
    operator.init(new OperatorContext()
        .setStartTime(params
            .getOrDefault("startTime", String.valueOf(this.startTime))
            .toString())
        .setEndTime(params
            .getOrDefault("endTime", String.valueOf(this.endTime))
            .toString())
        .setTimeFormat(params
            .getOrDefault("timeFormat", OperatorContext.DEFAULT_TIME_FORMAT)
            .toString())
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean)
    );
    return operator;
  }
}
