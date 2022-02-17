/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.detectors;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ThresholdRuleDetectorSpec extends AbstractSpec {

  private double min = Double.NaN;
  private double max = Double.NaN;

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
}
