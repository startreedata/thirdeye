package org.apache.pinot.thirdeye.detection.v2;

import java.util.Map;
import org.apache.pinot.thirdeye.api.v2.DetectionPlanApi;

public class PlanNodeContext {

  private String name;
  private Map<String, PlanNode> pipelinePlanNodes;
  private DetectionPlanApi detectionPlanApi;
  private long startTime;
  private long endTime;
  private Map<String, Object> properties;

  public String getName() {
    return name;
  }

  public PlanNodeContext setName(final String name) {
    this.name = name;
    return this;
  }

  public Map<String, PlanNode> getPipelinePlanNodes() {
    return pipelinePlanNodes;
  }

  public PlanNodeContext setPipelinePlanNodes(final Map<String, PlanNode> pipelinePlanNodes) {
    this.pipelinePlanNodes = pipelinePlanNodes;
    return this;
  }

  public DetectionPlanApi getDetectionPlanApi() {
    return detectionPlanApi;
  }

  public PlanNodeContext setDetectionPlanApi(final DetectionPlanApi detectionPlanApi) {
    this.detectionPlanApi = detectionPlanApi;
    return this;
  }

  public long getStartTime() {
    return startTime;
  }

  public PlanNodeContext setStartTime(final long startTime) {
    this.startTime = startTime;
    return this;
  }

  public long getEndTime() {
    return endTime;
  }

  public PlanNodeContext setEndTime(final long endTime) {
    this.endTime = endTime;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public PlanNodeContext setProperties(final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }
}
