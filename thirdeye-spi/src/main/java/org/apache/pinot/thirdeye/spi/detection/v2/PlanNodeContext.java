package org.apache.pinot.thirdeye.spi.detection.v2;

import java.util.Map;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;

public class PlanNodeContext {

  private String name;
  private Map<String, PlanNode> pipelinePlanNodes;
  private PlanNodeBean planNodeBean;
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

  public PlanNodeBean getDetectionPlanApi() {
    return planNodeBean;
  }

  public PlanNodeContext setDetectionPlanApi(final PlanNodeBean planNodeBean) {
    this.planNodeBean = planNodeBean;
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
