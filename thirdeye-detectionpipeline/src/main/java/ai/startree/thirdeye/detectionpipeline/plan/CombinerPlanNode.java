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
package ai.startree.thirdeye.detectionpipeline.plan;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Collections.emptyMap;

import ai.startree.thirdeye.detectionpipeline.Operator;
import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.detectionpipeline.operator.CombinerOperator;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import java.util.Map;

public class CombinerPlanNode extends DetectionPipelinePlanNode {

  public static final String TYPE = "Combiner";
  private Map<String, Object> params;

  public CombinerPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
    params = optional(planNodeBean.getParams()).map(TemplatableMap::valueMap).orElse(emptyMap());
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
    final CombinerOperator operator = new CombinerOperator();
    operator.init(createOperatorContext()
        .setDetectionInterval(detectionInterval)
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean)
    );
    return operator;
  }
}
