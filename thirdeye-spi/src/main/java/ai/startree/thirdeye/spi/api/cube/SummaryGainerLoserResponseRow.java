/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api.cube;

public class SummaryGainerLoserResponseRow extends BaseResponseRow {

  private String dimensionName;
  private String dimensionValue;
  private double cost;

  public String getDimensionName() {
    return dimensionName;
  }

  public SummaryGainerLoserResponseRow setDimensionName(final String dimensionName) {
    this.dimensionName = dimensionName;
    return this;
  }

  public String getDimensionValue() {
    return dimensionValue;
  }

  public SummaryGainerLoserResponseRow setDimensionValue(final String dimensionValue) {
    this.dimensionValue = dimensionValue;
    return this;
  }

  public double getCost() {
    return cost;
  }

  public SummaryGainerLoserResponseRow setCost(final double cost) {
    this.cost = cost;
    return this;
  }
}
