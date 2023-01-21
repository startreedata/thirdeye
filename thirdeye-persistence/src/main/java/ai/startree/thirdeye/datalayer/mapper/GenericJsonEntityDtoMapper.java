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

package ai.startree.thirdeye.datalayer.mapper;

import ai.startree.thirdeye.datalayer.dao.SubEntities;
import ai.startree.thirdeye.datalayer.entity.GenericJsonEntity;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.json.ThirdEyeSerialization;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GenericJsonEntityDtoMapper {

  public static final ObjectMapper OBJECT_MAPPER = ThirdEyeSerialization.getObjectMapper();

  public static <E extends AbstractDTO> String toJsonString(final E pojo)
      throws JsonProcessingException {
    return OBJECT_MAPPER.writeValueAsString(pojo);
  }

  public static <E extends AbstractDTO> GenericJsonEntity toGenericJsonEntity(final E pojo)
      throws JsonProcessingException {
    final int version = pojo.getVersion() == 0 ? 1 : pojo.getVersion();
    final String jsonVal = toJsonString(pojo);

    final GenericJsonEntity entity = new GenericJsonEntity()
        .setType(SubEntities.getType(pojo.getClass()))
        .setJsonVal(jsonVal);

    entity
        .setId(pojo.getId())
        .setCreateTime(pojo.getCreateTime())
        .setUpdateTime(pojo.getUpdateTime())
        .setVersion(version);

    return entity;
  }

  public static <DtoT extends AbstractDTO> DtoT toDto(final GenericJsonEntity entity,
      final Class<DtoT> beanClass)
      throws JsonProcessingException {
    DtoT dto = OBJECT_MAPPER.readValue(entity.getJsonVal(), beanClass);
    dto
        .setId(entity.getId())
        .setVersion(entity.getVersion())
        .setCreateTime(entity.getCreateTime())
        .setUpdateTime(entity.getUpdateTime());
    return dto;
  }
}
