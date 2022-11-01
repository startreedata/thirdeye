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
package ai.startree.thirdeye.detectionpipeline;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import java.util.List;
import java.util.Map;
import org.joda.time.Interval;

public class PlanNodeContext {

  private ApplicationContext applicationContext;
  private String name;
  private Map<String, PlanNode> pipelinePlanNodes;
  private PlanNodeBean planNodeBean;
  private Interval detectionInterval;
  private List<Predicate> predicates;
  private EnumerationItemDTO enumerationItem;


  public static PlanNodeContext copy(final PlanNodeContext toCopy) {
    // shallow copy
    final PlanNodeContext copy = new PlanNodeContext();
    copy.name = toCopy.name;
    copy.pipelinePlanNodes = toCopy.pipelinePlanNodes;
    copy.planNodeBean = toCopy.planNodeBean;
    copy.detectionInterval = toCopy.detectionInterval;
    copy.predicates = toCopy.predicates;
    copy.applicationContext = toCopy.applicationContext;

    return copy;
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  public PlanNodeContext setApplicationContext(
      final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    return this;
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
    return detectionInterval;
  }

  public PlanNodeContext setDetectionInterval(final Interval detectionInterval) {
    this.detectionInterval = detectionInterval;
    return this;
  }

  public List<Predicate> getPredicates() {
    return predicates;
  }

  public PlanNodeContext setPredicates(final List<Predicate> predicates) {
    this.predicates = predicates;
    return this;
  }

  public EnumerationItemDTO getEnumerationItem() {
    return enumerationItem;
  }

  public PlanNodeContext setEnumerationItem(
      final EnumerationItemDTO enumerationItem) {
    this.enumerationItem = enumerationItem;
    return this;
  }
}
