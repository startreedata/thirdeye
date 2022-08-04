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

import static ai.startree.thirdeye.detectionpipeline.PlanExecutor.executePlanNode;

import ai.startree.thirdeye.detectionpipeline.ContextKey;
import ai.startree.thirdeye.detectionpipeline.PlanExecutor;
import ai.startree.thirdeye.detectionpipeline.PlanNodeFactory;
import ai.startree.thirdeye.detectionpipeline.operator.EnumeratorOperator.EnumeratorResult;
import ai.startree.thirdeye.mapper.PlanNodeMapper;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.detection.model.DetectionResult;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.spi.detection.v2.Operator;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.PlanNode;
import ai.startree.thirdeye.spi.detection.v2.PlanNodeContext;
import ai.startree.thirdeye.util.StringTemplateUtils;
import com.google.common.collect.ImmutableBiMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class ForkJoinOperator extends DetectionPipelineOperator {

  public static final String K_ENUMERATOR = "enumerator";
  public static final String K_ROOT = "root";
  public static final String K_COMBINER = "combiner";
  private static final int PARALLELISM = 5;

  private PlanNode enumerator;
  private PlanNode root;
  private PlanNode combiner;
  private ExecutorService executorService;

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    final Map<String, Object> properties = context.getProperties();
    enumerator = (PlanNode) properties.get("enumerator");
    root = (PlanNode) properties.get("root");
    combiner = (PlanNode) properties.get("combiner");

    executorService = Executors.newFixedThreadPool(PARALLELISM);
  }

  @Override
  public void execute() throws Exception {
    /* Get all enumerations */
    final List<Map<String, Object>> enumeratorResults = getEnumeratorResults();

    /* Execute in parallel */
    final List<Callable<Map<String, DetectionPipelineResult>>> callables =
        prepareCallables(enumeratorResults);
    final List<Map<String, DetectionPipelineResult>> allResults = executeAll(callables);

    /* Combine results */
    final Map<String, DetectionPipelineResult> outputs = combineChildOutputs(allResults);
    resultMap.putAll(outputs);
  }

  private List<Map<String, Object>> getEnumeratorResults() throws Exception {
    final Operator op = enumerator.buildOperator();
    op.execute();
    final Map<String, DetectionPipelineResult> outputs = op.getOutputs();
    final EnumeratorResult enumeratorResult = (EnumeratorResult) outputs.get(EnumeratorOperator.DEFAULT_OUTPUT_KEY);
    return enumeratorResult.getResults();
  }

  private Map<String, DetectionPipelineResult> combineChildOutputs(
      final List<Map<String, DetectionPipelineResult>> allResults) throws Exception {
    final Operator combinerOp = combiner.buildOperator();
    combinerOp.setInput(CombinerOperator.DEFAULT_INPUT_KEY, new ForkJoinResult(allResults));
    combinerOp.execute();

    return combinerOp.getOutputs();
  }

  private List<Callable<Map<String, DetectionPipelineResult>>> prepareCallables(
      final List<Map<String, Object>> results) {
    final List<Callable<Map<String, DetectionPipelineResult>>> callables = new ArrayList<>();

    for (final Map<String, Object> result : results) {
      /* Clone all nodes for execution. Feed enumeration result */
      final Map<String, PlanNode> clonedPipelinePlanNodes = clonePipelinePlanNodes(root
          .getContext()
          .getPipelinePlanNodes(), result);

      /* Get the new root node in the cloned DAG */
      final PlanNode rootClone = clonedPipelinePlanNodes.get(root.getName());

      /* Create a callable for parallel execution */
      callables.add((() -> {
        /* The context stores all the outputs from all the nodes */
        final Map<ContextKey, DetectionPipelineResult> context = new HashMap<>();

        /* Execute the DAG */
        executePlanNode(clonedPipelinePlanNodes, context, rootClone);

        /* Return the output */
        return PlanExecutor.getOutput(context, rootClone.getName());
      }));
    }
    return callables;
  }

  private Map<String, PlanNode> clonePipelinePlanNodes(
      final Map<String, PlanNode> pipelinePlanNodes,
      final Map<String, Object> templateProperties) {
    final Map<String, PlanNode> clonedPipelinePlanNodes = new HashMap<>();
    for (final Map.Entry<String, PlanNode> key : pipelinePlanNodes.entrySet()) {
      final PlanNode planNode = deepCloneWithNewContext(key.getValue(),
          templateProperties,
          clonedPipelinePlanNodes);
      clonedPipelinePlanNodes.put(key.getKey(), planNode);
    }
    return clonedPipelinePlanNodes;
  }

  private List<Map<String, DetectionPipelineResult>> executeAll(
      final List<Callable<Map<String, DetectionPipelineResult>>> callables) {

    final List<Future<Map<String, DetectionPipelineResult>>> futures = callables.stream()
        .map(c -> executorService.submit(c))
        .collect(Collectors.toList());
    try {

      final List<Map<String, DetectionPipelineResult>> results = new ArrayList<>();
      for (final Future<Map<String, DetectionPipelineResult>> future : futures) {
        results.add(future.get(10, TimeUnit.SECONDS));
      }

      return results;
    } catch (final InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  private PlanNode deepCloneWithNewContext(final PlanNode sourceNode,
      final Map<String, Object> templateProperties,
      final Map<String, PlanNode> clonedPipelinePlanNodes) {
    try {
      /* Cloned context should contain the new nodes */
      final PlanNodeContext context = sourceNode.getContext();
      final PlanNodeContext clonedContext = cloneContext(context, templateProperties)
          .setPipelinePlanNodes(clonedPipelinePlanNodes);

      return PlanNodeFactory.build(sourceNode.getClass(), clonedContext);
    } catch (final ReflectiveOperationException e) {
      throw new RuntimeException("Failed to clone PlanNode: " + sourceNode.getName(), e);
    }
  }

  private PlanNodeContext cloneContext(final PlanNodeContext context,
      final Map<String, Object> templateProperties) {
    return new PlanNodeContext()
        .setName(context.getName())
        .setPlanNodeBean(clonePlanNodeBean(templateProperties, context.getPlanNodeBean()))
        .setProperties(context.getProperties())
        .setDetectionInterval(context.getDetectionInterval());
  }

  private PlanNodeBean clonePlanNodeBean(final Map<String, Object> templateProperties,
      final PlanNodeBean n) {
    final Map<String, Object> params = applyTemplatePropertiesOnParams(n.getParams(),
        templateProperties);
    return PlanNodeMapper.INSTANCE.clone(n).setParams(params);
  }

  private Map<String, Object> applyTemplatePropertiesOnParams(
      final Map<String, Object> params, final Map<String, Object> templateProperties) {
    if (params == null) {
      return null;
    }
    try {
      return ImmutableBiMap.copyOf(StringTemplateUtils.applyContext(
          new HashMap<>(params),
          templateProperties
      ));
    } catch (final IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getOperatorName() {
    return "forkjoin";
  }

  public static class ForkJoinResult implements DetectionPipelineResult {

    private final List<Map<String, DetectionPipelineResult>> results;

    public ForkJoinResult(final List<Map<String, DetectionPipelineResult>> results) {
      this.results = results;
    }

    @Override
    public List<DetectionResult> getDetectionResults() {
      return null;
    }

    public List<Map<String, DetectionPipelineResult>> getResults() {
      return results;
    }
  }
}
