/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.util.FileUtils.getFilesFromResourcesFolder;
import static ai.startree.thirdeye.util.FileUtils.readJsonObject;
import static ai.startree.thirdeye.util.ResourceUtils.respondOk;
import static com.google.api.client.util.Preconditions.checkArgument;

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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
  @Path("onboard-recommended")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response loadRecommendedTemplates(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @FormParam("updateExisting") boolean updateExisting) {

    List<AlertTemplateApi> alertTemplates = getAlertTemplatesFromResources();
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

  private List<AlertTemplateApi> getAlertTemplatesFromResources() {
    final File[] files = getFilesFromResourcesFolder(RESOURCES_TEMPLATES_PATH,
        this.getClass().getClassLoader());
    checkArgument(files != null, "No templates file found in templates resources.");

    return Arrays.stream(files)
        .map(file -> readJsonObject(file, AlertTemplateApi.class))
        .collect(Collectors.toList());
  }
}
