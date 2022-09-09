package ai.startree.thirdeye.mapper;

import ai.startree.thirdeye.spi.api.AnomalyLabelApi;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AnomalyLabelMapper {

  AnomalyLabelMapper INSTANCE = Mappers.getMapper(AnomalyLabelMapper.class);

  AnomalyLabelDTO toDto(AnomalyLabelApi api);

  AnomalyLabelApi toApi(AnomalyLabelDTO dto);
}
