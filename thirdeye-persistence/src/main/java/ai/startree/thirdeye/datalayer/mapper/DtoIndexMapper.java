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

package ai.startree.thirdeye.datalayer.mapper;

import ai.startree.thirdeye.datalayer.entity.AbstractIndexEntity;
import ai.startree.thirdeye.datalayer.entity.HasJsonVal;
import ai.startree.thirdeye.datalayer.entity.RcaInvestigationIndex;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import java.sql.Timestamp;
import org.modelmapper.ModelMapper;

public class DtoIndexMapper {

  private static final ModelMapper MODEL_MAPPER = new ModelMapper();

  static {
    // add custom mapping from DTO to index
    MODEL_MAPPER
        .createTypeMap(RcaInvestigationDTO.class, RcaInvestigationIndex.class)
        .addMappings(new RcaInvestigationIndexMapper());
  }

  public static <E extends AbstractDTO> AbstractIndexEntity toAbstractIndexEntity(final E pojo,
      final Class<? extends AbstractIndexEntity> indexClass, final String jsonVal)
      throws InstantiationException, IllegalAccessException {
    final AbstractIndexEntity abstractIndexEntity = indexClass.newInstance();
    if (abstractIndexEntity instanceof HasJsonVal) {
      ((HasJsonVal) abstractIndexEntity).setJsonVal(jsonVal);
    }
    MODEL_MAPPER.map(pojo, abstractIndexEntity);
    abstractIndexEntity.setBaseId(pojo.getId());
    abstractIndexEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));

    return abstractIndexEntity;
  }
}
