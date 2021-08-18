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
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.pinot.thirdeye.detection.v2.utils.DefaultTimeConverter;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;
import org.apache.pinot.thirdeye.spi.detection.TimeConverter;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DetectionPipeline forms the root of the detection class hierarchy. It represents a wireframe
 * for implementing (intermittently stateful) executable pipelines on top of it.
 */
public abstract class DetectionPipelineOperator implements
    Operator {

  protected static final String PROP_TYPE = "type";
  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelineOperator.class);

  protected PlanNodeBean planNode;
  protected long startTime;
  protected long endTime;
  protected TimeConverter timeConverter;
  protected String timeFormat = OperatorContext.DEFAULT_TIME_FORMAT;
  protected Map<String, DetectionPipelineResult> resultMap = new HashMap<>();
  protected Map<String, BaseComponent> instancesMap = new HashMap<>();
  protected Map<String, DetectionPipelineResult> inputMap;
  protected Map<String, String> outputKeyMap = new HashMap<>();

  protected DetectionPipelineOperator() {
  }

  @Override
  public void init(final OperatorContext context) {
    this.planNode = context.getPlanNode();
    this.timeFormat = context.getTimeFormat();
    this.timeConverter = DefaultTimeConverter.get(timeFormat);
    this.startTime = optional(context.getStartTime()).map(timeConverter::convert).orElse(-1L);
    this.endTime = optional(context.getEndTime()).map(timeConverter::convert).orElse(-1L);
    checkArgument(startTime <= endTime, "start time cannot be greater than end time");

    this.resultMap = new HashMap<>();
    this.instancesMap = new HashMap<>();
    this.inputMap = context.getInputsMap();
    if (context.getPlanNode().getOutputs() != null) {
      for (OutputBean outputBean : context.getPlanNode().getOutputs()) {
        outputKeyMap.put(outputBean.getOutputKey(), outputBean.getOutputName());
      }
    }
    this.initComponents();
  }

  /**
   * Returns a detection result for the time range between {@code startTime} and {@code endTime}.
   *
   * @return detection result
   */
  public abstract void execute()
      throws Exception;

  /**
   * Initialize all components in the pipeline
   */
  protected void initComponents() {
    Map<String, Object> componentSpecs = getComponentSpecs(planNode.getParams());
    if (componentSpecs != null) {
      for (String componentKey : componentSpecs.keySet()) {
        Map<String, Object> componentSpec = ConfigUtils.getMap(componentSpecs.get(componentKey));
        if (!instancesMap.containsKey(componentKey)) {
          instancesMap.put(componentKey, createComponent(componentSpec));
        }
      }
    }
  }

  protected Map<String, Object> getComponentSpecs(Map<String, Object> params) {
    Map<String, Object> componentSpecs = new HashMap<>();
    if (params != null) {
      for (String key : params.keySet()) {
        final String[] splits = key.split("\\.");
        if (splits.length > 2 && "component".equalsIgnoreCase(splits[0])) {
          String componentKey = splits[1];
          if (!componentSpecs.containsKey(componentKey)) {
            componentSpecs.put(componentKey, new HashMap<>());
          }
          final Map<String, Object> componentSpec = (Map<String, Object>) componentSpecs.get(
              componentKey);
          componentSpec.put(StringUtils.join(Arrays.copyOfRange(splits, 2, splits.length), "."),
              params.get(key));
        }
      }
    }
    return componentSpecs;
  }

  protected BaseComponent createComponent(Map<String, Object> componentSpec) {
    throw new UnsupportedOperationException(
        "Component Initialization is optional and should be provided by downstream implementations");
  }

  public PlanNodeBean getPlanNode() {
    return planNode;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public String getTimeFormat() {
    return timeFormat;
  }

  protected void setOutput(String key, DetectionPipelineResult output) {
    if (outputKeyMap.containsKey(key)) {
      key = outputKeyMap.get(key);
    }
    resultMap.put(key, output);
  }

  @Override
  public void setProperty(String key, Object value) {
    planNode.getParams().put(key, value);
  }

  public Map<String, BaseComponent> getComponents() {
    return instancesMap;
  }

  @Override
  public DetectionPipelineResult getOutput(String key) {
    return resultMap.get(key);
  }

  @Override
  public Map<String, DetectionPipelineResult> getOutputs() {
    return resultMap;
  }

  @Override
  public void setInput(String key, DetectionPipelineResult input) {
    this.inputMap.put(key, input);
  }
}
