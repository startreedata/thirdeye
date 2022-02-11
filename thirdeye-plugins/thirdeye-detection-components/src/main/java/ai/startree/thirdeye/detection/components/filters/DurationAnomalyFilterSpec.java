/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.filters;

import ai.startree.thirdeye.spi.detection.AbstractSpec;

public class DurationAnomalyFilterSpec extends AbstractSpec {

  private String minDuration = "PT0S"; // default value 0 seconds
  private String maxDuration = "P365D"; // default value 1 year

  public String getMinDuration() {
    return minDuration;
  }

  public void setMinDuration(String minDuration) {
    this.minDuration = minDuration;
  }

  public String getMaxDuration() {
    return maxDuration;
  }

  public void setMaxDuration(String maxDuration) {
    this.maxDuration = maxDuration;
  }
}
