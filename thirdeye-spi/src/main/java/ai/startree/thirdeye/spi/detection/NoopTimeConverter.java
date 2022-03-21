/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.dataframe.Series;

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
