package ai.startree.thirdeye.detection.components.detectors.results;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DimensionInfo {

  private List<String> dimensionColumns;
  private List<Object> dimensionValues;

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
