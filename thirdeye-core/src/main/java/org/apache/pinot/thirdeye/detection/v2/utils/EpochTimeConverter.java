package org.apache.pinot.thirdeye.detection.v2.utils;

import java.util.concurrent.TimeUnit;

public class EpochTimeConverter extends DefaultTimeConverter {

  private final TimeUnit timeUnit;

  public EpochTimeConverter(String timeUnit) {
    this.timeUnit = TimeUnit.valueOf(timeUnit);
  }

  @Override
  public long convert(final String timeValue) {
    return timeUnit.toMillis(Long.parseLong(timeValue));
  }
}
