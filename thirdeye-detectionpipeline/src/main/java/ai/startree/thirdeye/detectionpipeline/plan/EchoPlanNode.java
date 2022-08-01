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

import ai.startree.thirdeye.detectionpipeline.operator.EchoOperator;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.detection.v2.Operator;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.PlanNodeContext;
import java.util.Map;

public class EchoPlanNode extends DetectionPipelinePlanNode {

  public static final String TYPE = "Echo";

  public EchoPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Map<String, Templatable<Object>> getParams() {
    return planNodeBean.getParams();
  }

  @Override
  public Operator buildOperator() throws Exception {
    final EchoOperator operator = new EchoOperator();
    operator.init(new OperatorContext()
        .setDetectionInterval(this.detectionInterval)
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean)
    );
    return operator;
  }
}
