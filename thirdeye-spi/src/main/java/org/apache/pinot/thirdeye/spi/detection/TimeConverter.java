package org.apache.pinot.thirdeye.spi.detection;

import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.Series;

public interface TimeConverter {

  /**
   * Convert incoming time value string to milliseconds epoch value.
   *
   * @return milliseconds epoch value
   */
  long convert(String timeValue);

  /**
   * Convert back millis to String
   *
   * @param time
   * @return
   */
  String convertMillis(long time);

  /**
   * Convert incoming time series value to milliseconds epoch long series.
   *
   * @return milliseconds epoch long series.
   */
  LongSeries convertSeries(Series series);
}
