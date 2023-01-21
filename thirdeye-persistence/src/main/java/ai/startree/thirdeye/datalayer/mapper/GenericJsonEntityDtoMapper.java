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
import java.sql.Timestamp;

public class GenericJsonEntityDtoMapper {

  public static final ObjectMapper OBJECT_MAPPER = ThirdEyeSerialization.getObjectMapper();

  public static <E extends AbstractDTO> String toJsonString(final E pojo)
      throws JsonProcessingException {
    return OBJECT_MAPPER.writeValueAsString(pojo);
  }

  public static <E extends AbstractDTO> GenericJsonEntity toGenericJsonEntity(final E pojo)
      throws JsonProcessingException {
    final GenericJsonEntity ret = new GenericJsonEntity();
    final int version = pojo.getVersion() == 0 ? 1 : pojo.getVersion();
    ret.setId(pojo.getId());
    ret.setCreateTime(new Timestamp(System.currentTimeMillis()));
    ret.setUpdateTime(new Timestamp(System.currentTimeMillis()));
    ret.setVersion(version);
    ret.setType(SubEntities.getType(pojo.getClass()));
    final String jsonVal = toJsonString(pojo);
    ret.setJsonVal(jsonVal);
    return ret;
  }

  public static <E extends AbstractDTO> E toDto(final GenericJsonEntity entity,
      final Class<E> beanClass)
      throws JsonProcessingException {
    E e = OBJECT_MAPPER.readValue(entity.getJsonVal(), beanClass);
    e.setId(entity.getId());
    e.setVersion(entity.getVersion());
    e.setCreateTime(entity.getCreateTime());
    e.setUpdateTime(entity.getUpdateTime());
    return e;
  }
}
