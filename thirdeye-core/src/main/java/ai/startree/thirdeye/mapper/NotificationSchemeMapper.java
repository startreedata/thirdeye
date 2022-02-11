/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.mapper;

import ai.startree.thirdeye.spi.api.EmailSchemeApi;
import ai.startree.thirdeye.spi.api.NotificationSchemesApi;
import ai.startree.thirdeye.spi.api.WebhookSchemeApi;
import ai.startree.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import ai.startree.thirdeye.spi.datalayer.dto.WebhookSchemeDto;
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
