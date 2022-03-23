/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.ratio;

import ai.startree.thirdeye.cube.data.cube.CubeUtils;
import ai.startree.thirdeye.cube.data.node.BaseCubeNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.math.DoubleMath;

/**
 * A CubeNode for ratio metrics such as "observed over expected ratio".
 */
public class RatioCubeNode extends BaseCubeNode<RatioCubeNode, RatioRow> {

  private static final double epsilon = 0.0001;

  private double baselineNumeratorValue;
  private double currentNumeratorValue;
  private double baselineDenominatorValue;
  private double currentDenominatorValue;

  /**
   * Constructs a root CubeNode whose level and index is 0 and parent pointer is null.
   *
   * @param data the data of this root node.
   */
  public RatioCubeNode(RatioRow data) {
    super(data);
    resetValues();
  }

  /**
   * Constructs a CubeNode which is specified information.
   *
   * @param level the level of this node.
   * @param index the index of this node that is located in its parent's children list.
   * @param data the data of this node.
   * @param parent the parent of this node.
   */
  public RatioCubeNode(int level, int index, RatioRow data, RatioCubeNode parent) {
    super(level, index, data, parent);
    resetValues();
  }

  @Override
  public void resetValues() {
    this.baselineNumeratorValue = data.getBaselineNumeratorValue();
    this.currentNumeratorValue = data.getCurrentNumeratorValue();
    this.baselineDenominatorValue = data.getBaselineDenominatorValue();
    this.currentDenominatorValue = data.getCurrentDenominatorValue();
  }

  @Override
  public void removeNodeValues(RatioCubeNode node) {
    baselineNumeratorValue = CubeUtils
        .doubleMinus(baselineNumeratorValue, node.baselineNumeratorValue);
    currentNumeratorValue = CubeUtils
        .doubleMinus(currentNumeratorValue, node.currentNumeratorValue);
    baselineDenominatorValue = CubeUtils
        .doubleMinus(baselineDenominatorValue, node.baselineDenominatorValue);
    currentDenominatorValue = CubeUtils
        .doubleMinus(currentDenominatorValue, node.currentDenominatorValue);
    Preconditions.checkArgument(!(DoubleMath.fuzzyCompare(baselineNumeratorValue, 0, epsilon) < 0
        || DoubleMath.fuzzyCompare(currentNumeratorValue, 0, epsilon) < 0
        || DoubleMath.fuzzyCompare(baselineDenominatorValue, 0, epsilon) < 0
        || DoubleMath.fuzzyCompare(currentDenominatorValue, 0, epsilon) < 0));
  }

  @Override
  public void addNodeValues(RatioCubeNode node) {
    this.baselineNumeratorValue += node.baselineNumeratorValue;
    this.currentNumeratorValue += node.currentNumeratorValue;
    this.baselineDenominatorValue += node.baselineDenominatorValue;
    this.currentDenominatorValue += node.currentDenominatorValue;
  }

  @Override
  public double getBaselineSize() {
    return baselineNumeratorValue + baselineDenominatorValue;
  }

  @Override
  public double getCurrentSize() {
    return currentNumeratorValue + currentDenominatorValue;
  }

  @Override
  public double getOriginalBaselineSize() {
    return data.getBaselineNumeratorValue() + data.getBaselineDenominatorValue();
  }

  @Override
  public double getOriginalCurrentSize() {
    return data.getCurrentNumeratorValue() + data.getCurrentDenominatorValue();
  }

  /**
   * Calculates the value of the given numerator and denominator.
   * If denominator is non-zero, then return (numerator / denominator);
   * If both numerator and denominator are zero, then return 0.
   * If only denominator is zero, then return (numerator / node size).
   *
   * @param numerator the numerator.
   * @param denominator the denominator.
   * @return the value of the given numerator and denominator.
   */
  private double calculateValue(double numerator, double denominator) {
    if (!DoubleMath.fuzzyEquals(denominator, 0, epsilon)) {
      return numerator / denominator;
    } else if (DoubleMath.fuzzyEquals(numerator, 0, epsilon)) {
      return 0d; // Let the algorithm to handle this case as a missing value.
    } else {
      // Divide the numerator value by node size to prevent large change diff.
      return numerator / (getCurrentSize() + getBaselineSize());
    }
  }

  @Override
  public double getBaselineValue() {
    return calculateValue(baselineNumeratorValue, baselineDenominatorValue);
  }

  @Override
  public double getCurrentValue() {
    return calculateValue(currentNumeratorValue, currentDenominatorValue);
  }

