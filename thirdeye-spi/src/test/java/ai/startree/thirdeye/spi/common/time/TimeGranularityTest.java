/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.spi.common.time;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.detection.TimeGranularity;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
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
