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
package ai.startree.thirdeye.plugins.detection.components.filters;

import ai.startree.thirdeye.spi.detection.AbstractSpec;

public class PercentageChangeRuleAnomalyFilterSpec extends AbstractSpec {

  private String offset;
  private String pattern = "UP_OR_DOWN";
  private double threshold = 0.0; // by default set threshold to 0 to pass all anomalies
  private double upThreshold = Double.NaN;
  private double downThreshold = Double.NaN;

  public String getOffset() {
    return offset;
  }

  public PercentageChangeRuleAnomalyFilterSpec setOffset(final String offset) {
    this.offset = offset;
    return this;
  }

  public String getPattern() {
    return pattern;
  }

  public PercentageChangeRuleAnomalyFilterSpec setPattern(final String pattern) {
    this.pattern = pattern;
    return this;
  }

  public double getThreshold() {
    return threshold;
  }

  public PercentageChangeRuleAnomalyFilterSpec setThreshold(final double threshold) {
    this.threshold = threshold;
    return this;
  }

  public double getUpThreshold() {
    return upThreshold;
  }

  public PercentageChangeRuleAnomalyFilterSpec setUpThreshold(final double upThreshold) {
    this.upThreshold = upThreshold;
    return this;
  }

  public double getDownThreshold() {
    return downThreshold;
  }

  public PercentageChangeRuleAnomalyFilterSpec setDownThreshold(final double downThreshold) {
    this.downThreshold = downThreshold;
    return this;
  }
}
