package ai.startree.thirdeye.mapper;

import ai.startree.thirdeye.spi.api.RcaInvestigationApi;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RootCauseSessionMapper {

  RootCauseSessionMapper INSTANCE = Mappers.getMapper(RootCauseSessionMapper.class);

  RcaInvestigationDTO toDto(RcaInvestigationApi api);

  RcaInvestigationApi toApi(RcaInvestigationDTO dto);
}
