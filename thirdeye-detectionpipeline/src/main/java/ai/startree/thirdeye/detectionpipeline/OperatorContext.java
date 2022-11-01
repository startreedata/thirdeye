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
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.List;
import java.util.Map;
import org.joda.time.Interval;

public class OperatorContext {

  private PlanNodeContext planNodeContext;
  private Interval detectionInterval;
  private List<Predicate> predicates;
  private PlanNodeBean planNode;
  private Map<String, Object> properties;
  private Map<String, OperatorResult> inputsMap;
  private EnumerationItemDTO enumerationItem;

  public PlanNodeContext getPlanNodeContext() {
    return planNodeContext;
  }

  public OperatorContext setPlanNodeContext(
      final PlanNodeContext planNodeContext) {
    this.planNodeContext = planNodeContext;
    return this;
  }

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

  public Map<String, OperatorResult> getInputsMap() {
    return inputsMap;
  }

  public OperatorContext setInputsMap(final Map<String, OperatorResult> inputsMap) {
    this.inputsMap = inputsMap;
    return this;
  }

  public List<Predicate> getPredicates() {
    return predicates;
  }

  public OperatorContext setPredicates(
      final List<Predicate> predicates) {
    this.predicates = predicates;
    return this;
  }

  public EnumerationItemDTO getEnumerationItem() {
    return enumerationItem;
  }

  public OperatorContext setEnumerationItem(
      final EnumerationItemDTO enumerationItem) {
    this.enumerationItem = enumerationItem;
    return this;
  }
}
