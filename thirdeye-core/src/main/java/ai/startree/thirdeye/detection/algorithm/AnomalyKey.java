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
package ai.startree.thirdeye.detection.algorithm;

import ai.startree.thirdeye.spi.detection.AnomalyType;
import ai.startree.thirdeye.spi.detection.dimension.DimensionMap;
import java.util.Objects;

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
