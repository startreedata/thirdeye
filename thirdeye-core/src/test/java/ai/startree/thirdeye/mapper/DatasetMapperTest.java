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
package ai.startree.thirdeye.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.spi.api.TimeColumnApi;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;

@Test
public class DatasetMapperTest {

  @Test
  @Deprecated
  public void testMillisecondsGranularity() {
    final DatasetConfigDTO dto = new DatasetConfigDTO();
    DatasetMapper.updateTimeSpecOnDataset(dto,
        new TimeColumnApi().setInterval(Duration.ofMillis(1)));
    assertThat(dto.getTimeDuration()).isEqualTo(1);
    assertThat(dto.getTimeUnit()).isEqualTo(TimeUnit.MILLISECONDS);
  }

  @Test
  @Deprecated
  public void testSecondsGranularity() {
    final DatasetConfigDTO dto = new DatasetConfigDTO();
    // 1 sec granularity
    DatasetMapper.updateTimeSpecOnDataset(dto,
        new TimeColumnApi().setInterval(Duration.ofMillis(1000)));
    assertThat(dto.getTimeDuration()).isEqualTo(1);
    assertThat(dto.getTimeUnit()).isEqualTo(TimeUnit.SECONDS);

    // 34.xxx sec granularity is rounded - this kind of input should not happen
    DatasetMapper.updateTimeSpecOnDataset(dto,
        new TimeColumnApi().setInterval(Duration.ofMillis(34567)));
    assertThat(dto.getTimeDuration()).isEqualTo(34);
    assertThat(dto.getTimeUnit()).isEqualTo(TimeUnit.SECONDS);

  }

  @Test
  @Deprecated
  public void testMinutesGranularity() {
    final DatasetConfigDTO dto = new DatasetConfigDTO();
    DatasetMapper.updateTimeSpecOnDataset(dto,
        new TimeColumnApi().setInterval(Duration.ofMillis(60_000)));
    assertThat(dto.getTimeDuration()).isEqualTo(1);
    assertThat(dto.getTimeUnit()).isEqualTo(TimeUnit.MINUTES);
  }

  @Test
  @Deprecated
  public void testHoursGranularity() {
    final DatasetConfigDTO dto = new DatasetConfigDTO();
    DatasetMapper.updateTimeSpecOnDataset(dto,
        new TimeColumnApi().setInterval(Duration.ofMillis(3600_000)));
    assertThat(dto.getTimeDuration()).isEqualTo(1);
    assertThat(dto.getTimeUnit()).isEqualTo(TimeUnit.HOURS);
  }

  @Test
  @Deprecated
  public void testDaysGranularity() {
    final DatasetConfigDTO dto = new DatasetConfigDTO();
    DatasetMapper.updateTimeSpecOnDataset(dto,
        new TimeColumnApi().setInterval(Duration.ofMillis(86_400_000)));
    assertThat(dto.getTimeDuration()).isEqualTo(1);
    assertThat(dto.getTimeUnit()).isEqualTo(TimeUnit.DAYS);
  }
}
