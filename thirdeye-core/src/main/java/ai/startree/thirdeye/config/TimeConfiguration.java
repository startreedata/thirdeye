package ai.startree.thirdeye.config;

import static ai.startree.thirdeye.spi.Constants.DEFAULT_TIMEZONE;

import org.joda.time.DateTimeZone;

public class TimeConfiguration {

  private DateTimeZone timezone = DEFAULT_TIMEZONE;

  public DateTimeZone getTimezone() {
    return timezone;
  }

  public TimeConfiguration setTimezone(final DateTimeZone timezone) {
    this.timezone = timezone;
    return this;
  }
}
