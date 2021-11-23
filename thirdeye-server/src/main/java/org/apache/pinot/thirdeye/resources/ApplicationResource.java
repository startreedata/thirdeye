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
import org.apache.pinot.thirdeye.spi.api.ApplicationApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.ApplicationManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.ApplicationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Api(tags = "Application", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
public class ApplicationResource extends CrudResource<ApplicationApi, ApplicationDTO> {

  private static final Logger log = LoggerFactory.getLogger(ApplicationResource.class);

  @Inject
  public ApplicationResource(
      final ApplicationManager applicationManager) {
    super(applicationManager, ImmutableMap.of());
  }

  @Override
  protected ApplicationDTO createDto(final ThirdEyePrincipal principal, final ApplicationApi api) {
    final ApplicationDTO dto = toDto(api);
    dto.setCreatedBy(principal.getName());
    return dto;
  }

  @Override
  protected ApplicationDTO toDto(final ApplicationApi api) {
    return ApiBeanMapper.toApplicationDto(api);
  }

  @Override
  protected ApplicationApi toApi(final ApplicationDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}
