package org.apache.pinot.thirdeye.detection.v2.utils;

import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.Series;
import org.apache.pinot.thirdeye.spi.dataframe.Series.ObjectFunction;
import org.apache.pinot.thirdeye.spi.detection.NoopTimeConverter;
import org.apache.pinot.thirdeye.spi.detection.TimeConverter;
import org.apache.pinot.thirdeye.spi.util.SpiUtils.TimeFormat;

public abstract class DefaultTimeConverter implements TimeConverter {

  public static TimeConverter get(String timeFormatString) {
    TimeFormat timeFormat = null;
    try {
      final String[] tokens = timeFormatString.split(":", 2);
      try {
        timeFormat = TimeFormat.valueOf(tokens[0].toUpperCase());
      } catch (Exception e) {
        // Do nothing
      }
      switch (timeFormat) {
        case SIMPLE_DATE_FORMAT:
          return new SimpleDateFormatTimeConverter(tokens[1]);
        case EPOCH:
          return new EpochTimeConverter(tokens[1]);
      }
      return new NoopTimeConverter();
    } catch (Exception e) {
      throw new RuntimeException("Unable to parse time format - " + timeFormatString, e);
    }
  }

  @Override
  public LongSeries convertSeries(final Series series) {
    return series.map((ObjectFunction) values -> convert(String.valueOf(values[0]))).getLongs();
  }
}
