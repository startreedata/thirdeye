/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.spi.api;

import java.util.Map;
import java.util.StringJoiner;

public class DimensionFilterContributionApi {
  private Map<String, String> dimensionFilters;
  private Double value;
  private Double percentage;

  public Map<String, String> getDimensionFilters() {
    return dimensionFilters;
  }

  public DimensionFilterContributionApi setDimensionFilters(
      final Map<String, String> dimensionFilters) {
    this.dimensionFilters = dimensionFilters;
    return this;
  }

  public Double getValue() {
    return value;
  }

  public DimensionFilterContributionApi setValue(final Double value) {
    this.value = value;
    return this;
  }

  public Double getPercentage() {
    return percentage;
  }

  public DimensionFilterContributionApi setPercentage(final Double percentage) {
    this.percentage = percentage;
    return this;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "[", "]")
        .add(String.valueOf(dimensionFilters))
        .add(String.valueOf(value))
        .toString();
  }
}
