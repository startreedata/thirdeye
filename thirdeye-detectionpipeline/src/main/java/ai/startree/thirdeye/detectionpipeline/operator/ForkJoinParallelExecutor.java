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

import static ai.startree.thirdeye.detectionpipeline.PlanExecutor.executePlanNode;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.ApplicationContext;
import ai.startree.thirdeye.detectionpipeline.ContextKey;
import ai.startree.thirdeye.detectionpipeline.DetectionPipelineContext;
import ai.startree.thirdeye.detectionpipeline.ForkJoinConfiguration;
import ai.startree.thirdeye.detectionpipeline.PlanExecutor;
import ai.startree.thirdeye.detectionpipeline.PlanNode;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class ForkJoinParallelExecutor {

  private final ForkJoinConfiguration config;
  private final DetectionPipelineContext detectionPipelineContext;
  private final ExecutorService subTaskExecutor;

  public ForkJoinParallelExecutor(final DetectionPipelineContext detectionPipelineContext) {
    this.detectionPipelineContext = detectionPipelineContext;

    final ApplicationContext applicationContext = requireNonNull(detectionPipelineContext
        .getApplicationContext(), "application context is null");
    this.config = applicationContext.getConfiguration().getForkjoin();
    this.subTaskExecutor = applicationContext.getSubTaskExecutor();
  }

  public List<ForkJoinResultItem> execute(final PlanNode root,
      final List<EnumerationItemDTO> enumerationItems) {
    final var callables = prepareCallables(root, enumerationItems);
    return executeAll(callables);
  }

  public List<Callable<ForkJoinResultItem>> prepareCallables(
      final PlanNode root, final List<EnumerationItemDTO> enumerationItems) {
    final List<Callable<ForkJoinResultItem>> callables = new ArrayList<>();

    for (final var enumerationItem : enumerationItems) {
      /* Clone all nodes for execution. Feed enumeration result */
      final Map<String, PlanNode> clonedPipelinePlanNodes = new ForkJoinPipelineBuilder()
          .clonePipelinePlanNodes(root
              .getContext()
              .getPipelinePlanNodes(), enumerationItem);

      /* Get the new root node in the cloned DAG */
      final PlanNode rootClone = clonedPipelinePlanNodes.get(root.getName());

      /* Create a callable for parallel execution */
      callables.add((() -> {
        /* The context stores all the outputs from all the nodes */
        final Map<ContextKey, OperatorResult> context = new HashMap<>();

        /* Execute the DAG */
        executePlanNode(clonedPipelinePlanNodes, rootClone, context);

        /* Return the output */
        final Map<String, OperatorResult> outputs = PlanExecutor.getOutput(context,
            rootClone.getName());
        return new ForkJoinResultItem(enumerationItem, outputs, detectionPipelineContext);
      }));
    }
    return callables;
  }

  private List<ForkJoinResultItem> executeAll(final List<Callable<ForkJoinResultItem>> callables) {
    final var futures = callables.stream()
        .map(subTaskExecutor::submit)
        .collect(Collectors.toList());
    try {

      final List<ForkJoinResultItem> results = new ArrayList<>();
      for (final var future : futures) {
        results.add(future.get(config.getTimeout().getSeconds(), TimeUnit.SECONDS));
      }

      return results;
    } catch (final InterruptedException | ExecutionException | TimeoutException e) {
      futures.forEach(f -> f.cancel(true));
      throw new RuntimeException(e);
    }
  }
}
