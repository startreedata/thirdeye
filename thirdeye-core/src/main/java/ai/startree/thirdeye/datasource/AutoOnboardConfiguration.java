/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource;

import java.time.Duration;

public class AutoOnboardConfiguration {

  private boolean enabled = false;
  private Duration frequency = Duration.ofMinutes(5);

  public Duration getFrequency() {
    return frequency;
  }

  public AutoOnboardConfiguration setFrequency(final Duration frequency) {
    this.frequency = frequency;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public AutoOnboardConfiguration setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }
}
