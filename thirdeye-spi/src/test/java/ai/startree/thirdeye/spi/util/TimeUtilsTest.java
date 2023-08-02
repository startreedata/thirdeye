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
package ai.startree.thirdeye.spi.util;

import java.util.Set;
import org.assertj.core.api.Assertions;
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
      Assertions.assertThat(TimeUtils.timezonesAreEquivalent(e, "UTC")).isTrue();
      Assertions.assertThat(TimeUtils.timezonesAreEquivalent(e, "Europe/Paris")).isFalse();
      Assertions.assertThat(TimeUtils.timezonesAreEquivalent(e, "America/Los_Angeles")).isFalse();
    });

    BRUSSELS_LIKE_TIMEZONES.forEach(e -> {
      Assertions.assertThat(TimeUtils.timezonesAreEquivalent(e, "UTC")).isFalse();
      Assertions.assertThat(TimeUtils.timezonesAreEquivalent(e, "Europe/Paris")).isTrue();
      Assertions.assertThat(TimeUtils.timezonesAreEquivalent(e, "America/Los_Angeles")).isFalse();
    });

    PST_LIKE_TIMEZONES.forEach(e -> {
      Assertions.assertThat(TimeUtils.timezonesAreEquivalent(e, "UTC")).isFalse();
      Assertions.assertThat(TimeUtils.timezonesAreEquivalent(e, "Europe/Paris")).isFalse();
      Assertions.assertThat(TimeUtils.timezonesAreEquivalent(e, "America/Los_Angeles")).isTrue();
    });
  }
}
