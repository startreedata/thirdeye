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

import java.time.Duration;

public class DelayOperator extends DetectionPipelineOperator{

  private static final String DELAY_TIME = "delayTime";
  @Override
  public void execute() throws Exception {
    long sleepTime = Duration.parse(getPlanNode().getParams().get(DELAY_TIME).toString()).toMillis();
    Thread.sleep(sleepTime);
    inputMap.forEach(this::setOutput);
  }

  @Override
  public String getOperatorName() {
    return "DelayOperator";
  }
}
