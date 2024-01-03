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
package ai.startree.thirdeye.spi.util;

import static ai.startree.thirdeye.spi.util.TimeUtils.maximumTriggersPerMinute;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TimeUtilsTest {

  private static final Set<String> UTC_LIKE_TIMEZONES = Set.of("Etc/GMT", "Etc/GMT+0", "Etc/GMT0",
      "GMT", "GMT+0",
      "GMT-0", "GMT0", "Etc/UTC", "UTC", "Etc/Zulu", "Zulu");

  private static final Set<String> BRUSSELS_LIKE_TIMEZONES = Set.of("Europe/Paris",
      "Europe/Brussels",
      "Europe/Amsterdam", "Europe/Monaco");

  private static final Set<String> PST_LIKE_TIMEZONES = Set.of("America/Los_Angeles",
      "Canada/Pacific");

  @Test
  public void testTimezonesAreEquivalent() {
    UTC_LIKE_TIMEZONES.forEach(e -> {
      assertThat(TimeUtils.timezonesAreEquivalent(e, "UTC")).isTrue();
      assertThat(TimeUtils.timezonesAreEquivalent(e, "Europe/Paris")).isFalse();
      assertThat(TimeUtils.timezonesAreEquivalent(e, "America/Los_Angeles")).isFalse();
    });

    BRUSSELS_LIKE_TIMEZONES.forEach(e -> {
      assertThat(TimeUtils.timezonesAreEquivalent(e, "UTC")).isFalse();
      assertThat(TimeUtils.timezonesAreEquivalent(e, "Europe/Paris")).isTrue();
      assertThat(TimeUtils.timezonesAreEquivalent(e, "America/Los_Angeles")).isFalse();
    });

    PST_LIKE_TIMEZONES.forEach(e -> {
      assertThat(TimeUtils.timezonesAreEquivalent(e, "UTC")).isFalse();
      assertThat(TimeUtils.timezonesAreEquivalent(e, "Europe/Paris")).isFalse();
      assertThat(TimeUtils.timezonesAreEquivalent(e, "America/Los_Angeles")).isTrue();
    });
  }

  @DataProvider
  public static Object[][] cronCases() {
    return new Object[][]{
        {"* 0 * * ? *", 60},
        {"*/10 * 0 * ? *", 6},
        {"30/10 * * * ? *", 3},
        {"0-10 * * * ? *", 11},
        {"0 * * * ? *", 1},
        {"20 * * * ? *", 1},
        {"0,1,2 * * * ? *", 3},
        {"0,5/10,17-19 * * * ? *", 10},
        {"* */10 * ? * * *", 60}
    };
  }

  @Test(dataProvider = "cronCases")
  public void testMaximumTriggersPerMinute(final String cron, final int expectedMaxTriggers) {
    assertThat(maximumTriggersPerMinute(cron)).isEqualTo(expectedMaxTriggers);
  }
}
