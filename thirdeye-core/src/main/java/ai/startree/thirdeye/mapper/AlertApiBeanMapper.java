/*
 * Copyright 2022 StarTree Inc
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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.AlertApi;
import ai.startree.thirdeye.spi.api.UserApi;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertApiBeanMapper {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertApiBeanMapper.class);

  @Inject
  public AlertApiBeanMapper() {
  }

  public AlertDTO toAlertDTO(final AlertApi api) {
    final AlertDTO dto = new AlertDTO();

    dto.setName(api.getName());
    dto.setDescription(api.getDescription());
    dto.setActive(optional(api.getActive()).orElse(true));
    dto.setCron(api.getCron());
    dto.setLastTimestamp(optional(api.getLastTimestamp())
        .map(d -> d.toInstant().toEpochMilli())
        .orElse(0L));
    dto.setUpdateTime(new Timestamp(System.currentTimeMillis()));

    optional(api.getTemplate())
        .map(ApiBeanMapper::toAlertTemplateDto)
        .ifPresent(dto::setTemplate);

    optional(api.getTemplateProperties())
        .ifPresent(dto::setTemplateProperties);

    // May not get updated while edits
    optional(api.getOwner())
        .map(UserApi::getPrincipal)
        .ifPresent(dto::setCreatedBy);

    return dto;
  }
}
