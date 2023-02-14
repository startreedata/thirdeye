/*
 * Copyright 2023 StarTree Inc
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

import static ai.startree.thirdeye.spi.detection.DetectionPipelineUsage.DETECTION;
import static ai.startree.thirdeye.spi.detection.DetectionPipelineUsage.EVALUATION;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.DetectionPipelineContext;
import ai.startree.thirdeye.detectionpipeline.Operator;
import ai.startree.thirdeye.detectionpipeline.OperatorContext;
import ai.startree.thirdeye.detectionpipeline.PlanNode;
import ai.startree.thirdeye.detectionpipeline.operator.EnumeratorOperator.EnumeratorResult;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForkJoinOperator extends DetectionPipelineOperator {

  public static final String K_ENUMERATOR = "enumerator";
  public static final String K_ROOT = "root";
  public static final String K_COMBINER = "combiner";
  private static final int PARALLELISM = 5;

  private PlanNode enumerator;
  private PlanNode root;
  private PlanNode combiner;
  private DetectionPipelineContext detectionPipelineContext;

  private static AlertDTO newAlert(final Long alertId) {
    final AlertDTO alert = new AlertDTO();
    alert.setId(alertId);
    return alert;
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    detectionPipelineContext = context.getPlanNodeContext().getDetectionPipelineContext();

    final Map<String, Object> properties = context.getProperties();
    enumerator = (PlanNode) properties.get("enumerator");
    root = (PlanNode) properties.get("root");
    combiner = (PlanNode) properties.get("combiner");
  }

  @Override
  public void execute() throws Exception {
    final Boolean dryRun = optional(planNode.getParams().get("dryRun"))
        .map(Templatable::value)
        .map(b -> (Boolean) b)
        .orElse(false);

    /* Get all enumerations */
    final EnumeratorResult enumeratorResult = getEnumeratorResult();
    if (dryRun) {
      resultMap.put(dryRunOutputName(), enumeratorResult);
      return;
    }
    final List<EnumerationItemDTO> enumerationItems = prepareEnumerationItems(enumeratorResult.getResults());
    /* Execute in parallel */
    final ForkJoinParallelExecutor parallelExecutor = new ForkJoinParallelExecutor(
        detectionPipelineContext);
    final var allResults = parallelExecutor.execute(root, enumerationItems);

    /* Combine results */
    final Map<String, OperatorResult> outputs = runCombiner(new ForkJoinResult(allResults));
    resultMap.putAll(outputs);
  }

  private String dryRunOutputName() {
    return String.format("%s:%s:%s", getPlanNode().getType(), getPlanNode().getName(), "dryRun");
  }

  private EnumeratorResult getEnumeratorResult() throws Exception {
    final Operator op = enumerator.buildOperator();
    op.execute();
    final Map<String, OperatorResult> outputs = op.getOutputs();
    return (EnumeratorResult) outputs.get(EnumeratorOperator.DEFAULT_OUTPUT_KEY);
  }

  private Map<String, OperatorResult> runCombiner(final ForkJoinResult forkJoinResult)
      throws Exception {
    final Operator combinerOp = combiner.buildOperator();
    combinerOp.setInput(CombinerOperator.DEFAULT_INPUT_KEY, forkJoinResult);
    combinerOp.execute();

    return combinerOp.getOutputs();
  }

  private List<EnumerationItemDTO> prepareEnumerationItems(
      final List<EnumerationItemDTO> enumerationItems) {
    final DetectionPipelineUsage usage = requireNonNull(detectionPipelineContext.getUsage(),
        "Detection pipeline usage is not set");

    if (DETECTION.equals(usage)) {
      // Add alert id to enumeration items
      final Long alertId = requireNonNull(detectionPipelineContext.getAlertId(),
          "alert ID is not set");

      /* decorate enumeration item with alert id */
      final var decorated = enumerationItems.stream()
          .map(e -> e.setAlert(newAlert(alertId)))
          .collect(Collectors.toList());

      /* find existing or create new enumeration item */
      return decorated.stream()
          .map(this::findExistingOrCreate)
          .collect(Collectors.toList());
    } else if (EVALUATION.equals(usage)) {
      // do nothing - no need to persist enumerationItems nor fetch existing one downstream
      return enumerationItems;
    }
    // don't remove - put here to ensure it breaks if an enum is added one day
    throw new UnsupportedOperationException("DetectionPipelineUsage not implemented: " + usage);
  }

  private EnumerationItemDTO findExistingOrCreate(final EnumerationItemDTO source) {
    return detectionPipelineContext.getApplicationContext()
        .getEnumerationItemManager()
        .findExistingOrCreate(source);
  }

  @Override
  public String getOperatorName() {
    return "forkjoin";
  }
}
