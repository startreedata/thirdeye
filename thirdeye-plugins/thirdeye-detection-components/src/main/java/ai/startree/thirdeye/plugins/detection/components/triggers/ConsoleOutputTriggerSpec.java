/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.plugins.detection.components.triggers;

import ai.startree.thirdeye.spi.detection.AbstractSpec;

public class ConsoleOutputTriggerSpec extends AbstractSpec {
  private String format = "Got trigger event = %s";

  public String getFormat() {
    return format;
  }

  public ConsoleOutputTriggerSpec setFormat(final String format) {
    this.format = format;
    return this;
  }
}
