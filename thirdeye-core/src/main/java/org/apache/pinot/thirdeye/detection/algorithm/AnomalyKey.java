package org.apache.pinot.thirdeye.detection.algorithm;

import java.util.Objects;
import org.apache.pinot.thirdeye.spi.detection.AnomalyType;
import org.apache.pinot.thirdeye.spi.detection.dimension.DimensionMap;

public class AnomalyKey {

  final String metric;
  final String collection;
  final DimensionMap dimensions;
  final String mergeKey;
  final String componentKey;
  final AnomalyType type;

  public AnomalyKey(String metric, String collection, DimensionMap dimensions, String mergeKey,
      String componentKey,
      AnomalyType type) {
    this.metric = metric;
    this.collection = collection;
    this.dimensions = dimensions;
    this.mergeKey = mergeKey;
    this.componentKey = componentKey;
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AnomalyKey)) {
      return false;
    }
    AnomalyKey that = (AnomalyKey) o;
    return Objects.equals(metric, that.metric) && Objects.equals(collection, that.collection)
        && Objects.equals(
        dimensions, that.dimensions) && Objects.equals(mergeKey, that.mergeKey) && Objects
        .equals(componentKey,
            that.componentKey) && Objects.equals(type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metric, collection, dimensions, mergeKey, componentKey, type);
  }
}
