/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.detectionpipeline.plan;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.PlanNode;
import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DetectionPipelineOperator forms the root of the detection class hierarchy. It represents a wireframe
 * for implementing (intermittently stateful) executable pipelines on top of it.
 */
public abstract class DetectionPipelinePlanNode implements PlanNode {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelinePlanNode.class);

  protected PlanNodeBean planNodeBean = null;
  protected Interval detectionInterval = null;
  protected Map<String, OperatorResult> inputsMap = new HashMap<>();

  private PlanNodeContext context;

  protected DetectionPipelinePlanNode() {
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    this.context = planNodeContext;

    this.planNodeBean = planNodeContext.getPlanNodeBean();
    this.detectionInterval = planNodeContext.getDetectionInterval();
  }

  @Override
  public PlanNodeContext getContext() {
    return requireNonNull(context, "node not initialized! " + getClass().getSimpleName());
  }

  public Interval getDetectionInterval() {
    return detectionInterval;
  }

  @Override
  public List<InputBean> getPlanNodeInputs() {
    return planNodeBean.getInputs();
  }

  public void setInput(String key, OperatorResult input) {
    this.inputsMap.put(key, input);
  }

  @Override
  public Map<String, Object> getParams() {
    return optional(planNodeBean.getParams()).map(TemplatableMap::valueMap).orElse(null);
  }

  @Override
  public String getName() {
    return getContext().getName();
  }
}
