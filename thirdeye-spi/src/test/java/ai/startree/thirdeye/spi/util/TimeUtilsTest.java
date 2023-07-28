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
