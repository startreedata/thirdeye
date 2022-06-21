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
package ai.startree.thirdeye.spi.api.cube;

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
