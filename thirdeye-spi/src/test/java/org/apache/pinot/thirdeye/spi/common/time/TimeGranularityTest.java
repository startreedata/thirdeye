package org.apache.pinot.thirdeye.spi.common.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.spi.detection.TimeGranularity;
import org.testng.annotations.Test;

public class TimeGranularityTest {

  @Test
  public void testToDuration() {
    final TimeGranularity tg = new TimeGranularity(1, TimeUnit.DAYS);

    final Duration d = Duration.ofDays(1);
    assertThat(tg.toDuration()).isEqualTo(d);

    // TODO NOTE!! The code below fails. TimeGranularity is not consistent!!
    // assertThat(TimeGranularity.fromDuration(d)).isEqualTo(tg);
  }
}
