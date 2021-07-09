package org.apache.pinot.thirdeye.mapper;

import org.apache.pinot.thirdeye.spi.api.AlertTemplateApi;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.AlertTemplateBean;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AlertTemplateMapper {

  AlertTemplateMapper INSTANCE = Mappers.getMapper(AlertTemplateMapper.class);

  AlertTemplateBean toBean(AlertTemplateApi api);

  AlertTemplateApi toApi(AlertTemplateBean bean);
}
