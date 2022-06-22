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
package ai.startree.thirdeye.metric;

import ai.startree.thirdeye.spi.metric.MetricType;
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
