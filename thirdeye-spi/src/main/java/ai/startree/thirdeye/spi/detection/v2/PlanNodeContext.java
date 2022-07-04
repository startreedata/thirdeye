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
package ai.startree.thirdeye.spi.detection.v2;

import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import java.util.Map;
import org.joda.time.Interval;

public class PlanNodeContext {

  private String name;
  private Map<String, PlanNode> pipelinePlanNodes;
  private PlanNodeBean planNodeBean;
  private Interval detectionInterval;
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

  public PlanNodeBean getPlanNodeBean() {
    return planNodeBean;
  }

  public PlanNodeContext setPlanNodeBean(final PlanNodeBean planNodeBean) {
    this.planNodeBean = planNodeBean;
    return this;
  }

  public Interval getDetectionInterval() {
    return detectionInterval;
  }

  public PlanNodeContext setDetectionInterval(final Interval detectionInterval) {
    this.detectionInterval = detectionInterval;
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
