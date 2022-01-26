package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.util.ResourceUtils.badRequest;
import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.util.ResourceUtils.respondOk;
import static org.apache.pinot.thirdeye.util.ResourceUtils.serverError;

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
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.spi.ThirdEyeException;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.ThirdEyeStatus;
import org.apache.pinot.thirdeye.spi.api.DataSourceApi;
import org.apache.pinot.thirdeye.spi.api.StatusApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DataSourceManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DataSourceDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeDataSource;

@Api(tags = "Data Source", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class DataSourceResource extends CrudResource<DataSourceApi, DataSourceDTO> {

  private final DataSourceCache dataSourceCache;

  @Inject
  public DataSourceResource(
      final DataSourceManager dataSourceManager,
      final DataSourceCache dataSourceCache) {
    super(dataSourceManager, ImmutableMap.of());
    this.dataSourceCache = dataSourceCache;
  }

  @Override
  protected DataSourceDTO createDto(final ThirdEyePrincipal principal,
      final DataSourceApi api) {
    final DataSourceDTO dto = toDto(api);
    dto.setCreatedBy(principal.getName());
    return dto;
  }

  @Override
  protected DataSourceDTO toDto(final DataSourceApi api) {
    return ApiBeanMapper.toDataSourceDto(api);
  }

  @Override
  protected DataSourceApi toApi(final DataSourceDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  @POST
  @Path("onboard-dataset")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response onboardDataset(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @FormParam("dataSourceName") String dataSourceName,
      @FormParam("datasetName") String datasetName) {

    ensureExists(dataSourceName, "dataSourceName is a required field");
    ensureExists(datasetName, "datasetName is a required field");

    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(dataSourceName);
    ensureExists(dataSource, ThirdEyeStatus.ERR_DATASOURCE_NOT_LOADED, dataSourceName);

    final DatasetConfigDTO datasetConfigDTO = dataSource.onboardDataset(datasetName);
    ensureExists(datasetConfigDTO, ThirdEyeStatus.ERR_DATASET_NOT_FOUND, datasetName);

    return respondOk(ApiBeanMapper.toApi(datasetConfigDTO));
  }

  @POST
  @Path("onboard-all")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response onboardAll(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @FormParam("name") String name) {

    ensureExists(name, "name is a required field");

    final ThirdEyeDataSource dataSource = dataSourceCache.getDataSource(name);
    ensureExists(dataSource, ThirdEyeStatus.ERR_DATASOURCE_NOT_LOADED, name);

    final List<DatasetConfigDTO> datasets = dataSource.onboardAll();

    return respondOk(datasets.stream()
        .map(ApiBeanMapper::toApi)
        .collect(Collectors.toList()));
  }

  @DELETE
  @Path("cache")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response clearDataSourceCache(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) throws Exception {
    dataSourceCache.clear();
    return Response.ok().build();
  }

  @GET
  @Path("status")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response status(@QueryParam("name") String name) {
    ensureExists(name, "name is a required field");
    try {
      // throws ThirdEyeException on datasource not found in DB
      // returns null when not able to load datasource
      if (dataSourceCache.getDataSource(name).validate()) {
        return respondOk(new StatusApi().setCode(ThirdEyeStatus.HEALTHY));
      }
    } catch (ThirdEyeException e) {
      throw badRequest(ThirdEyeStatus.ERR_DATASOURCE_NOT_FOUND, name);
    } catch (Exception e) {
      throw serverError(ThirdEyeStatus.ERR_DATASOURCE_UNREACHABLE, name);
    }
    throw serverError(ThirdEyeStatus.UNHEALTHY, "datasource", name);
  }
}
