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
package ai.startree.thirdeye.plugins.detection.components.detectors;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.Pattern;

public class HoltWintersDetectorSpec extends AbstractSpec {

  private double alpha = -1;
  private double beta = -1;
  private double gamma = -1;
  @Deprecated // kept for backward compatibility - prefer seasonalityPeriod
  private int period = 7;
  private double sensitivity = 5;
  private Pattern pattern = Pattern.UP_OR_DOWN;
  private String weekStart = "WEDNESDAY";
  @Deprecated // kept for backward compatibility - prefer lookbackPeriod
  private Integer lookback = null;
  /**Lookback period in ISO-8601 format eg P1D. Used with monitoringGranularity to compute lookback in steps.*/
  private String lookbackPeriod = null;
  /*Biggest period in ISO-8601 format eg P7D. Used with monitoringGranularity to compute period in steps.*/
  private String seasonalityPeriod = null;

  public double getAlpha() {
    return alpha;
  }

  public HoltWintersDetectorSpec setAlpha(final double alpha) {
    this.alpha = alpha;
    return this;
  }

  public double getBeta() {
    return beta;
  }

  public HoltWintersDetectorSpec setBeta(final double beta) {
    this.beta = beta;
    return this;
  }

  public double getGamma() {
    return gamma;
  }

  public HoltWintersDetectorSpec setGamma(final double gamma) {
    this.gamma = gamma;
    return this;
  }

  public int getPeriod() {
    return period;
  }

  public HoltWintersDetectorSpec setPeriod(final int period) {
    this.period = period;
    return this;
  }

  public double getSensitivity() {
    return sensitivity;
  }

  public HoltWintersDetectorSpec setSensitivity(final double sensitivity) {
    this.sensitivity = sensitivity;
    return this;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public HoltWintersDetectorSpec setPattern(final Pattern pattern) {
    this.pattern = pattern;
    return this;
  }

  public String getWeekStart() {
    return weekStart;
  }

  public HoltWintersDetectorSpec setWeekStart(final String weekStart) {
    this.weekStart = weekStart;
    return this;
  }

  @Deprecated
  public Integer getLookback() {
    return lookback;
  }

  @Deprecated
  public HoltWintersDetectorSpec setLookback(final Integer lookback) {
    this.lookback = lookback;
    return this;
  }

  public String getLookbackPeriod() {
    return lookbackPeriod;
  }

  public HoltWintersDetectorSpec setLookbackPeriod(final String lookbackPeriod) {
    this.lookbackPeriod = lookbackPeriod;
    return this;
  }

  public String getSeasonalityPeriod() {
    return seasonalityPeriod;
  }

  public HoltWintersDetectorSpec setSeasonalityPeriod(final String seasonalityPeriod) {
    this.seasonalityPeriod = seasonalityPeriod;
    return this;
  }
}
