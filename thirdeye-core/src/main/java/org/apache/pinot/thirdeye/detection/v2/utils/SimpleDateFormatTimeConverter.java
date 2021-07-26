package org.apache.pinot.thirdeye.detection.v2.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class SimpleDateFormatTimeConverter extends DefaultTimeConverter {

  private final SimpleDateFormat sdf;

  public SimpleDateFormatTimeConverter(String timeFormat) {
    sdf = new SimpleDateFormat(timeFormat);
  }

  @Override
  public long convert(final String timeValue) {
    try {
      return sdf.parse(timeValue).getTime();
    } catch (ParseException e) {
      throw new RuntimeException(
          "Unable to parse time value " + timeValue, e);
    }
  }
}
