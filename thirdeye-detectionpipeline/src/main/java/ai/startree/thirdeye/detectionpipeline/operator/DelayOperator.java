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
package ai.startree.thirdeye.detectionpipeline.operator;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import java.time.Duration;

public class DelayOperator extends DetectionPipelineOperator{

  private static final String DELAY_TIME = "delayTime";
  @Override
  public void execute() throws Exception {
    final long sleepTime = optional(getPlanNode().getParams().get(DELAY_TIME))
        .map(Templatable::value)
        .map(Object::toString)
        .map(text -> Duration.parse(text).toMillis())
        .orElse(0L);
    Thread.sleep(sleepTime);
    inputMap.forEach(this::setOutput);
  }

  @Override
  public String getOperatorName() {
    return "DelayOperator";
  }
}
