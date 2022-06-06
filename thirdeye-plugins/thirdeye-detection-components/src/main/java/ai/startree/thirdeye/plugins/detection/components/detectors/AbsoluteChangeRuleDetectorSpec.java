/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.detection.components.detectors;

import ai.startree.thirdeye.spi.detection.AbstractSpec;

public class AbsoluteChangeRuleDetectorSpec extends AbstractSpec {

  private double absoluteChange = Double.NaN;
  private String offset = "wo1w";
  private String pattern = "UP_OR_DOWN";

  public double getAbsoluteChange() {
    return absoluteChange;
  }

  public AbsoluteChangeRuleDetectorSpec setAbsoluteChange(final double absoluteChange) {
    this.absoluteChange = absoluteChange;
    return this;
  }

  public String getOffset() {
    return offset;
  }

  public AbsoluteChangeRuleDetectorSpec setOffset(final String offset) {
    this.offset = offset;
    return this;
  }

  public String getPattern() {
    return pattern;
  }

  public AbsoluteChangeRuleDetectorSpec setPattern(final String pattern) {
    this.pattern = pattern;
    return this;
  }
}
