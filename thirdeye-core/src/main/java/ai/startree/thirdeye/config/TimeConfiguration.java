/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.config;

import static ai.startree.thirdeye.spi.Constants.DEFAULT_CHRONOLOGY;

import ai.startree.thirdeye.spi.Constants;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.DateTimeZone;

public class TimeConfiguration {

  private static final long JAN_1_2000_UTC = 946684800000L;

  /**
   * Timezone to use in notifications.
   */
  private DateTimeZone timezone = DEFAULT_CHRONOLOGY.getZone();

  /**
   * Time format to use in notifications.
   * For instance with "MMM dd, yyyy HH:mm" the datetime will render as "Feb 03, 2020 00:00".
   * See pattern specification here
   * https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
   * It is recommended to not put the timezone it this pattern.
   * The timezone is appended in the notification text by ThirdEye, but not in all datetime strings 
   * to avoid repetition.
   */
  private @NonNull String dateTimePattern = Constants.NOTIFICATIONS_DEFAULT_DATE_PATTERN;

  /**
   * Onboarding start time >= onboardingStartTimeLimit.
   * This allows administrators to prevent users from running alert onboardings too far in the past, reading too
   * much data.
   */
  private long minimumOnboardingStartTime = JAN_1_2000_UTC;

  public DateTimeZone getTimezone() {
    return timezone;
  }

  public TimeConfiguration setTimezone(final DateTimeZone timezone) {
    this.timezone = timezone;
    return this;
  }

  public long getMinimumOnboardingStartTime() {
    return minimumOnboardingStartTime;
  }

  public TimeConfiguration setMinimumOnboardingStartTime(final long minimumOnboardingStartTime) {
    this.minimumOnboardingStartTime = minimumOnboardingStartTime;
    return this;
  }

  public @NonNull String getDateTimePattern() {
    return dateTimePattern;
  }

  public TimeConfiguration setDateTimePattern(
      final String dateTimePattern) {
    this.dateTimePattern = dateTimePattern;
    return this;
  }
}
