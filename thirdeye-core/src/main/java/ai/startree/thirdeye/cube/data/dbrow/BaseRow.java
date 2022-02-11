/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.dbrow;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public abstract class BaseRow implements Row {

  protected Dimensions dimensions;
  protected DimensionValues dimensionValues;

  public BaseRow() {
  }

  public BaseRow(Dimensions dimensions, DimensionValues dimensionValues) {
    this.dimensions = Preconditions.checkNotNull(dimensions);
    this.dimensionValues = Preconditions.checkNotNull(dimensionValues);
  }

  @Override
  public Dimensions getDimensions() {
    return dimensions;
  }

  @Override
  public void setDimensions(Dimensions dimensions) {
    this.dimensions = Preconditions.checkNotNull(dimensions);
  }

  @Override
  public DimensionValues getDimensionValues() {
    return dimensionValues;
  }

  @Override
  public void setDimensionValues(DimensionValues dimensionValues) {
    this.dimensionValues = Preconditions.checkNotNull(dimensionValues);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BaseRow)) {
      return false;
    }
    BaseRow baseRow = (BaseRow) o;
    return Objects.equal(dimensions, baseRow.dimensions) && Objects
        .equal(dimensionValues, baseRow.dimensionValues);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(dimensions, dimensionValues);
  }
}
