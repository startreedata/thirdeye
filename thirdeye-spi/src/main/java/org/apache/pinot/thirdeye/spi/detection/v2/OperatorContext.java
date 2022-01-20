package org.apache.pinot.thirdeye.spi.detection.v2;

import java.util.Map;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;

public class OperatorContext {
  private long startTime;
  private long endTime;
  private PlanNodeBean planNode;
  private Map<String, Object> properties;
  private Map<String, DetectionPipelineResult> inputsMap;

  public long getStartTime() {
    return startTime;
  }

  public OperatorContext setStartTime(final long startTime) {
    this.startTime = startTime;
    return this;
  }

  public long getEndTime() {
    return endTime;
  }

  public OperatorContext setEndTime(final long endTime) {
    this.endTime = endTime;
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
