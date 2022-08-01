/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.detectionpipeline.plan;

import static ai.startree.thirdeye.detectionpipeline.operator.ForkJoinOperator.K_COMBINER;
import static ai.startree.thirdeye.detectionpipeline.operator.ForkJoinOperator.K_ENUMERATOR;
import static ai.startree.thirdeye.detectionpipeline.operator.ForkJoinOperator.K_ROOT;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.operator.ForkJoinOperator;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.detection.v2.Operator;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.PlanNode;
import ai.startree.thirdeye.spi.detection.v2.PlanNodeContext;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

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
  public Map<String, Templatable<Object>> getParams() {
    return planNodeBean.getParams();
  }

  @Override
  public Operator buildOperator() throws Exception {
    final PlanNode enumerator = getPlanNode(K_ENUMERATOR);
    final PlanNode root = getPlanNode(K_ROOT);
    final PlanNode combiner = getPlanNode(K_COMBINER);

    final ForkJoinOperator operator = new ForkJoinOperator();
    operator.init(new OperatorContext()
        .setDetectionInterval(detectionInterval)
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
    final String nodeName = String.valueOf(requireNonNull(getParams().get(key).value(),
        "param missing: " + key));
    return requireNonNull(pipelinePlanNodes.get(nodeName), "node not found: " + nodeName);
  }
}
