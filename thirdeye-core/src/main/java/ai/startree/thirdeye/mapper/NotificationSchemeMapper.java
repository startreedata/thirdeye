/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
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
