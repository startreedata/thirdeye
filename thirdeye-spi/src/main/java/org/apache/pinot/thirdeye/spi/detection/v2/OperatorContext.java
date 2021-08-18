package org.apache.pinot.thirdeye.spi.detection.v2;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.util.SpiUtils.TimeFormat;

public class OperatorContext {
  public static final String DEFAULT_TIME_FORMAT = TimeFormat.EPOCH + ":" + TimeUnit.MILLISECONDS;

  private String timeFormat = DEFAULT_TIME_FORMAT;
  private String startTime;
  private String endTime;
  private PlanNodeBean planNode;
  private Map<String, Object> properties;
  private Map<String, DetectionPipelineResult> inputsMap;

  public String getStartTime() {
    return startTime;
  }

  public OperatorContext setStartTime(final String startTime) {
    this.startTime = startTime;
    return this;
  }

  public String getEndTime() {
    return endTime;
  }

  public OperatorContext setEndTime(final String endTime) {
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

  public String getTimeFormat() {
    return timeFormat;
  }

  public OperatorContext setTimeFormat(final String timeFormat) {
    this.timeFormat = timeFormat;
    return this;
  }
}
