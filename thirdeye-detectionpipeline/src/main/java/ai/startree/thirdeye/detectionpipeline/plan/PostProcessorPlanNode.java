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
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.detectionpipeline.Operator;
import ai.startree.thirdeye.detectionpipeline.PlanNodeContext;
import ai.startree.thirdeye.detectionpipeline.PostProcessorRegistry;
import ai.startree.thirdeye.detectionpipeline.operator.AnomalyDetectorOperator;
import ai.startree.thirdeye.detectionpipeline.operator.PostProcessorOperator;
import ai.startree.thirdeye.spi.datalayer.TemplatableMap;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import java.util.Map;

public class PostProcessorPlanNode extends DetectionPipelinePlanNode {

  public static final String TYPE = "PostProcessor";
  private PostProcessorRegistry postProcessorRegistry;

  public PostProcessorPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
    for (final InputBean input : getPlanNodeInputs()) {
      // an input without source/target property defaults to the default output of an AnomalyDetectorOperator
      if (input.getSourceProperty() == null && input.getTargetProperty() == null) {
        input.setSourceProperty(AnomalyDetectorOperator.DEFAULT_OUTPUT_KEY);
        input.setTargetProperty(AnomalyDetectorOperator.DEFAULT_OUTPUT_KEY);
      }
    }

    postProcessorRegistry = planNodeContext.getApplicationContext().getPostProcessorRegistry();
    requireNonNull(postProcessorRegistry, "PostProcessorRegistry is not set");
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Map<String, Object> getParams() {
    return optional(planNodeBean.getParams()).map(TemplatableMap::valueMap).orElse(null);
  }

  @Override
  public Operator buildOperator() throws Exception {
    final PostProcessorOperator postProcessorOperator = new PostProcessorOperator();
    postProcessorOperator.init(createOperatorContext()
        .setDetectionInterval(this.detectionInterval)
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean));

    return postProcessorOperator;
  }
}