  @Override
  public double getOriginalBaselineValue() {
    return data.getBaselineNumeratorValue() / data.getBaselineDenominatorValue();
  }

  @Override
  public double getOriginalCurrentValue() {
    return data.getCurrentNumeratorValue() / data.getCurrentDenominatorValue();
  }

  @Override
  public double originalChangeRatio() {
    return (data.getCurrentNumeratorValue() / data.getCurrentDenominatorValue()) / (
        data.getBaselineNumeratorValue() / data.getBaselineDenominatorValue());
  }

  @Override
  public double changeRatio() {
    return (currentNumeratorValue / currentDenominatorValue) / (baselineNumeratorValue
        / baselineDenominatorValue);
  }

  @Override
  public boolean side() {
    double currentValue = getCurrentValue();
    double baselineValue = getBaselineValue();
    if (!DoubleMath.fuzzyEquals(currentValue, 0, epsilon) && !DoubleMath
        .fuzzyEquals(baselineValue, 0, epsilon)) {
      // The most common case is located first in order to reduce performance impact
      return DoubleMath.fuzzyCompare(currentValue, baselineValue, epsilon) >= 0;
    } else {
      if (parent != null) {
        if (DoubleMath.fuzzyEquals(currentValue, 0, epsilon) && DoubleMath
            .fuzzyEquals(baselineValue, 0, epsilon)) {
          return parent.side();
        } else if (DoubleMath.fuzzyEquals(currentValue, 0, epsilon)) {
          return DoubleMath.fuzzyCompare(baselineValue, parent.getBaselineValue(), epsilon) < 0;
        } else { //if (DoubleMath.fuzzyEquals(baselineValue, 0, epsilon)) {
          return DoubleMath.fuzzyCompare(currentValue, parent.getCurrentValue(), epsilon) >= 0;
        }
      } else {
        return DoubleMath.fuzzyCompare(currentValue, baselineValue, epsilon) >= 0;
      }
    }
  }

  /**
   * Returns the baseline numerator value.
   *
   * @return the baseline numerator value.
   */
  public double getBaselineNumeratorValue() {
    return baselineNumeratorValue;
  }

  /**
   * Returns the baseline denominator value.
   *
   * @return the baseline denominator value.
   */
  public double getBaselineDenominatorValue() {
    return baselineDenominatorValue;
  }

  /**
   * Returns the current numerator value.
   *
   * @return the current numerator value.
   */
  public double getCurrentNumeratorValue() {
    return currentNumeratorValue;
  }

  /**
   * Returns the current denominator value.
   *
   * @return the current denominator value.
   */
  public double getCurrentDenominatorValue() {
    return currentDenominatorValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RatioCubeNode)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    RatioCubeNode that = (RatioCubeNode) o;
    return Double.compare(that.baselineNumeratorValue, baselineNumeratorValue) == 0
        && Double.compare(that.currentNumeratorValue, currentNumeratorValue) == 0
        && Double.compare(that.baselineDenominatorValue, baselineDenominatorValue) == 0
        && Double.compare(that.currentDenominatorValue, currentDenominatorValue) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), baselineNumeratorValue, currentNumeratorValue,
        baselineDenominatorValue,
        currentDenominatorValue);
  }

  /**
   * ToString that handles if the given cube node is null, i.e., a root cube node. Moreover, it does
   * not invoke
   * parent's toString() to prevent multiple calls of toString to their parents.
   *
   * @param node the node to be converted to string.
   * @return a simple string representation of a parent cube node, which does not toString its
   *     parent node recursively.
   */
  private String toStringAsParent(RatioCubeNode node) {
    if (node == null) {
      return "null";
    } else {
      return MoreObjects.toStringHelper(this)
          .add("level", level)
          .add("index", index)
          .add("baselineNumeratorValue", baselineNumeratorValue)
          .add("baselineDenominatorValue", baselineDenominatorValue)
          .add("currentNumeratorValue", currentNumeratorValue)
          .add("currentDenominatorValue", currentDenominatorValue)
          .add("cost", cost)
          .add("data", data)
          .toString();
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("level", level)
        .add("index", index)
        .add("baselineNumeratorValue", baselineNumeratorValue)
        .add("baselineDenominatorValue", baselineDenominatorValue)
        .add("currentNumeratorValue", currentNumeratorValue)
        .add("currentDenominatorValue", currentDenominatorValue)
        .add("cost", cost)
        .add("data", data)
        .add("parent", toStringAsParent(parent))
        .toString();
  }
}
