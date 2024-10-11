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
package ai.startree.thirdeye.spi.api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.joda.time.DateTimeZone;

public class TimeConfigurationApi {

  private DateTimeZone timezone;
  private @NonNull String dateTimePattern;
  private long minimumOnboardingStartTime;

  public DateTimeZone getTimezone() {
    return timezone;
  }

  public TimeConfigurationApi setTimezone(final DateTimeZone timezone) {
    this.timezone = timezone;
    return this;
  }

  public long getMinimumOnboardingStartTime() {
    return minimumOnboardingStartTime;
  }

  public TimeConfigurationApi setMinimumOnboardingStartTime(final long minimumOnboardingStartTime) {
    this.minimumOnboardingStartTime = minimumOnboardingStartTime;
    return this;
  }

  public @NonNull String getDateTimePattern() {
    return dateTimePattern;
  }

  public TimeConfigurationApi setDateTimePattern(
      final String dateTimePattern) {
    this.dateTimePattern = dateTimePattern;
    return this;
  }
}
