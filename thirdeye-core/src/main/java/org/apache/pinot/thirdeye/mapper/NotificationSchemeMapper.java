package org.apache.pinot.thirdeye.mapper;

import org.apache.pinot.thirdeye.spi.api.EmailSchemeApi;
import org.apache.pinot.thirdeye.spi.api.NotificationSchemesApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NotificationSchemeMapper {
  NotificationSchemeMapper INSTANCE = Mappers.getMapper(NotificationSchemeMapper.class);

  @Mappings({
      @Mapping(target = "emailScheme", source = "api.email"),
  })
  NotificationSchemesDto toDto(NotificationSchemesApi api);

  @Mappings({
      @Mapping(target = "email", source = "dto.emailScheme"),
  })
  NotificationSchemesApi toApi(NotificationSchemesDto dto);

  EmailSchemeDto toDto(EmailSchemeApi api);
  EmailSchemeApi toApi(EmailSchemeDto dto);
}
