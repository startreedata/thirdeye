/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.metric;

import ai.startree.thirdeye.spi.detection.metric.MetricType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class MetricSpec {

  private String name;
  private String alias;
  private MetricType type;

  public MetricSpec() {
  }

  public MetricSpec(String name, MetricType type) {
    this.name = name;
    this.type = type;
  }

  @JsonProperty
  public String getName() {
    return name;
  }

  @JsonProperty
  public String getAlias() {
    return alias;
  }

  @JsonProperty
  public MetricType getType() {
    return type;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final MetricSpec that = (MetricSpec) o;
    return name.equals(that.name) &&
        type == that.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type);
  }
}
