/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.ratio;

import ai.startree.thirdeye.cube.data.dbrow.BaseRow;
import ai.startree.thirdeye.cube.data.dbrow.DimensionValues;
import ai.startree.thirdeye.cube.data.dbrow.Dimensions;
import ai.startree.thirdeye.cube.data.node.CubeNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Stores the ratio metric that is returned from DB.
 */
public class RatioRow extends BaseRow {

  protected double baselineNumeratorValue;
  protected double currentNumeratorValue;
  protected double baselineDenominatorValue;
  protected double currentDenominatorValue;

  /**
   * Constructs an ratio row.
   *
   * @param dimensions the dimension names of this row.
   * @param dimensionValues the dimension values of this row.
   */
  public RatioRow(Dimensions dimensions, DimensionValues dimensionValues) {
    super(dimensions, dimensionValues);
    this.baselineNumeratorValue = 0.0;
    this.currentNumeratorValue = 0.0;
    this.baselineDenominatorValue = 0.0;
    this.currentDenominatorValue = 0.0;
  }

  /**
   * Constructs an ratio row.
   *
   * @param dimensions the dimension names of this row.
   * @param dimensionValues the dimension values of this row.
   * @param baselineNumeratorValue the baseline numerator of this ratio row.
   * @param baselineDenominatorValue the baseline denominator of this ratio row.
   * @param currentNumeratorValue the current numerator of this ratio row.
   * @param currentDenominatorValue the current denominator of this ratio row.
   */
  public RatioRow(Dimensions dimensions, DimensionValues dimensionValues,
      double baselineNumeratorValue,
      double baselineDenominatorValue, double currentNumeratorValue,
      double currentDenominatorValue) {
    super(dimensions, dimensionValues);
    this.baselineNumeratorValue = baselineNumeratorValue;
    this.baselineDenominatorValue = baselineDenominatorValue;
    this.currentNumeratorValue = currentNumeratorValue;
    this.currentDenominatorValue = currentDenominatorValue;
  }

  /**
   * Returns the baseline numerator of this ratio row.
   *
   * @return the baseline numerator of this ratio row.
   */
  public double getBaselineNumeratorValue() {
    return baselineNumeratorValue;
  }

  /**
   * Sets the baseline numerator value of this ratio row.
   *
   * @param baselineNumeratorValue the baseline numerator value of this ratio row.
   */
  public void setBaselineNumeratorValue(double baselineNumeratorValue) {
    this.baselineNumeratorValue = baselineNumeratorValue;
  }

  /**
   * Returns the current numerator of this ratio row.
   *
   * @return the current numerator of this ratio row.
   */
  public double getCurrentNumeratorValue() {
    return currentNumeratorValue;
  }

  /**
   * Sets the baseline numerator value of this ratio row.
   *
   * @param currentNumeratorValue the baseline numerator value of this ratio row.
   */
  public void setCurrentNumeratorValue(double currentNumeratorValue) {
    this.currentNumeratorValue = currentNumeratorValue;
  }

  /**
   * Returns the baseline denominator of this ratio row.
   *
   * @return the baseline denominator of this ratio row.
   */
  public double getBaselineDenominatorValue() {
    return baselineDenominatorValue;
  }

  /**
   * Sets the baseline denominator value of this ratio row.
   *
   * @param denominatorBaselineValue the baseline denominator value of this ratio row.
   */
  public void setBaselineDenominatorValue(double denominatorBaselineValue) {
    this.baselineDenominatorValue = denominatorBaselineValue;
  }

  /**
   * Returns the current denominator of this ratio row.
   *
   * @return the current denominator of this ratio row.
   */
  public double getCurrentDenominatorValue() {
    return currentDenominatorValue;
  }

  /**
   * Sets the current denominator value of this ratio row.
   *
   * @param denominatorCurrentValue the current denominator value of this ratio row.
   */
  public void setCurrentDenominatorValue(double denominatorCurrentValue) {
    this.currentDenominatorValue = denominatorCurrentValue;
  }

  @Override
  public RatioCubeNode toNode() {
    return new RatioCubeNode(this);
  }

  @Override
  public CubeNode toNode(int level, int index, CubeNode parent) {
    return new RatioCubeNode(level, index, this, (RatioCubeNode) parent);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RatioRow)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    RatioRow ratioRow = (RatioRow) o;
    return Double.compare(ratioRow.baselineNumeratorValue, baselineNumeratorValue) == 0
        && Double.compare(ratioRow.currentNumeratorValue, currentNumeratorValue) == 0
        && Double.compare(ratioRow.baselineDenominatorValue, baselineDenominatorValue) == 0
        && Double.compare(ratioRow.currentDenominatorValue, currentDenominatorValue) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), baselineNumeratorValue, currentNumeratorValue,
        baselineDenominatorValue,
        currentDenominatorValue);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("baselineNumerator", baselineNumeratorValue)
        .add("baselineDenominator", baselineDenominatorValue)
        .add("currentNumerator", currentNumeratorValue)
        .add("currentDenominator", currentDenominatorValue)
        .add("changeRatio", currentNumeratorValue / baselineNumeratorValue)
        .add("dimensions", super.getDimensions())
        .add("dimensionValues", super.getDimensionValues())
        .toString();
  }
}
