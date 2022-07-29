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
package ai.startree.thirdeye.plugins.detection.components.detectors.results;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DimensionInfo {

  private final List<String> dimensionColumns;
  private final List<Object> dimensionValues;

  public DimensionInfo(final List<String> dimensionColumns,
      final List<Object> dimensionValues) {
    this.dimensionColumns = dimensionColumns;
    this.dimensionValues = dimensionValues;
  }

  public List<String> getDimensionColumns() {
    return dimensionColumns;
  }

  public List<Object> getDimensionValues() {
    return dimensionValues;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DimensionInfo that = (DimensionInfo) o;
    return Arrays.toString(dimensionColumns.toArray())
        .equals(Arrays.toString(that.dimensionColumns.toArray()))
        && Arrays.toString(dimensionValues.toArray())
        .equals(Arrays.toString(that.dimensionValues.toArray()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(dimensionColumns, dimensionValues);
  }
}
