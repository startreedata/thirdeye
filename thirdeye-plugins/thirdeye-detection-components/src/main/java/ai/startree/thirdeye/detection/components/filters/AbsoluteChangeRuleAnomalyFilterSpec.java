/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.filters;

import ai.startree.thirdeye.spi.detection.AbstractSpec;

public class AbsoluteChangeRuleAnomalyFilterSpec extends AbstractSpec {

  private double threshold = Double.NaN;
  private String offset;
  private String pattern = "UP_OR_DOWN";

  public double getThreshold() {
    return threshold;
  }

  public AbsoluteChangeRuleAnomalyFilterSpec setThreshold(final double threshold) {
    this.threshold = threshold;
    return this;
  }

  public String getOffset() {
    return offset;
  }

  public AbsoluteChangeRuleAnomalyFilterSpec setOffset(final String offset) {
    this.offset = offset;
    return this;
  }

  public String getPattern() {
    return pattern;
  }

  public AbsoluteChangeRuleAnomalyFilterSpec setPattern(final String pattern) {
    this.pattern = pattern;
    return this;
  }
}
