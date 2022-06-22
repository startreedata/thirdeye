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

package ai.startree.thirdeye.plugins.rca.contributors.cube.data;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Objects;

/**
 * Stores the additive metric that is returned from DB.
 */
public class AdditiveRow {

  protected double baselineValue;
  protected double currentValue;
  protected Dimensions dimensions;
  protected DimensionValues dimensionValues;

  /**
   * Constructs an additive row.
   *
   * @param dimensions the dimension names of this row.
   * @param dimensionValues the dimension values of this row.
   */
  public AdditiveRow(Dimensions dimensions, DimensionValues dimensionValues) {
    this(dimensions, dimensionValues, 0, 0);
  }

  /**
   * Constructs an additive row.
   *
   * @param dimensions the dimension names of this row.
   * @param dimensionValues the dimension values of this row.
   * @param baselineValue the baseline value of this additive metric.
   * @param currentValue the current value of this additive metric.
   */
  public AdditiveRow(Dimensions dimensions, DimensionValues dimensionValues, double baselineValue,
      double currentValue) {
    this.dimensions = Preconditions.checkNotNull(dimensions);
    this.dimensionValues = Preconditions.checkNotNull(dimensionValues);
    this.baselineValue = baselineValue;
    this.currentValue = currentValue;
  }

  /**
   * Returns the baseline value of this additive row.
   *
   * @return the baseline value of this additive row.
   */
  public double getBaselineValue() {
    return baselineValue;
  }

  /**
   * Sets the baseline value of this additive row.
   *
   * @param baselineValue the baseline value of this additive row.
   */
  public void setBaselineValue(double baselineValue) {
    this.baselineValue = baselineValue;
  }

  /**
   * Returns the current value of this additive row.
   *
   * @return the current value of this additive row.
   */
  public double getCurrentValue() {
    return currentValue;
  }

  /**
   * Sets the current value of this additive row.
   *
   * @param currentValue the current value of this additive row.
   */
  public void setCurrentValue(double currentValue) {
    this.currentValue = currentValue;
  }

  /**
   * Returns the dimension names of this row, such as ["country", "page key"].
   *
   * @return the dimension names of this row.
   */
  public Dimensions getDimensions() {
    return dimensions;
  }

  /**
   * Sets dimension names of this row, such as ["country", "page key"].
   *
   * @param dimensions the dimension names for this row.
   */
  public void setDimensions(Dimensions dimensions) {
    this.dimensions = Preconditions.checkNotNull(dimensions);
  }

  /**
   * Returns dimension values of this row, such as ["US", "linkedin.com"]
   *
   * @return dimension values of this row, such as ["US", "linkedin.com"]
   */
  public DimensionValues getDimensionValues() {
    return dimensionValues;
  }

  /**
   * Sets dimension values of this row, such as ["US", "linkedin.com"]
   *
   * @param dimensionValues the dimension values for this row.
   */
  public void setDimensionValues(DimensionValues dimensionValues) {
    this.dimensionValues = Preconditions.checkNotNull(dimensionValues);
  }

  /**
   * Converts current row to a CubeNode.
   *
   * @return a CubeNode of this row.
   */
  public AdditiveCubeNode toNode() {
    return new AdditiveCubeNode(this);
  }

  /**
   * Converts current row to a CubeNode.
   *
   * @param level the level of this node; 0 is the top level.
   * @param index the index of this node, which is used for speeding up algorithm speed.
   * @param parent the parent of this node.
   * @return a CubeNode of this row.
   */
  public AdditiveCubeNode toNode(int level, int index, AdditiveCubeNode parent) {
    return new AdditiveCubeNode(level, index, this, (AdditiveCubeNode) parent);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdditiveRow row = (AdditiveRow) o;
    return Double.compare(row.getBaselineValue(), getBaselineValue()) == 0
        && Double.compare(row.getCurrentValue(), getCurrentValue()) == 0 && Objects
        .equals(getDimensions(), row.getDimensions()) && Objects
        .equals(getDimensionValues(), row.getDimensionValues());
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(getDimensions(), getDimensionValues(), getBaselineValue(), getCurrentValue());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("baselineValue", baselineValue)
        .add("currentValue", currentValue)
        .add("dimensions", dimensions)
        .add("dimensionValues", dimensionValues)
        .toString();
  }
}
