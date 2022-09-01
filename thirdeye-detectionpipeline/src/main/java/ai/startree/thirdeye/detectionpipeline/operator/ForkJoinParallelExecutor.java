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
import ai.startree.thirdeye.mapper.PlanNodeMapper;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.detection.v2.DetectionResult;
import ai.startree.thirdeye.spi.detection.v2.PlanNode;
import ai.startree.thirdeye.spi.detection.v2.PlanNodeContext;
import ai.startree.thirdeye.util.StringTemplateUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class ForkJoinParallelExecutor {

  private static final int PARALLELISM = 5;

  private final PlanNode root;
  private final List<EnumerationItemDTO> enumerationItems;
  private final ExecutorService executorService;

  public ForkJoinParallelExecutor(final PlanNode root,
      final List<EnumerationItemDTO> enumerationItems) {
    this.root = root;
    this.enumerationItems = enumerationItems;
    executorService = Executors.newFixedThreadPool(PARALLELISM);
  }

  public List<ForkJoinResultItem> execute() {
    final var callables = prepareCallables();
    return executeAll(callables);
  }

  public List<Callable<ForkJoinResultItem>> prepareCallables() {
    final List<Callable<ForkJoinResultItem>> callables = new ArrayList<>();

    for (final var enumerationItem : enumerationItems) {
      /* Clone all nodes for execution. Feed enumeration result */
      final Map<String, PlanNode> clonedPipelinePlanNodes = clonePipelinePlanNodes(root
          .getContext()
          .getPipelinePlanNodes(), enumerationItem);

      /* Get the new root node in the cloned DAG */
      final PlanNode rootClone = clonedPipelinePlanNodes.get(root.getName());

      /* Create a callable for parallel execution */
      callables.add((() -> {
        /* The context stores all the outputs from all the nodes */
        final Map<ContextKey, DetectionResult> context = new HashMap<>();

        /* Execute the DAG */
        executePlanNode(clonedPipelinePlanNodes, context, rootClone);

        /* Return the output */
        return new ForkJoinResultItem(enumerationItem, PlanExecutor.getOutput(context, rootClone.getName()));
      }));
    }
    return callables;
  }

  private Map<String, PlanNode> clonePipelinePlanNodes(
      final Map<String, PlanNode> pipelinePlanNodes,
      final EnumerationItemDTO enumerationItem) {
    final Map<String, PlanNode> clonedPipelinePlanNodes = new HashMap<>();
    for (final Map.Entry<String, PlanNode> key : pipelinePlanNodes.entrySet()) {
      final PlanNode planNode = deepCloneWithNewContext(key.getValue(),
          enumerationItem.getParams(),
          clonedPipelinePlanNodes);
      clonedPipelinePlanNodes.put(key.getKey(), planNode);
    }
    return clonedPipelinePlanNodes;
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
    final TemplatableMap<String, Object> params = applyTemplatePropertiesOnParams(n.getParams(),
        templateProperties);
    return PlanNodeMapper.INSTANCE.clone(n).setParams(params);
  }

  private TemplatableMap<String, Object> applyTemplatePropertiesOnParams(
      final TemplatableMap<String, Object> params, final Map<String, Object> templateProperties) {
    if (params == null) {
      return null;
    }
    try {
      return new TemplatableMap<>(StringTemplateUtils.applyContext(params, templateProperties));
    } catch (final IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private List<ForkJoinResultItem> executeAll(final List<Callable<ForkJoinResultItem>> callables) {

    final var futures = callables.stream()
        .map(executorService::submit)
        .collect(Collectors.toList());
    try {

      final List<ForkJoinResultItem> results = new ArrayList<>();
      for (final var future : futures) {
        results.add(future.get(10, TimeUnit.SECONDS));
      }

      return results;
    } catch (final InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }
}
