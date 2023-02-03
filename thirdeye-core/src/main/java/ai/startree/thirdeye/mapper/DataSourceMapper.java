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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DataSourceMetaApi;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DataSourceMapper {

  DataSourceMapper INSTANCE = Mappers.getMapper(DataSourceMapper.class);

  default DataSourceDTO toBean(DataSourceApi api) {
    if (api == null) {
      return null;
    }
    final DataSourceDTO dto = new DataSourceDTO();
    dto
        .setName(api.getName())
        .setProperties(api.getProperties())
        .setType(api.getType())
        .setMetaList(optional(api.getMetaList())
            .map(l -> l.stream().map(DataSourceMapper::toDataSourceMetaBean)
                .collect(Collectors.toList()))
            .orElse(null));
    dto.setId(api.getId());
    optional(api.getAuth()).map(ApiBeanMapper::toAuthorizationConfigurationDTO)
        .ifPresent(dto::setAuth);
    return dto;
  }

  default DataSourceApi toApi(DataSourceDTO dto) {
    if (dto == null) {
      return null;
    }
    return new DataSourceApi()
        .setId(dto.getId())
        .setName(dto.getName())
        .setType(dto.getType())
        .setProperties(optional(dto.getProperties()).filter(p -> !p.isEmpty()).orElse(null))
        .setMetaList(optional(dto.getMetaList()).filter(l -> !l.isEmpty())
            .map(l -> l.stream().map(DataSourceMapper::toApi).collect(Collectors.toList()))
            .orElse(null))
        .setAuth(optional(dto.getAuth())
            .map(ApiBeanMapper::toApi).orElse(null));
  }

  private static DataSourceMetaApi toApi(final DataSourceMetaBean metaBean) {
    return new DataSourceMetaApi()
        .setClassRef(metaBean.getClassRef())
        .setProperties(metaBean.getProperties());
  }

  private static DataSourceMetaBean toDataSourceMetaBean(final DataSourceMetaApi api) {
    return new DataSourceMetaBean()
        .setClassRef(api.getClassRef())
        .setProperties(api.getProperties());
  }
}
