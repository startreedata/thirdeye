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

package org.apache.pinot.thirdeye.detection.v2.operator;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.detection.v2.utils.EpochTimeConverter;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import org.apache.pinot.thirdeye.spi.detection.TimeConverter;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DetectionPipelineOperator forms the root of the detection class hierarchy. It represents a wireframe
 * for implementing (intermittently stateful) executable pipelines on top of it.
 */
public abstract class DetectionPipelineOperator implements Operator {

  protected static final String PROP_TYPE = "type";
  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelineOperator.class);
  private static final TimeConverter TIME_CONVERTER = new EpochTimeConverter(TimeUnit.MILLISECONDS.toString());

  protected PlanNodeBean planNode;
  protected long startTime;
  protected long endTime;
  protected Map<String, DetectionPipelineResult> resultMap = new HashMap<>();
  protected Map<String, DetectionPipelineResult> inputMap;
  protected Map<String, String> outputKeyMap = new HashMap<>();

  protected DetectionPipelineOperator() {
  }

  protected static Map<String, Object> getComponentSpec(final Map<String, Object> params) {
    final Map<String, Object> componentSpec = new HashMap<>();
    if (params == null || params.isEmpty()) {
      return componentSpec;
    }
    final String prefix = "component.";
    params.forEach((key, value) -> {
      if (key.startsWith(prefix)) {
        componentSpec.put(key.substring(prefix.length()), value);
      }
    });
    return componentSpec;
  }

  @Override
  public void init(final OperatorContext context) {
    planNode = context.getPlanNode();
    startTime = context.getStartTime();
    endTime = context.getEndTime();
    checkArgument(startTime <= endTime, "start time cannot be greater than end time");

    resultMap = new HashMap<>();
    inputMap = context.getInputsMap();
    if (context.getPlanNode().getOutputs() != null) {
      for (final OutputBean outputBean : context.getPlanNode().getOutputs()) {
        outputKeyMap.put(outputBean.getOutputKey(), outputBean.getOutputName());
      }
    }
  }

  /**
   * Returns a detection result for the time range between {@code startTime} and {@code endTime}.
   *
   * @return detection result
   */
  @Override
  public abstract void execute()
      throws Exception;

  public PlanNodeBean getPlanNode() {
    return planNode;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  protected void setOutput(String key, final DetectionPipelineResult output) {
    if (outputKeyMap.containsKey(key)) {
      key = outputKeyMap.get(key);
    }
    resultMap.put(key, output);
  }

  @Override
  public void setProperty(final String key, final Object value) {
    planNode.getParams().put(key, value);
  }

  @Override
  public DetectionPipelineResult getOutput(final String key) {
    return resultMap.get(key);
  }

  @Override
  public Map<String, DetectionPipelineResult> getOutputs() {
    return resultMap;
  }

  @Override
  public void setInput(final String key, final DetectionPipelineResult input) {
    inputMap.put(key, input);
  }
}
