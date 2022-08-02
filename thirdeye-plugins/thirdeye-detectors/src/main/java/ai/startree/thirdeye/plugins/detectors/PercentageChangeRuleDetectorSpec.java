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
package ai.startree.thirdeye.plugins.detectors;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PercentageChangeRuleDetectorSpec extends AbstractSpec {

  private double percentageChange = Double.NaN;
  private String offset = "wo1w";
  private String pattern = "UP_OR_DOWN";
  private String weekStart = "WEDNESDAY";

  public double getPercentageChange() {
    return percentageChange;
  }

  public PercentageChangeRuleDetectorSpec setPercentageChange(final double percentageChange) {
    this.percentageChange = percentageChange;
    return this;
  }

  public String getOffset() {
    return offset;
  }

  public PercentageChangeRuleDetectorSpec setOffset(final String offset) {
    this.offset = offset;
    return this;
  }

  public String getPattern() {
    return pattern;
  }

  public PercentageChangeRuleDetectorSpec setPattern(final String pattern) {
    this.pattern = pattern;
    return this;
  }

  public String getWeekStart() {
    return weekStart;
  }

  public PercentageChangeRuleDetectorSpec setWeekStart(final String weekStart) {
    this.weekStart = weekStart;
    return this;
  }
}
