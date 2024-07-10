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
package ai.startree.thirdeye.datalayer.mapper;

import ai.startree.thirdeye.datalayer.dao.SubEntities;
import ai.startree.thirdeye.datalayer.entity.GenericJsonEntity;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public class GenericJsonEntityDtoMapper {

  public static <E extends AbstractDTO> GenericJsonEntity toGenericJsonEntity(final E pojo)
      throws JsonProcessingException {
    final int version = pojo.getVersion() == 0 ? 1 : pojo.getVersion();
    final String jsonVal = Constants.TEMPLATABLE_OBJECT_MAPPER.writeValueAsString(pojo);

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
    DtoT dto = Constants.TEMPLATABLE_OBJECT_MAPPER.readValue(entity.getJsonVal(), beanClass);
    dto
        .setId(entity.getId())
        .setVersion(entity.getVersion())
        .setCreateTime(entity.getCreateTime())
        .setUpdateTime(entity.getUpdateTime());
    return dto;
  }
}
