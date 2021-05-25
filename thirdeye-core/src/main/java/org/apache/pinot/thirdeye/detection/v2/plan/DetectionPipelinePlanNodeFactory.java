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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.pinot.thirdeye.api.v2.DetectionPlanApi;
import org.apache.pinot.thirdeye.detection.v2.PlanNode;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectionPipelinePlanNodeFactory {

  protected static final Logger LOG = LoggerFactory.getLogger(DetectionPipelinePlanNodeFactory.class);
  private static final Map<String, Class<? extends PlanNode>> PLAN_NODE_TYPE_TO_CLASS_MAP = new HashMap<>();
  public static final String V2_DETECTION_PLAN_PACKAGE_NAME = "org.apache.pinot.thirdeye.detection.v2.plan";

  static {
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
      if (!PLAN_NODE_TYPE_TO_CLASS_MAP.containsKey(typeKey)) {
        PLAN_NODE_TYPE_TO_CLASS_MAP.put(typeKey, planNodeClass);
      } else {
        LOG.error("Found duplicated type key: {}", typeKey);
        throw new RuntimeException("Found duplicated type key - " + typeKey);
      }
    }
    LOG.info("Initialized planNodeTypeToClassNameMap with {} functions: {} in {}ms",
        PLAN_NODE_TYPE_TO_CLASS_MAP.size(),
        PLAN_NODE_TYPE_TO_CLASS_MAP.keySet(),
        System.currentTimeMillis() - startTimeMs);
  }

  public static PlanNode get(String name,
      Map<String, PlanNode> pipelinePlanNodes,
      DetectionPlanApi detectionPlanApi, long startTime, long endTime) {
    String typeKey = detectionPlanApi.getType();
    Class<? extends PlanNode> planNodeClass = PLAN_NODE_TYPE_TO_CLASS_MAP.get(typeKey);
    if (planNodeClass == null) {
      throw new UnsupportedOperationException("Not supported type - " + typeKey);
    }
    try {
      final Constructor<?> constructor = planNodeClass
          .getConstructor(String.class,
              Map.class,
              DetectionPlanApi.class,
              long.class,
              long.class);
      return (DetectionPipelinePlanNode) constructor
          .newInstance(name, pipelinePlanNodes, detectionPlanApi, startTime, endTime);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to initialize the plan node: type - " + typeKey,
          e);
    }
  }

  public static Map<String, Class<? extends PlanNode>> getAllPlanNodes() {
    return PLAN_NODE_TYPE_TO_CLASS_MAP;
  }
}
