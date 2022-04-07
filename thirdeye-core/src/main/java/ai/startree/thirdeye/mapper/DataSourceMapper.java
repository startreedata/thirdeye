/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
            .orElse(null));
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
