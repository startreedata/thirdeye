package org.apache.pinot.thirdeye.config;

public class ThirdEyeSchedulerConfiguration {

  private HolidayEventsLoaderConfiguration holiday = new HolidayEventsLoaderConfiguration();

  public HolidayEventsLoaderConfiguration getHoliday() {
    return holiday;
  }

  public ThirdEyeSchedulerConfiguration setHoliday(
      final HolidayEventsLoaderConfiguration holiday) {
    this.holiday = holiday;
    return this;
  }
}
