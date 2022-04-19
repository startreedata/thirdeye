package ai.startree.thirdeye.mapper;

import ai.startree.thirdeye.spi.api.RootCauseSessionApi;
import ai.startree.thirdeye.spi.datalayer.dto.RootCauseSessionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RootCauseSessionMapper {

  RootCauseSessionMapper INSTANCE = Mappers.getMapper(RootCauseSessionMapper.class);

  @Mappings({
      @Mapping(target = "webhook", source = "dto.webhookScheme"),
      @Mapping(target = "updatedBy", source = "api.owner"),
  })
  RootCauseSessionDTO toDto(RootCauseSessionApi api);

  RootCauseSessionApi toApi(RootCauseSessionDTO dto);
}
