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
package ai.startree.thirdeye.cube.cost;

public interface CostFunction {

  /**
   * Calculates the error cost of a node when it is inserted back to it's parent.
   *
   * @param parentChangeRatio change ratio of the parent node.
   * @param baselineValue the baseline value of the node.
   * @param currentValue the current value of the node.
   * @param baselineSize the baseline size of the node.
   * @param currentSize the current size of the node.
   * @param topBaselineValue the baseline value of the root node.
   * @param topCurrentValue the current value of the root node.
   * @param topBaselineSize the baseline size of root node.
   * @param topCurrentSize the current size of root node.
   * @return the error cost of the current node.
   */
  // TODO: Change to take as input nodes instead of values
  double computeCost(double parentChangeRatio,
      double baselineValue,
      double currentValue,
      double baselineSize,
      double currentSize,
      double topBaselineValue,
      double topCurrentValue,
      double topBaselineSize,
      double topCurrentSize);
}
