/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.detectors;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import ai.startree.thirdeye.spi.detection.Pattern;

public class MeanVarianceRuleDetectorSpec extends AbstractSpec {

  private int lookback = 52; //default look back of 52 units
  private double sensitivity = 5; //default sensitivity of 5, equals +/- 1 sigma
  private Pattern pattern = Pattern.UP_OR_DOWN;

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
}
