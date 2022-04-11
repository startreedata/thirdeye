/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.plan;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
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

  public static final String DATA_SOURCE_CACHE_REF_KEY = "$DataSourceCache";
  private static final Logger LOG = LoggerFactory.getLogger(PlanNodeFactory.class);

  /* List of plan node classes that are built in with thirdeye */
  private static final List<Class<? extends PlanNode>> BUILT_IN_PLAN_NODE_CLASSES = ImmutableList.of(
      AnomalyDetectorPlanNode.class,
      CombinerPlanNode.class,
      DataFetcherPlanNode.class,
      EchoPlanNode.class,
      EnumeratorPlanNode.class,
      EventTriggerPlanNode.class,
      ForkJoinPlanNode.class,
      IndexFillerPlanNode.class,
      SqlExecutionPlanNode.class
  );
  /**
   * Contains the list of built in as well as node/operators coming from plugins.
   * TODO spyne implement loading nodes from plugins
   */
  private final Map<String, Class<? extends PlanNode>> planNodeTypeToClassMap;
  private final DataSourceCache dataSourceCache;

  @Inject
  public PlanNodeFactory(final DataSourceCache dataSourceCache) {
    this.dataSourceCache = dataSourceCache;
    this.planNodeTypeToClassMap = buildPlanNodeTypeToClassMap();
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
        .setProperties(ImmutableMap.of(DATA_SOURCE_CACHE_REF_KEY, dataSourceCache));

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
