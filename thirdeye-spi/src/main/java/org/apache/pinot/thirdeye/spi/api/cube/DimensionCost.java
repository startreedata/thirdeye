package org.apache.pinot.thirdeye.spi.api.cube;

import com.google.common.base.Preconditions;
import java.util.Objects;

public class DimensionCost {

  private final String name;
  private final double cost;

  public DimensionCost(String name, double cost) {
    this.name = Preconditions.checkNotNull(name);
    this.cost = cost;
  }

  public String getName() {
    return name;
  }

  public double getCost() {
    return cost;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DimensionCost that = (DimensionCost) o;
    return Double.compare(that.getCost(), getCost()) == 0 && Objects
        .equals(getName(), that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getCost());
  }
}
