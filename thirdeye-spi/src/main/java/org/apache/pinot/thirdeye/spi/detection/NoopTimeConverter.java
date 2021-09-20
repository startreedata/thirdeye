package org.apache.pinot.thirdeye.spi.detection;

import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.Series;

public class NoopTimeConverter implements TimeConverter {

  @Override
  public long convert(final String timeValue) {
    return Long.parseLong(timeValue);
  }

  @Override
  public String convertMillis(final long time) {
    return String.valueOf(time);
  }

  @Override
  public LongSeries convertSeries(final Series series) {
    return series.getLongs();
  }
}
