package org.apache.pinot.thirdeye.detection.v2.plan;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.detection.v2.operator.ForkJoinOperator.K_COMBINER;
import static org.apache.pinot.thirdeye.detection.v2.operator.ForkJoinOperator.K_ENUMERATOR;
import static org.apache.pinot.thirdeye.detection.v2.operator.ForkJoinOperator.K_ROOT;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.operator.ForkJoinOperator;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;

public class ForkJoinPlanNode extends DetectionPipelinePlanNode {

  public static final String TYPE = "ForkJoin";
  private static final String PROP_DETECTOR = "detector";
  private static final String PROP_METRIC_URN = "metricUrn";
  private Map<String, PlanNode> pipelinePlanNodes;

  public ForkJoinPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
    pipelinePlanNodes = planNodeContext.getPipelinePlanNodes();
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Map<String, Object> getParams() {
    return planNodeBean.getParams();
  }

  @Override
  public Operator buildOperator() throws Exception {
    final PlanNode enumerator = getPlanNode(K_ENUMERATOR);
    final PlanNode root = getPlanNode(K_ROOT);
    final PlanNode combiner = getPlanNode(K_COMBINER);

    final ForkJoinOperator operator = new ForkJoinOperator();
    operator.init(new OperatorContext()
        .setStartTime((Long) getParams().getOrDefault("startTime", startTime))
        .setEndTime((Long) getParams().getOrDefault("endTime", endTime))
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean)
        .setProperties(ImmutableMap.of(
            K_ENUMERATOR, enumerator,
            K_ROOT, root,
            K_COMBINER, combiner
        ))
    );
    return operator;
  }

  private PlanNode getPlanNode(final String key) {
    final String nodeName = String.valueOf(requireNonNull(getParams().get(key),
        "param missing: " + key));
    return requireNonNull(pipelinePlanNodes.get(nodeName), "node not found: " + nodeName);
  }
}
