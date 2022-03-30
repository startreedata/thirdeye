/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.plan;

import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.spi.detection.v2.PlanNode;
import ai.startree.thirdeye.spi.detection.v2.PlanNodeContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
