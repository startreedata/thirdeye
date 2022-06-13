/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection.v2;

import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import java.util.Map;
import org.joda.time.Interval;

public class OperatorContext {
  private Interval detectionInterval;
  private PlanNodeBean planNode;
  private Map<String, Object> properties;
  private Map<String, DetectionPipelineResult> inputsMap;

  public Interval getDetectionInterval() {
    return detectionInterval;
  }

  public OperatorContext setDetectionInterval(final Interval detectionInterval) {
    this.detectionInterval = detectionInterval;
    return this;
  }

  public PlanNodeBean getPlanNode() {
    return planNode;
  }

  public OperatorContext setPlanNode(final PlanNodeBean planNodeBean) {
    this.planNode = planNodeBean;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public OperatorContext setProperties(final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public Map<String, DetectionPipelineResult> getInputsMap() {
    return inputsMap;
  }

  public OperatorContext setInputsMap(final Map<String, DetectionPipelineResult> inputsMap) {
    this.inputsMap = inputsMap;
    return this;
  }
}
