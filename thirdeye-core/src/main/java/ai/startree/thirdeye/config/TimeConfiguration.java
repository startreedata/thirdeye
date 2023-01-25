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
package ai.startree.thirdeye.config;

import static ai.startree.thirdeye.spi.Constants.DEFAULT_CHRONOLOGY;

import org.joda.time.DateTimeZone;

public class TimeConfiguration {

  private static final long JAN_1_2000_UTC = 946684800000L;

  /**
   * Timezone used for notifications
   */
  private DateTimeZone timezone = DEFAULT_CHRONOLOGY.getZone();

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
}
