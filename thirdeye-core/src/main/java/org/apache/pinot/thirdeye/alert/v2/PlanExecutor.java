package org.apache.pinot.thirdeye.alert.v2;

import java.util.Map;
import java.util.Map.Entry;
import org.apache.pinot.thirdeye.spi.api.v2.DetectionPlanApi.InputApi;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;

public class PlanExecutor {

  private static final String CONTEXT_KEY_SPLIT = "\t\t\t\t";

  public static void executePlanNode(Map<String, PlanNode> pipelinePlanNodes,
      Map<String, DetectionPipelineResult> context, final PlanNode node)
      throws Exception {
    for (InputApi input : node.getPlanNodeInputs()) {
      final String contextKey = getContextKey(input.getSourcePlanNode(), input.getSourceProperty());
      if (!context.containsKey(contextKey)) {
        PlanNode inputPlanNode = pipelinePlanNodes.get(input.getSourcePlanNode());
        executePlanNode(pipelinePlanNodes, context, inputPlanNode);
      }
      if (!context.containsKey(contextKey)) {
        throw new RuntimeException("Missing context key - " + contextKey);
      }
      node.setInput(input.getTargetProperty(), context.get(contextKey));
    }
    Operator operator = node.run();
    operator.execute();
    Map<String, DetectionPipelineResult> outputs = operator.getOutputs();
    for (Entry<String, DetectionPipelineResult> output : outputs.entrySet()) {
      context.put(getContextKey(node.getName(), output.getKey()), output.getValue());
    }
  }

  public static String getContextKey(final String name, final String key) {
    return String.format("%s" + CONTEXT_KEY_SPLIT + "%s", name, key);
  }

  public static String getOutputKeyFromContextKey(final String contextKey) {
    return contextKey.split(CONTEXT_KEY_SPLIT)[1];
  }

  public static String getNodeFromContextKey(final String contextKey) {
    return contextKey.split(CONTEXT_KEY_SPLIT)[0];
  }
}
