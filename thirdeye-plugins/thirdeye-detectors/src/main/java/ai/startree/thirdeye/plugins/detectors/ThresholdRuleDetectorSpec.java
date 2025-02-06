/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.plugins.detectors;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ThresholdRuleDetectorSpec extends AbstractSpec {

  private double min = Double.NaN;
  private double max = Double.NaN;
  
  private boolean maxInclusive = true;

  private boolean minInclusive = true;

  public double getMin() {
    return min;
  }

  public ThresholdRuleDetectorSpec setMin(final double min) {
    this.min = min;
    return this;
  }

  public double getMax() {
    return max;
  }

  public ThresholdRuleDetectorSpec setMax(final double max) {
    this.max = max;
    return this;
  }

  public boolean isMaxInclusive() {
    return maxInclusive;
  }

  public ThresholdRuleDetectorSpec setMaxInclusive(final boolean maxInclusive) {
    this.maxInclusive = maxInclusive;
    return this;
  }

  public boolean isMinInclusive() {
    return minInclusive;
  }

  public ThresholdRuleDetectorSpec setMinInclusive(final boolean minInclusive) {
    this.minInclusive = minInclusive;
    return this;
  }
}
