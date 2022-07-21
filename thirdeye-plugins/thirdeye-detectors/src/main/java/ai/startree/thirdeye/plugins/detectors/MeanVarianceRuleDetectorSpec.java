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
import ai.startree.thirdeye.spi.detection.Pattern;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MeanVarianceRuleDetectorSpec extends AbstractSpec {

  @Deprecated
  private int lookback = 52; //default look back of 52 units
  private double sensitivity = 5; //default sensitivity of 5, equals +/- 1 sigma
  private Pattern pattern = Pattern.UP_OR_DOWN;
  /**Lookback period in ISO-8601 format eg P1D. Used with monitoringGranularity to compute lookback in steps.*/
  private String lookbackPeriod = null;
  /**Biggest period in ISO-8601 format. Possible values are P7D and P1D. Used to take into account seasonality when computing mean-variance.*/
  private String seasonalityPeriod = null;

  public int getLookback() {
    return lookback;
  }

  public MeanVarianceRuleDetectorSpec setLookback(final int lookback) {
    this.lookback = lookback;
    return this;
  }

  public double getSensitivity() {
    return sensitivity;
  }

  public MeanVarianceRuleDetectorSpec setSensitivity(final double sensitivity) {
    this.sensitivity = sensitivity;
    return this;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public MeanVarianceRuleDetectorSpec setPattern(
      final Pattern pattern) {
    this.pattern = pattern;
    return this;
  }

  public String getLookbackPeriod() {
    return lookbackPeriod;
  }

  public MeanVarianceRuleDetectorSpec setLookbackPeriod(final String lookbackPeriod) {
    this.lookbackPeriod = lookbackPeriod;
    return this;
  }

  public String getSeasonalityPeriod() {
    return seasonalityPeriod;
  }

  public MeanVarianceRuleDetectorSpec setSeasonalityPeriod(final String seasonalityPeriod) {
    this.seasonalityPeriod = seasonalityPeriod;
    return this;
  }
}
