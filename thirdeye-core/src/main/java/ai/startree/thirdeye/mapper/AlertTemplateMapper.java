package ai.startree.thirdeye.mapper;

import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AlertTemplateMapper {

  AlertTemplateMapper INSTANCE = Mappers.getMapper(AlertTemplateMapper.class);

  AlertTemplateDTO toBean(AlertTemplateApi api);

  AlertTemplateApi toApi(AlertTemplateDTO dto);
}
