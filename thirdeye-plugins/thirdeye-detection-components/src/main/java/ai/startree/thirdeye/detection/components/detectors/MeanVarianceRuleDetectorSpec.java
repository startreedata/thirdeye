/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.detectors;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.Pattern;

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
