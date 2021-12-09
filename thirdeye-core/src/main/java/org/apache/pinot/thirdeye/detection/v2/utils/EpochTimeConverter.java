package org.apache.pinot.thirdeye.detection.v2.utils;

import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.spi.detection.TimeConverter;

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
