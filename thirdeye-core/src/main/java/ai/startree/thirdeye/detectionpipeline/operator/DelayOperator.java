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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelayOperator extends DetectionPipelineOperator{

  private static final Logger LOG = LoggerFactory.getLogger(DelayOperator.class);

  @Override
  public void execute() throws Exception {
    long delayTime = Long.parseLong(getPlanNode().getParams().get("delayTime").toString());
    int randomOffset = Integer.parseInt(getPlanNode().getParams().get("randomOffset").toString());
    boolean fixedDelay = Boolean.parseBoolean(getPlanNode().getParams().get("isFixedDelay").toString());
    long sleepTime = delayTime + (fixedDelay ? 0 : new Random().nextInt(randomOffset));
    Thread.sleep(sleepTime);
    inputMap.forEach(this::setOutput);
  }

  @Override
  public String getOperatorName() {
    return "DelayOperator";
  }
}
