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

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.spi.api.PlanNodeApi;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DetectionPipelinePlanNodeFactory {

  public static final String V2_DETECTION_PLAN_PACKAGE_NAME = "org.apache.pinot.thirdeye.detection.v2.plan";
  public static final String DATA_SOURCE_CACHE_REF_KEY = "$DataSourceCache";
  protected static final Logger LOG = LoggerFactory.getLogger(DetectionPipelinePlanNodeFactory.class);
  private final Map<String, Class<? extends PlanNode>> planNodeTypeToClassMap = new HashMap<>();
  private final DataSourceCache dataSourceCache;

  @Inject
  public DetectionPipelinePlanNodeFactory(DataSourceCache dataSourceCache) {
    this.dataSourceCache = dataSourceCache;
    initPlanNodeTypeToClassMap();
  }

  private void initPlanNodeTypeToClassMap() {
    long startTimeMs = System.currentTimeMillis();
    Reflections reflections = new Reflections(V2_DETECTION_PLAN_PACKAGE_NAME);
    Set<Class<? extends PlanNode>> classes = reflections.getSubTypesOf(PlanNode.class);
    for (Class<? extends PlanNode> planNodeClass : classes) {
      if (Modifier.isAbstract(planNodeClass.getModifiers())) {
        continue;
      }
      String typeKey;
      try {
        PlanNode planNodeInstance = planNodeClass.newInstance();
        typeKey = planNodeInstance.getType();
      } catch (Exception e) {
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

  public PlanNode get(String name,
      Map<String, PlanNode> pipelinePlanNodes,
      PlanNodeApi planNodeApi, long startTime, long endTime) {
    String typeKey = planNodeApi.getType();
    Class<? extends PlanNode> planNodeClass = planNodeTypeToClassMap.get(typeKey);
    if (planNodeClass == null) {
      throw new UnsupportedOperationException("Not supported type - " + typeKey);
    }
    try {
      final Constructor<?> constructor = planNodeClass.getConstructor();
      PlanNode planNode = (PlanNode) constructor.newInstance();
      planNode.init(new PlanNodeContext()
          .setName(name)
          .setPipelinePlanNodes(pipelinePlanNodes)
          .setDetectionPlanApi(planNodeApi)
          .setStartTime(startTime)
          .setEndTime(endTime)
          .setProperties(ImmutableMap.of(DATA_SOURCE_CACHE_REF_KEY, dataSourceCache)));
      return planNode;
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to initialize the plan node: type - " + typeKey,
          e);
    }
  }

  public Map<String, Class<? extends PlanNode>> getAllPlanNodes() {
    return planNodeTypeToClassMap;
  }
}
