/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detectionpipeline.utils;

import ai.startree.thirdeye.spi.detection.TimeConverter;
import java.util.concurrent.TimeUnit;

public class EpochTimeConverter implements TimeConverter {

  private final TimeUnit timeUnit;

  public EpochTimeConverter(String timeUnit) {
    this.timeUnit = TimeUnit.valueOf(timeUnit);
  }

  @Override
  public long convert(final String timeValue) {
    return timeUnit.toMillis(Long.parseLong(timeValue));
  }

  @Override
  public String convertMillis(final long time) {
    return String.valueOf(timeUnit.convert(time, TimeUnit.MILLISECONDS));
  }
}
