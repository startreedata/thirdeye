/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.rca.contributors.cube.cost;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Map;

/**
 * This cost function consider change difference, change changeRatio, and node size (contribution
 * percentage of a node).
 * More for details: {@link CostFunction#computeCost(double, double, double, double, double, double,
 * double, double, double)}.
 */
public class BalancedCostFunction implements CostFunction {

  @VisibleForTesting
  protected static final String CHANGE_CONTRIBUTION_THRESHOLD_PARAM = "threshold";
  private static final double FLOATING_POINT_ERROR_EPSILON = 0.00001;
  // The threshold to the contribution to overall changes in percentage
  private double changeContributionThreshold = 3;

  public BalancedCostFunction() {
  }

  public BalancedCostFunction(Map<String, String> params) {
    if (params.containsKey(CHANGE_CONTRIBUTION_THRESHOLD_PARAM)) {
      String pctThresholdString = params.get(CHANGE_CONTRIBUTION_THRESHOLD_PARAM);
      Preconditions.checkArgument(!Strings.isNullOrEmpty(pctThresholdString));
      this.changeContributionThreshold = Double.parseDouble(pctThresholdString);
    }
  }

  @VisibleForTesting
  protected double getChangeContributionThreshold() {
    return changeContributionThreshold;
  }

  /**
   * Returns the cost that consider change difference, change changeRatio, and node size
   * (contribution percentage of a node).
   *
   * In brief, this function uses this formula to compute the cost:
   * change difference * log(contribution percentage * change changeRatio)
   *
   * In addition, if a node's contribution to overall changes is smaller than the threshold, which
   * is defined when
   * constructing this class, then the cost is always zero.
   *
   * @param parentChangeRatio the changeRatio between baseline and current value of parent node.
   * @param baselineValue the baseline value of the current node.
   * @param currentValue the current value of the current node.
   * @param topBaselineValue the baseline value of the top node.
   * @param topCurrentValue the current value of the top node.
   * @return the cost that consider change difference, change changeRatio, and node size.
   */
  @Override
  public double computeCost(double parentChangeRatio, double baselineValue, double currentValue,
      double baselineSize,
      double currentSize, double topBaselineValue, double topCurrentValue, double topBaselineSize,
      double topCurrentSize) {

    // Typically, users don't care nodes with small contribution to overall changes
    double contributionToOverallChange =
        Math.abs((currentValue - baselineValue) / (topCurrentValue - topBaselineValue));
    if (Double.compare(contributionToOverallChange, changeContributionThreshold / 100d) < 0) {
      return 0d;
    }
    // Contribution is the size of the node
    double contribution = (baselineSize + currentSize) / (topBaselineSize + topCurrentSize);
    // fixme cyril chekState here - contribution can be nan ?
    contribution = correctFloatingPointArithmeticError(contribution);
    Preconditions
        .checkState(Double.compare(contribution, 0) >= 0, "Contribution {} is smaller than 0.",
            contribution);
    Preconditions
        .checkState(Double.compare(contribution, 1) <= 0, "Contribution {} is larger than 1",
            contribution);
    // The cost function considers change difference, change changeRatio, and node size (i.e., contribution)
    return fillEmptyValuesAndGetError(baselineValue, currentValue, parentChangeRatio, contribution);
  }

  @VisibleForTesting
  protected static double correctFloatingPointArithmeticError(double contribution) {
    // fixme cyril no need to round if contribution is in [0,1] - see checks after call to this function
    if (Math.abs(0d - contribution) < FLOATING_POINT_ERROR_EPSILON) {
      contribution = 0d;
    } else if (Math.abs(1d - contribution) < FLOATING_POINT_ERROR_EPSILON) {
      contribution = 1d;
    }
    return contribution;
  }

  private static double error(double baselineValue, double currentValue, double parentRatio,
      double contribution) {
    double expectedBaselineValue = parentRatio * baselineValue;
    double expectedRatio = currentValue / expectedBaselineValue;
    double weightedExpectedRatio = (expectedRatio - 1) * contribution + 1;
    double logExpRatio = Math.log(weightedExpectedRatio);
    double cost = (currentValue - expectedBaselineValue) * logExpRatio;
    return cost;
  }

  private static double errorWithEmptyBaseline(double currentValue, double parentRatio) {
    if (Double.compare(parentRatio, 1) < 0) {
      parentRatio = 2 - parentRatio;
    }
    double logExpRatio = Math.log(parentRatio);
    double cost = currentValue * logExpRatio;
    return cost;
  }

  private static double errorWithEmptyCurrent(double baseline, double parentRatio) {
    if (Double.compare(parentRatio, 1) > 0) {
      parentRatio = 2 - parentRatio;
    }
    double logExpRatio = Math.log(parentRatio);
    double cost = -baseline * logExpRatio;
    return cost;
  }

  /**
   * Auto fill in baselineValue and currentValue using parentRatio when one of them is zero.
   * If baselineValue and currentValue both are zero or parentRatio is not finite, this function
   * returns 0.
   */
  private static double fillEmptyValuesAndGetError(double baselineValue, double currentValue,
      double parentRatio,
      double contribution) {
    if (Double.compare(0., parentRatio) == 0 || Double.isNaN(parentRatio)) {
      parentRatio = 1d;
    }
    if (Double.compare(0., baselineValue) != 0 && Double.compare(0., currentValue) != 0) {
      return error(baselineValue, currentValue, parentRatio, contribution);
    } else if (Double.compare(baselineValue, 0d) == 0 || Double.compare(currentValue, 0d) == 0) {
      if (Double.compare(0., baselineValue) == 0) {
        return errorWithEmptyBaseline(currentValue, parentRatio);
      } else {
        return errorWithEmptyCurrent(baselineValue, parentRatio);
      }
    } else { // baselineValue and currentValue are zeros. Set cost to zero so the node will be naturally aggregated to its parent.
      return 0.;
    }
  }
}
