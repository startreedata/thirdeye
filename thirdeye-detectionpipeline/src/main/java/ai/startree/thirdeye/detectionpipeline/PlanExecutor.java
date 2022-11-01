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
package ai.startree.thirdeye.detectionpipeline;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Singleton
public class PlanExecutor {

  public static final String ROOT_OPERATOR_KEY = "root";

  private final PlanNodeFactory planNodeFactory;

  private final DataSourceCache dataSourceCache;
  private final DetectionRegistry detectionRegistry;
  private final PostProcessorRegistry postProcessorRegistry;
  private final EventManager eventManager;
  private final DatasetConfigManager datasetConfigManager;
  private final ApplicationContext applicationContext;

  @Inject
  public PlanExecutor(final PlanNodeFactory planNodeFactory,
      final DataSourceCache dataSourceCache,
      final DetectionRegistry detectionRegistry,
      final PostProcessorRegistry postProcessorRegistry,
      final EventManager eventManager,
      final DatasetConfigManager datasetConfigManager) {
    this.planNodeFactory = planNodeFactory;
    this.dataSourceCache = dataSourceCache;
    this.detectionRegistry = detectionRegistry;
    this.postProcessorRegistry = postProcessorRegistry;
    this.eventManager = eventManager;
    this.datasetConfigManager = datasetConfigManager;

    applicationContext = createApplicationContext();
  }

  private ApplicationContext createApplicationContext() {
    return new ApplicationContext(
        dataSourceCache,
        detectionRegistry,
        postProcessorRegistry,
        eventManager,
        datasetConfigManager
    );
  }

  @VisibleForTesting
  public static void executePlanNode(final Map<String, PlanNode> pipelinePlanNodes,
      final PlanNode node,
      final Map<ContextKey, OperatorResult> resultMap)
      throws Exception {
    for (final InputBean input : optional(node.getPlanNodeInputs()).orElse(emptyList())) {
      final ContextKey contextKey = key(input.getSourcePlanNode(), input.getSourceProperty());
      if (!resultMap.containsKey(contextKey)) {
        final PlanNode inputPlanNode = pipelinePlanNodes.get(input.getSourcePlanNode());
        checkArgument(inputPlanNode != null,
            "sourcePlanNode \"%s\" found in \"%s\" node configuration does not exist. Template is invalid.",
            input.getSourcePlanNode(), node.getName());
        executePlanNode(pipelinePlanNodes, inputPlanNode, resultMap);
      }
      if (!resultMap.containsKey(contextKey)) {
        throw new RuntimeException("Missing resultMap key - " + contextKey);
      }
      node.setInput(input.getTargetProperty(), resultMap.get(contextKey));
    }
    final Operator operator = node.buildOperator();
    operator.execute();
    final Map<String, OperatorResult> outputs = operator.getOutputs();
    for (final Entry<String, OperatorResult> output : outputs.entrySet()) {
      resultMap.put(key(node.getName(), output.getKey()), output.getValue());
    }
  }

  @VisibleForTesting
  static ContextKey key(final String name, final String key) {
    return new ContextKey(name, key);
  }

  public static Map<String, OperatorResult> getOutput(
      final Map<ContextKey, OperatorResult> context, final String nodeName) {
    final Map<String, OperatorResult> results = new HashMap<>();
    for (final ContextKey contextKey : context.keySet()) {
      if (contextKey.getNodeName().equals(nodeName)) {
        results.put(contextKey.getKey(), context.get(contextKey));
      }
    }
    return results;
  }

  /**
   * @param planNodeBeans The pipeline DAG as a list of nodes
   * @return Outputs from the root node in the DAG
   * @throws Exception All exceptions are to be handled by upstream consumer.
   */
  public Map<String, OperatorResult> runPipelineAndGetRootOutputs(
      final List<PlanNodeBean> planNodeBeans,
      final PlanNodeContext runTimeContext)
      throws Exception {
    final Map<ContextKey, OperatorResult> context = runPipeline(planNodeBeans, runTimeContext);

    /* Return the output */
    return getOutput(context, ROOT_OPERATOR_KEY);
  }

  /**
   * Main interface for running the pipeline.
   *
   * @param planNodeBeans The pipeline DAG as a list of nodes
   * @return The result map. All the outputs from all the nodes are emitted here.
   * @throws Exception All exceptions are to be handled by upstream consumer.
   */
  public Map<ContextKey, OperatorResult> runPipeline(
      final List<PlanNodeBean> planNodeBeans,
      final PlanNodeContext runTimeContext) throws Exception {

    /* Set Application Context */
    runTimeContext.setApplicationContext(applicationContext);

    /* map of all the plan nodes constructed from beans(persisted objects) */
    final Map<String, PlanNode> pipelinePlanNodes = buildPlanNodeMap(
        planNodeBeans,
        runTimeContext);

    /* The context stores all the outputs from all the nodes */
    final Map<ContextKey, OperatorResult> resultMap = new HashMap<>();

    /* Execute the DAG */
    final PlanNode rootNode = pipelinePlanNodes.get(ROOT_OPERATOR_KEY);
    executePlanNode(pipelinePlanNodes, rootNode, resultMap);

    return resultMap;
  }

  @VisibleForTesting
  Map<String, PlanNode> buildPlanNodeMap(final List<PlanNodeBean> planNodeBeans,
      final PlanNodeContext runTimeContext) {
    final Map<String, PlanNode> pipelinePlanNodes = new HashMap<>();
    for (final PlanNodeBean planNodeBean : planNodeBeans) {
      final PlanNode planNode = planNodeFactory.build(
          planNodeBean,
          runTimeContext,
          pipelinePlanNodes
      );

      pipelinePlanNodes.put(planNodeBean.getName(), planNode);
    }
    return pipelinePlanNodes;
  }
}
