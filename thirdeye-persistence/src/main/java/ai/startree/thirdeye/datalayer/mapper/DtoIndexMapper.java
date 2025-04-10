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

import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.datalayer.entity.AbstractIndexEntity;
import ai.startree.thirdeye.datalayer.entity.HasJsonVal;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import org.modelmapper.ModelMapper;

public class DtoIndexMapper {

  /**
   * ModelMapper is deprecated in favor of MapStruct. All future work should use mapstruct
   * instead of ModelMapper. This is scheduled for removal.
   */
  @Deprecated
  private static final ModelMapper MODEL_MAPPER = new ModelMapper();

  public static <E extends AbstractDTO> AbstractIndexEntity toAbstractIndexEntity(final E pojo,
      final Class<? extends AbstractIndexEntity> indexClass,
      final String jsonVal)
      throws InstantiationException, IllegalAccessException {
    final AbstractIndexEntity abstractIndexEntity = buildAbstractIndexEntity(pojo, indexClass);

    if (abstractIndexEntity instanceof HasJsonVal) {
      ((HasJsonVal) abstractIndexEntity).setJsonVal(jsonVal);
    }
    abstractIndexEntity.setBaseId(pojo.getId());
    abstractIndexEntity.setUpdateTime(pojo.getUpdateTime());
    // todo cyril authz - namespace not empty string should be tested sooner - it's not trivial because it can come from jackson by reflection, so doing a precondition check in the setter is not enough - also in new mode maybe we will want to prevent the null namespace  
    //  for the moment we at least ensure it fails at db write time
    checkArgument(pojo.namespace() == null || !pojo.namespace().isEmpty(), "Namespace cannot be an empty string. Null namespace is allowed");
    abstractIndexEntity.setNamespace(pojo.namespace());

    return abstractIndexEntity;
  }

  private static <E extends AbstractDTO> AbstractIndexEntity buildAbstractIndexEntity(final E pojo,
      final Class<? extends AbstractIndexEntity> indexClass)
      throws InstantiationException, IllegalAccessException {
    return switch (pojo) {
      case AnomalyDTO obj -> IndexMapper.INSTANCE.toIndexEntity(obj);
      case EnumerationItemDTO obj -> IndexMapper.INSTANCE.toIndexEntity(obj);
      case RcaInvestigationDTO obj -> IndexMapper.INSTANCE.toIndexEntity(obj);
      case null, default -> buildWithLegacyModelMapper(pojo, indexClass);
    };
  }

  /**
   * TODO cyril remove modelmapper code and dependencies
   */
  private static <E extends AbstractDTO> AbstractIndexEntity buildWithLegacyModelMapper(
      final E pojo,
      final Class<? extends AbstractIndexEntity> indexClass)
      throws InstantiationException, IllegalAccessException {
    final AbstractIndexEntity abstractIndexEntity = indexClass.newInstance();
    MODEL_MAPPER.map(pojo, abstractIndexEntity);
    return abstractIndexEntity;
  }
}
