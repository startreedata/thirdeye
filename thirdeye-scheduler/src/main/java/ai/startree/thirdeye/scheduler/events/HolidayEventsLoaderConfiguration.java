/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.scheduler.events;

import java.util.Collections;
import java.util.List;

/**
 * The type Holiday events loader configuration.
 */
public class HolidayEventsLoaderConfiguration {

  private boolean enabled = false;

  private String googleJsonKeyPath = "config/holiday-loader-key.json";

  /**
   * Specify the time range used to calculate the upper bound for an holiday's start time. In
   * milliseconds
   */
  private long holidayLoadRange;

  /**
   * The list of calendar to fetch holidays from
   */
  private List<String> calendars = Collections.emptyList();

  /**
   * Run frequency of holiday events loader (Days)
   */
  private int runFrequency;

  public boolean isEnabled() {
    return enabled;
  }

  public HolidayEventsLoaderConfiguration setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public String getGoogleJsonKeyPath() {
    return googleJsonKeyPath;
  }

  public HolidayEventsLoaderConfiguration setGoogleJsonKeyPath(final String googleJsonKeyPath) {
    this.googleJsonKeyPath = googleJsonKeyPath;
    return this;
  }

  public long getHolidayLoadRange() {
    return holidayLoadRange;
  }

  public HolidayEventsLoaderConfiguration setHolidayLoadRange(final long holidayLoadRange) {
    this.holidayLoadRange = holidayLoadRange;
    return this;
  }

  public List<String> getCalendars() {
    return calendars;
  }

  public HolidayEventsLoaderConfiguration setCalendars(final List<String> calendars) {
    this.calendars = calendars;
    return this;
  }

  public int getRunFrequency() {
    return runFrequency;
  }

  public HolidayEventsLoaderConfiguration setRunFrequency(final int runFrequency) {
    this.runFrequency = runFrequency;
    return this;
  }
}
