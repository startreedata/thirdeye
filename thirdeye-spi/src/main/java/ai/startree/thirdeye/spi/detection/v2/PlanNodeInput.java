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
package ai.startree.thirdeye.spi.detection.v2;

/**
 * Defines an input for any PlanNode, with plan node name and all the keys of outputs.
 */
public class PlanNodeInput {
  private String inputKey;
  private String nodeName;
  private String outputKey;

  public String getInputKey() {
    return inputKey;
  }

  public PlanNodeInput setInputKey(final String inputKey) {
    this.inputKey = inputKey;
    return this;
  }

  public String getNodeName() {
    return nodeName;
  }

  public PlanNodeInput setNodeName(final String nodeName) {
    this.nodeName = nodeName;
    return this;
  }

  public String getOutputKey() {
    return outputKey;
  }

  public PlanNodeInput setOutputKey(final String outputKey) {
    this.outputKey = outputKey;
    return this;
  }
}
