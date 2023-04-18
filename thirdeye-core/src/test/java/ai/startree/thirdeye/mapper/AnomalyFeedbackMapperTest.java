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
package ai.startree.thirdeye.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import java.sql.Timestamp;
import java.time.Instant;
import org.testng.annotations.Test;

public class AnomalyFeedbackMapperTest {

  @Test
  public void toApi_convertsFeedbackType() {
    final AnomalyFeedbackDTO dto = new AnomalyFeedbackDTO();
    dto.setFeedbackType(AnomalyFeedbackType.ANOMALY);

    AnomalyFeedbackApi api = AnomalyFeedbackMapper.INSTANCE.toApi(dto);

    assertThat(api.getType()).isEqualTo(AnomalyFeedbackType.ANOMALY);
  }

  @Test
  public void toApi_convertsCreateTime() {
    final AnomalyFeedbackDTO dto = new AnomalyFeedbackDTO();
    Instant createTime = Instant.now();
    dto.setCreateTime(Timestamp.from(createTime));

    AnomalyFeedbackApi api = AnomalyFeedbackMapper.INSTANCE.toApi(dto);

    assertThat(api.getCreated().toInstant()).isEqualTo(createTime);
  }

  @Test
  public void toApi_convertsUpdateTime() {
    final AnomalyFeedbackDTO dto = new AnomalyFeedbackDTO();
    Instant updateTime = Instant.now();
    dto.setUpdateTime(Timestamp.from(updateTime));

    AnomalyFeedbackApi api = AnomalyFeedbackMapper.INSTANCE.toApi(dto);

    assertThat(api.getUpdated().toInstant()).isEqualTo(updateTime);
  }

  @Test
  public void toDto_convertsType() {
    final AnomalyFeedbackApi api = new AnomalyFeedbackApi();
    api.setType(AnomalyFeedbackType.ANOMALY_EXPECTED);

    AnomalyFeedbackDTO dto = AnomalyFeedbackMapper.INSTANCE.toDto(api);

    assertThat(dto.getFeedbackType()).isEqualTo(AnomalyFeedbackType.ANOMALY_EXPECTED);
  }

  @Test
  public void toDto_createdIgnored() {
    final AnomalyFeedbackApi api = new AnomalyFeedbackApi();
    api.setCreated(Timestamp.from(Instant.now()));

    AnomalyFeedbackDTO dto = AnomalyFeedbackMapper.INSTANCE.toDto(api);

    assertThat(dto.getCreateTime()).isNull();
  }

  @Test
  public void toDto_updatedIgnored() {
    final AnomalyFeedbackApi api = new AnomalyFeedbackApi();
    api.setUpdated(Timestamp.from(Instant.now()));

    AnomalyFeedbackDTO dto = AnomalyFeedbackMapper.INSTANCE.toDto(api);

    assertThat(dto.getCreateTime()).isNull();
  }
}
