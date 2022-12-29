package ai.startree.thirdeye.plugins.bootstrap.opencore;

import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import org.mapstruct.Mapper;
import org.mapstruct.control.DeepClone;
import org.mapstruct.factory.Mappers;

@Mapper(mappingControl = DeepClone.class)
public interface AlertTemplateMapper {

  AlertTemplateMapper INSTANCE = Mappers.getMapper(AlertTemplateMapper.class);

  AlertTemplateApi deepClone(AlertTemplateApi src);
}
