/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.events;

import java.util.Collections;
import java.util.List;

/**
 * The type Holiday events loader configuration.
 */
public class HolidayEventsLoaderConfiguration {

  private boolean enabled = false;
  private String googleJsonKey = "holiday-loader-key.json";

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

  public String getGoogleJsonKey() {
    return googleJsonKey;
  }

  public HolidayEventsLoaderConfiguration setGoogleJsonKey(final String googleJsonKey) {
    this.googleJsonKey = googleJsonKey;
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
