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
package ai.startree.thirdeye.spi.api;

import java.util.Map;

public class HeatMapResultApi {
  private MetricApi metric;
  private HeatMapBreakdownApi baseline;
  private HeatMapBreakdownApi current;

  public MetricApi getMetric() {
    return metric;
  }

  public HeatMapResultApi setMetric(final MetricApi metric) {
    this.metric = metric;
    return this;
  }

  public HeatMapBreakdownApi getBaseline() {
    return baseline;
  }

  public HeatMapResultApi setBaseline(
      final HeatMapBreakdownApi baseline) {
    this.baseline = baseline;
    return this;
  }

  public HeatMapBreakdownApi getCurrent() {
    return current;
  }

  public HeatMapResultApi setCurrent(
      final HeatMapBreakdownApi current) {
    this.current = current;
    return this;
  }

  public static class HeatMapBreakdownApi {
    private Map<String, Map<String, Double>> breakdown;

    public Map<String, Map<String, Double>> getBreakdown() {
      return breakdown;
    }

    public HeatMapBreakdownApi setBreakdown(
        final Map<String, Map<String, Double>> breakdown) {
      this.breakdown = breakdown;
      return this;
    }
  }
}
