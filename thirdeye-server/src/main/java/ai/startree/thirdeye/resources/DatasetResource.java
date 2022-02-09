package ai.startree.thirdeye.resources;

import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
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

@Api(tags = "Dataset", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class DatasetResource extends CrudResource<DatasetApi, DatasetConfigDTO> {

  @Inject
  public DatasetResource(
      final DatasetConfigManager datasetConfigManager) {
    super(datasetConfigManager, ImmutableMap.of());
  }

  @Override
  protected DatasetConfigDTO createDto(final ThirdEyePrincipal principal,
      final DatasetApi api) {
    final DatasetConfigDTO dto = toDto(api);
    dto.setCreatedBy(principal.getName());
    return dto;
  }

  @Override
  protected DatasetConfigDTO toDto(final DatasetApi api) {
    return ApiBeanMapper.toDatasetConfigDto(api);
  }

  @Override
  protected DatasetApi toApi(final DatasetConfigDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }
}
