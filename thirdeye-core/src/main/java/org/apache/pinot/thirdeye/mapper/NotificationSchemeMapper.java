package org.apache.pinot.thirdeye.mapper;

import org.apache.pinot.thirdeye.spi.api.EmailSchemeApi;
import org.apache.pinot.thirdeye.spi.api.NotificationSchemesApi;
import org.apache.pinot.thirdeye.spi.api.WebhookSchemeApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import org.apache.pinot.thirdeye.spi.datalayer.dto.WebhookSchemeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NotificationSchemeMapper {
  NotificationSchemeMapper INSTANCE = Mappers.getMapper(NotificationSchemeMapper.class);

  @Mappings({
      @Mapping(target = "webhookScheme", source = "api.webhook"),
      @Mapping(target = "emailScheme", source = "api.email"),
  })
  NotificationSchemesDto toDto(NotificationSchemesApi api);

  @Mappings({
      @Mapping(target = "webhook", source = "dto.webhookScheme"),
      @Mapping(target = "email", source = "dto.emailScheme"),
  })
  NotificationSchemesApi toApi(NotificationSchemesDto dto);

  WebhookSchemeDto toDto(WebhookSchemeApi api);
  WebhookSchemeApi toApi(WebhookSchemeDto dto);

  EmailSchemeDto toDto(EmailSchemeApi api);
  EmailSchemeApi toApi(EmailSchemeDto dto);
}
