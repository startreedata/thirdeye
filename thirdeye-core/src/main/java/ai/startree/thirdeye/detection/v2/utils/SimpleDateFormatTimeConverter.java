/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.v2.utils;

import ai.startree.thirdeye.spi.detection.TimeConverter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// todo cyril this class is not used anymore - delete?
public class SimpleDateFormatTimeConverter implements TimeConverter {

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

  @Override
  public String convertMillis(final long time) {
    return sdf.format(new Date(time));
  }
}
