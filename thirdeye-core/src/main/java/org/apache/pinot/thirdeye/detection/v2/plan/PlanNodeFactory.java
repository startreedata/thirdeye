/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.detection.v2.plan;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PlanNodeFactory {

  public static final String V2_DETECTION_PLAN_PACKAGE_NAME = "org.apache.pinot.thirdeye.detection.v2.plan";
  public static final String DATA_SOURCE_CACHE_REF_KEY = "$DataSourceCache";
  protected static final Logger LOG = LoggerFactory.getLogger(PlanNodeFactory.class);
  private final Map<String, Class<? extends PlanNode>> planNodeTypeToClassMap = new HashMap<>();
  private final DataSourceCache dataSourceCache;

  @Inject
  public PlanNodeFactory(final DataSourceCache dataSourceCache) {
    this.dataSourceCache = dataSourceCache;
    initPlanNodeTypeToClassMap();
  }

  private void initPlanNodeTypeToClassMap() {
    final long startTimeMs = System.currentTimeMillis();
    final Reflections reflections = new Reflections(V2_DETECTION_PLAN_PACKAGE_NAME);
    final Set<Class<? extends PlanNode>> classes = reflections.getSubTypesOf(PlanNode.class);
    for (final Class<? extends PlanNode> planNodeClass : classes) {
      if (Modifier.isAbstract(planNodeClass.getModifiers())) {
        continue;
      }
      final String typeKey;
      try {
        final PlanNode planNodeInstance = planNodeClass.newInstance();
        typeKey = planNodeInstance.getType();
      } catch (final Exception e) {
        throw new RuntimeException("Unable to init PlanNode Class - " + planNodeClass, e);
      }
      if (!planNodeTypeToClassMap.containsKey(typeKey)) {
        planNodeTypeToClassMap.put(typeKey, planNodeClass);
      } else {
        LOG.error("Found duplicated type key: {}", typeKey);
        throw new RuntimeException("Found duplicated type key - " + typeKey);
      }
    }
    LOG.info("Initialized planNodeTypeToClassNameMap with {} functions: {} in {}ms",
        planNodeTypeToClassMap.size(),
        planNodeTypeToClassMap.keySet(),
        System.currentTimeMillis() - startTimeMs);
  }

  public PlanNode build(final PlanNodeBean planNodeBean,
      final long startTime,
      final long endTime,
      final Map<String, PlanNode> pipelinePlanNodes) {
    final String typeKey = requireNonNull(planNodeBean.getType(), "node type is null");
    final Class<? extends PlanNode> planNodeClass =
        requireNonNull(planNodeTypeToClassMap.get(typeKey), "Unknown node type: " + typeKey);
    try {
      final Constructor<?> constructor = planNodeClass.getConstructor();
      final PlanNode planNode = (PlanNode) constructor.newInstance();
      planNode.init(new PlanNodeContext()
          .setName(planNodeBean.getName())
          .setPlanNodeBean(planNodeBean)
          .setStartTime(startTime)
          .setEndTime(endTime)
          .setPipelinePlanNodes(pipelinePlanNodes)
          .setProperties(ImmutableMap.of(DATA_SOURCE_CACHE_REF_KEY, dataSourceCache)));
      return planNode;
    } catch (final Exception e) {
      throw new IllegalArgumentException("Failed to initialize the plan node: type - " + typeKey,
          e);
    }
  }

  public Map<String, Class<? extends PlanNode>> getAllPlanNodes() {
    return planNodeTypeToClassMap;
  }
}
