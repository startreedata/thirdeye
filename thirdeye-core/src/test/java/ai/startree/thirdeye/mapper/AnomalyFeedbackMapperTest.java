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

import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.api.UserApi;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import ai.startree.thirdeye.spi.detection.AnomalyCause;
import ai.startree.thirdeye.spi.detection.AnomalyFeedbackType;
import java.sql.Timestamp;
import java.time.Instant;
import org.testng.annotations.Test;

public class AnomalyFeedbackMapperTest {

  @Test
  public void toApi() {
    final AnomalyFeedbackDTO dto = new AnomalyFeedbackDTO()
        .setFeedbackType(AnomalyFeedbackType.ANOMALY)
        .setCause(AnomalyCause.FRAUD)
        .setComment("comment");
    Instant expectedTime = Instant.now();
    dto.setCreateTime(Timestamp.from(expectedTime))
        .setUpdateTime(Timestamp.from(expectedTime))
        .setUpdatedBy("updatedBy")
        .setCreatedBy("createdBy");

    AnomalyFeedbackApi api = AnomalyFeedbackMapper.INSTANCE.toApi(dto);

    assertThat(api.getType()).isEqualTo(AnomalyFeedbackType.ANOMALY);
    assertThat(api.getCause()).isEqualTo(AnomalyCause.FRAUD);
    assertThat(api.getComment()).isEqualTo("comment");
    assertThat(api.getOwner().getPrincipal()).isEqualTo("createdBy");
    assertThat(api.getUpdatedBy().getPrincipal()).isEqualTo("updatedBy");
    assertThat(api.getCreated().toInstant()).isEqualTo(expectedTime);
    assertThat(api.getUpdated().toInstant()).isEqualTo(expectedTime);
  }

  @Test
  public void toDto() {
    final AnomalyFeedbackApi api = new AnomalyFeedbackApi()
        .setType(AnomalyFeedbackType.ANOMALY_EXPECTED)
        .setCause(AnomalyCause.FRAUD)
        .setComment("comment")
        .setId(1234L);

    AnomalyFeedbackDTO dto = AnomalyFeedbackMapper.INSTANCE.toDto(api);

    assertThat(dto.getFeedbackType()).isEqualTo(AnomalyFeedbackType.ANOMALY_EXPECTED);
    assertThat(dto.getCause()).isEqualTo(AnomalyCause.FRAUD);
    assertThat(dto.getComment()).isEqualTo("comment");
    assertThat(dto.getId()).isEqualTo(1234L);
  }

  @Test
  public void toDto_withIgnoredFields() {
    final AnomalyFeedbackApi api = new AnomalyFeedbackApi()
        .setCreated(Timestamp.from(Instant.now()))
        .setUpdated(Timestamp.from(Instant.now()))
        .setOwner(new UserApi().setPrincipal("test user"))
        .setUpdatedBy(new UserApi().setPrincipal("test user"));

    AnomalyFeedbackDTO dto = AnomalyFeedbackMapper.INSTANCE.toDto(api);

    assertThat(dto.getCreateTime()).isNull();
    assertThat(dto.getUpdateTime()).isNull();
    assertThat(dto.getCreatedBy()).isNull();
    assertThat(dto.getUpdatedBy()).isNull();
  }
}
