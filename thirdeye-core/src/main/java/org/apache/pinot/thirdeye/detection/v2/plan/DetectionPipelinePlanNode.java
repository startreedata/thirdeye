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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.api.v2.DetectionPlanApi;
import org.apache.pinot.thirdeye.api.v2.DetectionPlanApi.InputApi;
import org.apache.pinot.thirdeye.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.detection.v2.PlanNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DetectionPipeline forms the root of the detection class hierarchy. It represents a wireframe
 * for implementing (intermittently stateful) executable pipelines on top of it.
 */
public abstract class DetectionPipelinePlanNode implements PlanNode {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelinePlanNode.class);

  protected final String name;
  protected final Map<String, PlanNode> pipelinePlanNodes;
  protected final DetectionPlanApi detectionPlanApi;
  protected final long startTime;
  protected final long endTime;
  protected final Map<String, DetectionPipelineResult> inputsMap;

  protected DetectionPipelinePlanNode() {
    this.name = null;
    this.pipelinePlanNodes = null;
    this.detectionPlanApi = null;
    this.startTime = -1;
    this.endTime = -1;
    this.inputsMap = null;
  }

  protected DetectionPipelinePlanNode(String name, Map<String, PlanNode> pipelinePlanNodes,
      DetectionPlanApi detectionPlanApi, long startTime,
      long endTime) {
    this.name = name;
    this.pipelinePlanNodes = pipelinePlanNodes;
    this.detectionPlanApi = detectionPlanApi;
    this.startTime = startTime;
    this.endTime = endTime;
    this.inputsMap = new HashMap<>();
  }

  /**
   * @param properties Used to set properties during plan execution.
   */
  abstract void setNestedProperties(Map<String, Object> properties);

  public DetectionPlanApi getDetectionPlanApi() {
    return detectionPlanApi;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  @Override
  public List<InputApi> getPlanNodeInputs() {
    return detectionPlanApi.getInputs();
  }

  public void setInput(String key, DetectionPipelineResult input) {
    this.inputsMap.put(key, input);
  }

  @Override
  public Map<String, Object> getParams() {
    return detectionPlanApi.getParams();
  }

  @Override
  public String getName() {
    return name;
  }
}
