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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DetectionPipelineOperator forms the root of the detection class hierarchy. It represents a wireframe
 * for implementing (intermittently stateful) executable pipelines on top of it.
 */
public abstract class DetectionPipelinePlanNode implements PlanNode {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelinePlanNode.class);

  protected PlanNodeBean planNodeBean = null;
  protected long startTime = -1;
  protected long endTime = -1;
  protected Map<String, DetectionPipelineResult> inputsMap = new HashMap<>();

  private PlanNodeContext context;

  protected DetectionPipelinePlanNode() {
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    this.context = planNodeContext;

    this.planNodeBean = planNodeContext.getPlanNodeBean();
    this.startTime = planNodeContext.getStartTime();
    this.endTime = planNodeContext.getEndTime();
  }

  @Override
  public PlanNodeContext getContext() {
    return requireNonNull(context, "node not initialized! " + getClass().getSimpleName());
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  @Override
  public List<InputBean> getPlanNodeInputs() {
    return planNodeBean.getInputs();
  }

  public void setInput(String key, DetectionPipelineResult input) {
    this.inputsMap.put(key, input);
  }

  @Override
  public Map<String, Object> getParams() {
    return planNodeBean.getParams();
  }

  @Override
  public String getName() {
    return getContext().getName();
  }
}
