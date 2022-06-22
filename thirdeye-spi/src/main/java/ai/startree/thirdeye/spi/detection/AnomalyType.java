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
package ai.startree.thirdeye.spi.detection;

/**
 * The type of anomaly.
 */
public enum AnomalyType {
  // Metric deviates from normal behavior. This is the default type.
  DEVIATION("Deviation"),
  // There is a trend change for underline metric.
  TREND_CHANGE("Trend Change"),
  // The metric is not available within specified time.
  DATA_SLA("SLA Violation");

  private final String label;

  AnomalyType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
