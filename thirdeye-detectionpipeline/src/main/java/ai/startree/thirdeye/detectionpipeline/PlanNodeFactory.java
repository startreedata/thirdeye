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

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.plan.AnomalyDetectorPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.CombinerPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.DataFetcherPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EchoPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EnumeratorPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EventFetcherPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EventTriggerPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.ForkJoinPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.IndexFillerPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.SqlExecutionPlanNode;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.detection.v2.PlanNode;
import ai.startree.thirdeye.spi.detection.v2.PlanNodeContext;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jersey.repackaged.com.google.common.collect.ImmutableList;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PlanNodeFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PlanNodeFactory.class);

  /* List of plan node classes that are built in with thirdeye */
  private static final List<Class<? extends PlanNode>> BUILT_IN_PLAN_NODE_CLASSES = ImmutableList.of(
      AnomalyDetectorPlanNode.class,
      CombinerPlanNode.class,
      DataFetcherPlanNode.class,
      EchoPlanNode.class,
      EnumeratorPlanNode.class,
      EventTriggerPlanNode.class,
      EventFetcherPlanNode.class,
      ForkJoinPlanNode.class,
      IndexFillerPlanNode.class,
      SqlExecutionPlanNode.class,
      DelayPlanNode.class
  );
  /**
   * Contains the list of built in as well as node/operators coming from plugins.
   * TODO spyne implement loading nodes from plugins
   */
  private final Map<String, Class<? extends PlanNode>> planNodeTypeToClassMap;
  private final DataSourceCache dataSourceCache;
  private final DetectionRegistry detectionRegistry;
  private final EventManager eventDao;

  @Inject
  public PlanNodeFactory(final DataSourceCache dataSourceCache, final DetectionRegistry detectionRegistry, final EventManager eventDao) {
    this.dataSourceCache = dataSourceCache;
    this.detectionRegistry = detectionRegistry;
    this.planNodeTypeToClassMap = buildPlanNodeTypeToClassMap();
    this.eventDao = eventDao;
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

  private Map<String, Class<? extends PlanNode>> buildPlanNodeTypeToClassMap() {
    final HashMap<String, Class<? extends PlanNode>> stringClassHashMap = new HashMap<>();
    for (Class<? extends PlanNode> c : BUILT_IN_PLAN_NODE_CLASSES) {
      try {
        stringClassHashMap.put(c.newInstance().getType(), c);
      } catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException("Failed to initialize PlanNode: " + c.getSimpleName(), e);
      }
    }
    return stringClassHashMap;
  }

  public PlanNode build(final PlanNodeBean planNodeBean,
      final Interval detectionInterval,
      final Map<String, PlanNode> pipelinePlanNodes) {
    final PlanNodeContext context = new PlanNodeContext()
        .setName(planNodeBean.getName())
        .setPlanNodeBean(planNodeBean)
        .setDetectionInterval(detectionInterval)
        .setPipelinePlanNodes(pipelinePlanNodes)
        .setProperties(ImmutableMap.of(
            Constants.DATA_SOURCE_CACHE_REF_KEY, dataSourceCache,
            Constants.DETECTION_REGISTRY_REF_KEY, detectionRegistry,
            Constants.EVENT_MANAGER_REF_KEY, eventDao
        ));

    final String type = requireNonNull(planNodeBean.getType(), "node type is null");
    final Class<? extends PlanNode> planNodeClass = requireNonNull(planNodeTypeToClassMap.get(type),
        "Unknown node type: " + type);
    try {
      return build(planNodeClass, context);
    } catch (final Exception e) {
      throw new IllegalArgumentException("Failed to initialize the plan node: type - " + type, e);
    }
  }
}
