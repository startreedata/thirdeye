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
package ai.startree.thirdeye.detectionpipeline.operator;

import ai.startree.thirdeye.detectionpipeline.operator.EnumeratorOperator.EnumeratorResult;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.spi.detection.v2.Operator;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.PlanNode;
import java.util.List;
import java.util.Map;

public class ForkJoinOperator extends DetectionPipelineOperator {

  public static final String K_ENUMERATOR = "enumerator";
  public static final String K_ROOT = "root";
  public static final String K_COMBINER = "combiner";

  private PlanNode enumerator;
  private PlanNode root;
  private PlanNode combiner;

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    final Map<String, Object> properties = context.getProperties();
    enumerator = (PlanNode) properties.get("enumerator");
    root = (PlanNode) properties.get("root");
    combiner = (PlanNode) properties.get("combiner");
  }

  @Override
  public void execute() throws Exception {
    /* Get all enumerations */
    final List<EnumerationItemDTO> enumeratorResults = getEnumeratorResults();

    /* Execute in parallel */
    final var allResults = new ForkJoinParallelExecutor(root, enumeratorResults).execute();

    /* Combine results */
    final Map<String, DetectionPipelineResult> outputs = runCombiner(new ForkJoinResult(allResults));
    resultMap.putAll(outputs);
  }

  private List<EnumerationItemDTO> getEnumeratorResults() throws Exception {
    final Operator op = enumerator.buildOperator();
    op.execute();
    final Map<String, DetectionPipelineResult> outputs = op.getOutputs();
    final EnumeratorResult enumeratorResult = (EnumeratorResult) outputs.get(EnumeratorOperator.DEFAULT_OUTPUT_KEY).getDetectionResults().get(0);
    return enumeratorResult.getResults();
  }

  private Map<String, DetectionPipelineResult> runCombiner(final ForkJoinResult forkJoinResult) throws Exception {
    final Operator combinerOp = combiner.buildOperator();
    combinerOp.setInput(CombinerOperator.DEFAULT_INPUT_KEY, forkJoinResult);
    combinerOp.execute();

    return combinerOp.getOutputs();
  }

  @Override
  public String getOperatorName() {
    return "forkjoin";
  }
}
