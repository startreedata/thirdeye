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

import ai.startree.thirdeye.detectionpipeline.Operator;
import ai.startree.thirdeye.detectionpipeline.OperatorContext;
import ai.startree.thirdeye.detectionpipeline.operator.TimeIndexFillerOperator;

public class IndexFillerPlanNode extends DetectionPipelinePlanNode {

  @Override
  public String getType() {
    return "TimeIndexFiller";
  }

  @Override
  public Operator buildOperator() throws Exception {
    final TimeIndexFillerOperator timeIndexFillerOperator = new TimeIndexFillerOperator();
    timeIndexFillerOperator.init(new OperatorContext()
        .setDetectionInterval(this.detectionInterval)
        .setPlanNode(planNodeBean)
        .setInputsMap(inputsMap)
    );
    return timeIndexFillerOperator;
  }
}
