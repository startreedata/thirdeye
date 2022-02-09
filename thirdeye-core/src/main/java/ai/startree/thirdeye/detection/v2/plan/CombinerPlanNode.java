package ai.startree.thirdeye.detection.v2.plan;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Collections.emptyMap;

import ai.startree.thirdeye.detection.v2.operator.CombinerOperator;
import ai.startree.thirdeye.spi.detection.v2.Operator;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.PlanNodeContext;
import java.util.Map;

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
  public String getType() {
    return TYPE;
  }

  @Override
  public Map<String, Object> getParams() {
    return params;
  }

  @Override
  public Operator buildOperator() throws Exception {
    final CombinerOperator operator = new CombinerOperator();
    operator.init(new OperatorContext()
        .setStartTime((Long) getParams().getOrDefault("startTime", startTime))
        .setEndTime((Long) getParams().getOrDefault("endTime", endTime))
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean)
    );
    return operator;
  }
}
