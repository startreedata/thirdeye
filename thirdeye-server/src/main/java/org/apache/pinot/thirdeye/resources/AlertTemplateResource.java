package org.apache.pinot.thirdeye.resources;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.AlertTemplateApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertTemplateManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertTemplateDTO;

@Api(tags = "Alert Template", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AlertTemplateResource extends CrudResource<AlertTemplateApi, AlertTemplateDTO> {

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
}
