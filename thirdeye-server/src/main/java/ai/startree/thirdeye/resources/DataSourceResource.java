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

import static ai.startree.thirdeye.util.ResourceUtils.badRequest;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.ResourceUtils.respondOk;
import static ai.startree.thirdeye.util.ResourceUtils.serverError;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.StatusApi;
import ai.startree.thirdeye.spi.datalayer.bao.DataSourceManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.ThirdEyeDataSource;
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
  
  @Override
  protected void deleteDto(DataSourceDTO dto) {
    super.deleteDto(dto);
    dataSourceCache.removeDataSource(dto.getName());
  }

  @Override
  protected void prepareUpdatedDto(
    final ThirdEyePrincipal principal,
    final DataSourceDTO existing,
    final DataSourceDTO updated) {
    dataSourceCache.removeDataSource(existing.getName());
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
