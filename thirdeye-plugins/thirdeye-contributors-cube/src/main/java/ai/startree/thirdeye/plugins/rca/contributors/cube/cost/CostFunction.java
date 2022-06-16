/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.rca.contributors.cube.cost;

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
