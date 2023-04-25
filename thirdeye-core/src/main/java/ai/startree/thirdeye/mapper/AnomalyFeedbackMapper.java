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

import ai.startree.thirdeye.spi.api.AnomalyFeedbackApi;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFeedbackDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AnomalyFeedbackMapper {

  AnomalyFeedbackMapper INSTANCE = Mappers.getMapper(AnomalyFeedbackMapper.class);

  @Mapping(source = "type", target = "feedbackType")
  @Mapping(source = "updatedBy.principal", target = "updatedBy", ignore = true)
  AnomalyFeedbackDTO toDto(AnomalyFeedbackApi api);

  @Mapping(source = "feedbackType", target = "type")
  @Mapping(source = "createTime", target = "created")
  @Mapping(source = "updateTime", target = "updated")
  @Mapping(source = "createdBy", target = "owner.principal")
  @Mapping(source = "updatedBy", target = "updatedBy.principal")
  AnomalyFeedbackApi toApi(AnomalyFeedbackDTO dto);
}
