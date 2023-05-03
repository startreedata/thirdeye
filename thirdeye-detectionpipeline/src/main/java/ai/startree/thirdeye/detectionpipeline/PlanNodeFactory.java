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
package ai.startree.thirdeye.detectionpipeline;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.plan.AnomalyDetectorPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.DataFetcherPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.DelayPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EchoPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EventFetcherPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EventTriggerPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.IndexFillerPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.PostProcessorPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.SqlExecutionPlanNode;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PlanNodeFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PlanNodeFactory.class);

  /* List of plan node classes that are built in with thirdeye */
  private static final List<Class<? extends PlanNode>> BUILT_IN_PLAN_NODE_CLASSES = ImmutableList.of(
      AnomalyDetectorPlanNode.class,
      DataFetcherPlanNode.class,
      EchoPlanNode.class,
      EventTriggerPlanNode.class,
      EventFetcherPlanNode.class,
      IndexFillerPlanNode.class,
      SqlExecutionPlanNode.class,
      DelayPlanNode.class,
      PostProcessorPlanNode.class
  );
  /**
   * Contains the list of built in as well as node/operators coming from plugins.
   * TODO spyne implement loading nodes from plugins
   */
  private final Map<String, Class<? extends PlanNode>> typeVsPlanNodeClassMap = new HashMap<>();

  @Inject
  public PlanNodeFactory() {
    BUILT_IN_PLAN_NODE_CLASSES.forEach(this::registerPlanNodeClass);
  }

  public static PlanNode build(
      final Class<? extends PlanNode> planNodeClass,
      final PlanNodeContext context
  ) throws ReflectiveOperationException {
    final Constructor<?> constructor = planNodeClass.getConstructor();
    final PlanNode planNode = (PlanNode) constructor.newInstance();
    planNode.init(context);
    return planNode;
  }

  public void registerPlanNodeClass(final Class<? extends PlanNode> c) {
    final String type;
    try {
      // TODO spyne this interface needs enhancement. We should not need to instantiate the class
      type = c.getDeclaredConstructor().newInstance().getType();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to initialize PlanNode: " + c.getSimpleName(), e);
    }
    checkState(!typeVsPlanNodeClassMap.containsKey(type),
        String.format("PlanNode with type: %s is already registered. class: %s",
            type,
            optional(typeVsPlanNodeClassMap.get(type)).map(Class::getName).orElse("null")));

    typeVsPlanNodeClassMap.put(type, c);
  }

  public PlanNode build(final PlanNodeBean planNodeBean,
      final DetectionPipelineContext detectionPipelineContext,
      final Map<String, PlanNode> pipelinePlanNodes) {
    final PlanNodeContext context = new PlanNodeContext()
        .setDetectionPipelineContext(detectionPipelineContext)
        .setName(planNodeBean.getName())
        .setPlanNodeBean(planNodeBean)
        .setPipelinePlanNodes(pipelinePlanNodes);

    final String type = requireNonNull(planNodeBean.getType(), "node type is null");
    final Class<? extends PlanNode> planNodeClass = requireNonNull(typeVsPlanNodeClassMap.get(type),
        "Unknown node type: " + type);
    try {
      return build(planNodeClass, context);
    } catch (final Exception e) {
      throw new IllegalArgumentException("Failed to initialize the plan node: type - " + type, e);
    }
  }
}
