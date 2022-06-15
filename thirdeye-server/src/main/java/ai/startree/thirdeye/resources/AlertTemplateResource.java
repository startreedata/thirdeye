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
package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.util.FileUtils.readJsonObjectsFromResourcesFolder;
import static ai.startree.thirdeye.util.ResourceUtils.respondOk;

import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.AlertTemplateApi;
import ai.startree.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertTemplateDTO;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "Alert Template", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AlertTemplateResource extends CrudResource<AlertTemplateApi, AlertTemplateDTO> {

  private static final String RESOURCES_TEMPLATES_PATH = "alert-templates";

  @Inject
  public AlertTemplateResource(
      final AlertTemplateManager alertTemplateManager) {
    super(alertTemplateManager, ImmutableMap.of());
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

  @POST
  @Path("load-defaults")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response loadRecommendedTemplates(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @FormParam("updateExisting") boolean updateExisting) {

    List<AlertTemplateApi> alertTemplates = readJsonObjectsFromResourcesFolder(
        RESOURCES_TEMPLATES_PATH,
        this.getClass(),
        AlertTemplateApi.class);
    List<AlertTemplateApi> toCreateTemplates = new ArrayList<>();
    List<AlertTemplateApi> toUpdateTemplates = new ArrayList<>();
    for (AlertTemplateApi templateApi : alertTemplates) {
      AlertTemplateDTO existingTemplate = dtoManager.findByName(templateApi.getName())
          .stream().findFirst().orElse(null);
      if (existingTemplate == null) {
        toCreateTemplates.add(templateApi);
      } else {
        templateApi.setId(existingTemplate.getId());
        toUpdateTemplates.add(templateApi);
      }
    }

    List<AlertTemplateApi> upserted = internalCreateMultiple(principal, toCreateTemplates);
    if (updateExisting) {
      List<AlertTemplateApi> updated = internalEditMultiple(principal, toUpdateTemplates);
      upserted.addAll(updated);
    }

    return respondOk(upserted);
  }
}
