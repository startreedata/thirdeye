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

package ai.startree.thirdeye.service;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class AlertTemplateService extends CrudService<AlertTemplateApi, AlertTemplateDTO> {

  @Inject
  public AlertTemplateService(final AlertTemplateManager alertTemplateManager,
      final AuthorizationManager authorizationManager) {
    super(authorizationManager, alertTemplateManager, ImmutableMap.of());
  }

  @Override
  protected AlertTemplateDTO createDto(final ThirdEyePrincipal principal,
      final AlertTemplateApi api) {
    final AlertTemplateDTO alertTemplateDTO = ApiBeanMapper.toAlertTemplateDto(api);
    alertTemplateDTO.setCreatedBy(principal.getName());
    return alertTemplateDTO;
  }

  @Override
  protected AlertTemplateDTO toDto(final AlertTemplateApi api) {
    return ApiBeanMapper.toAlertTemplateDto(api);
  }

  @Override
  protected AlertTemplateApi toApi(final AlertTemplateDTO dto) {
    return ApiBeanMapper.toAlertTemplateApi(dto);
  }


  protected List<AlertTemplateApi> loadTemplates(final ThirdEyePrincipal principal,
      final List<AlertTemplateApi> alertTemplates, final boolean updateExisting) {
    final List<AlertTemplateApi> toCreateTemplates = new ArrayList<>();
    final List<AlertTemplateApi> toUpdateTemplates = new ArrayList<>();
    for (final AlertTemplateApi templateApi : alertTemplates) {
      final AlertTemplateDTO existingTemplate = dtoManager.findByName(templateApi.getName())
          .stream()
          .findFirst()
          .orElse(null);
      if (existingTemplate == null) {
        toCreateTemplates.add(templateApi);
      } else {
        templateApi.setId(existingTemplate.getId());
        toUpdateTemplates.add(templateApi);
      }
    }

    final List<AlertTemplateApi> upserted = createMultiple(principal, toCreateTemplates);
    if (updateExisting) {
      final List<AlertTemplateApi> updated = editMultiple(principal, toUpdateTemplates);
      upserted.addAll(updated);
    }
    return upserted;
  }
}
