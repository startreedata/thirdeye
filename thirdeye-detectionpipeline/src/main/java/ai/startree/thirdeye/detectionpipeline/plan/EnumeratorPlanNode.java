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
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.DetectionRegistry;
import ai.startree.thirdeye.detectionpipeline.Operator;
import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.detectionpipeline.operator.EnumeratorOperator;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

public class EnumeratorPlanNode extends DetectionPipelinePlanNode {

  public static final String TYPE = "Enumerator";
  private Map<String, Object> params;
  private DetectionRegistry detectionRegistry;
  private EnumerationItemDTO enumerationItem;

  public EnumeratorPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
    detectionRegistry = (DetectionRegistry) planNodeContext.getProperties()
        .get(Constants.K_DETECTION_REGISTRY);
    requireNonNull(detectionRegistry, "DetectionRegistry is not set");

    params = optional(planNodeBean.getParams()).map(TemplatableMap::valueMap).orElse(emptyMap());
    enumerationItem = planNodeContext.getEnumerationItem();
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Map<String, Object> getParams() {
    return params;
  }

  @Override
  public Operator buildOperator() throws Exception {
    final EnumeratorOperator operator = new EnumeratorOperator();
    operator.init(createOperatorContext()
        .setDetectionInterval(detectionInterval)
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean)
        .setProperties(ImmutableMap.of(Constants.K_DETECTION_REGISTRY, detectionRegistry))
        .setEnumerationItem(enumerationItem)
    );
    return operator;
  }
}
