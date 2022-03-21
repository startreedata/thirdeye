/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.detection.TimeSpec;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.Duration;

@JsonInclude(Include.NON_NULL)
public class TimeColumnApi {

  private String name;
  private Duration interval;
  private String format = TimeSpec.SINCE_EPOCH_FORMAT;
  private String timezone = TimeSpec.DEFAULT_TIMEZONE;

  public String getName() {
    return name;
  }

  public TimeColumnApi setName(final String name) {
    this.name = name;
    return this;
  }

  public Duration getInterval() {
    return interval;
  }

  public TimeColumnApi setInterval(final Duration interval) {
    this.interval = interval;
    return this;
  }

  public String getFormat() {
    return format;
  }

  public TimeColumnApi setFormat(final String format) {
    this.format = format;
    return this;
  }

  public String getTimezone() {
    return timezone;
  }

  public TimeColumnApi setTimezone(final String timezone) {
    this.timezone = timezone;
    return this;
  }
}
