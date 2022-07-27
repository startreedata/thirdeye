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

import java.util.Random;

public class DelayOperator extends DetectionPipelineOperator{

  private static final String DELAY_TIME = "delayTime";
  private static final String RANDOM_OFFSET = "randomOffset";
  private static final String ID_FIXED_DELAY = "isFixedDelay";
  @Override
  public void execute() throws Exception {
    long delayTime = Long.parseLong(getPlanNode().getParams().get(DELAY_TIME).toString());
    int randomOffset = Integer.parseInt(getPlanNode().getParams().get(RANDOM_OFFSET).toString());
    boolean fixedDelay = Boolean.parseBoolean(getPlanNode().getParams().get(ID_FIXED_DELAY).toString());
    long sleepTime = delayTime + (fixedDelay ? 0 : new Random().nextInt(randomOffset));
    Thread.sleep(sleepTime);
    inputMap.forEach(this::setOutput);
  }

  @Override
  public String getOperatorName() {
    return "DelayOperator";
  }
}
