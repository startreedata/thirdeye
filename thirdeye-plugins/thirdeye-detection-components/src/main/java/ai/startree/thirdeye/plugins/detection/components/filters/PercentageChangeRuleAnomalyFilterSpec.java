/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
