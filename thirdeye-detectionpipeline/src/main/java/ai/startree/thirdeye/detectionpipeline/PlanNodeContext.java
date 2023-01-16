/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.detectionpipeline;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import java.util.List;
import java.util.Map;
import org.joda.time.Interval;

public class PlanNodeContext {

  private DetectionPipelineContext detectionPipelineContext;
  private String name;
  private Map<String, PlanNode> pipelinePlanNodes;
  private PlanNodeBean planNodeBean;

  public static PlanNodeContext copy(final PlanNodeContext src) {
    return PlanNodeContextMapper.INSTANCE.clone(src);  // a new detectionPipelineContext  instance is created, the rest is shallow copy
  }

  public DetectionPipelineContext getDetectionPipelineContext() {
    return detectionPipelineContext;
  }

  public PlanNodeContext setDetectionPipelineContext(
      final DetectionPipelineContext detectionPipelineContext) {
    this.detectionPipelineContext = detectionPipelineContext;
    return this;
  }

  public ApplicationContext getApplicationContext() {
    return detectionPipelineContext.getApplicationContext();
  }

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

  public PlanNodeBean getPlanNodeBean() {
    return planNodeBean;
  }

  public PlanNodeContext setPlanNodeBean(final PlanNodeBean planNodeBean) {
    this.planNodeBean = planNodeBean;
    return this;
  }

  public Interval getDetectionInterval() {
    return detectionPipelineContext.getDetectionInterval();
  }

  public List<Predicate> getPredicates() {
    return detectionPipelineContext.getPredicates();
  }
}
