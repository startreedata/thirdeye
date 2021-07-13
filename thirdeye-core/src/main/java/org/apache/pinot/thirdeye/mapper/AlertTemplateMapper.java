package org.apache.pinot.thirdeye.mapper;

import org.apache.pinot.thirdeye.spi.api.AlertTemplateApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AlertTemplateMapper {

  AlertTemplateMapper INSTANCE = Mappers.getMapper(AlertTemplateMapper.class);

  AlertTemplateDTO toBean(AlertTemplateApi api);

  AlertTemplateApi toApi(AlertTemplateDTO dto);
}
