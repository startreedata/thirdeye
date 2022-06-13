/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import ai.startree.thirdeye.spi.util.SpiUtils.TimeFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.concurrent.TimeUnit;

public class TimeSpec {

  private static final TimeGranularity DEFAULT_TIME_GRANULARITY = new TimeGranularity(1,
      TimeUnit.DAYS);
  public static String SINCE_EPOCH_FORMAT = TimeFormat.EPOCH.toString();

  private String columnName;
  private TimeGranularity dataGranularity = DEFAULT_TIME_GRANULARITY;
  private String format = SINCE_EPOCH_FORMAT; //sinceEpoch or yyyyMMdd

  public TimeSpec() {
  }

  public TimeSpec(String columnName, TimeGranularity dataGranularity, String format) {
    this.columnName = columnName;
    this.dataGranularity = dataGranularity;
    this.format = format;
  }

  @JsonProperty
  public String getColumnName() {
    return columnName;
  }

  @JsonProperty
  public TimeGranularity getDataGranularity() {
    return dataGranularity;
  }

  @JsonProperty
  public String getFormat() {
    return format;
  }
}
