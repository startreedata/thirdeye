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
package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DATASOURCE_VALIDATION_FAILED;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.ResourceUtils.respondOk;
import static ai.startree.thirdeye.util.ResourceUtils.statusResponse;

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.service.DataSourceService;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.StatusApi;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

  private final DataSourceService dataSourceService;

  @Inject
  public DataSourceResource(final DataSourceService dataSourceService) {
    super(dataSourceService);
    this.dataSourceService = dataSourceService;
  }

  @Timed
  @GET
  @Path("/name/{name}/datasets")
  public Response getDatasets(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @PathParam("name") String name) {
    return respondOk(dataSourceService.getDatasets(name));
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

    return respondOk(dataSourceService.onboardDataset(dataSourceName, datasetName));
  }

  @POST
  @Path("onboard-all")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response onboardAll(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @FormParam("name") String name) {

    ensureExists(name, "name is a required field");
    final List<DatasetApi> onboarded = dataSourceService.onboardAll(name);
    return respondOk(onboarded);
  }

  @DELETE
  @Path("offboard-all")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response offboardAll(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @FormParam("name") String name) {
    ensureExists(name, "name is a required field");
    return respondOk(dataSourceService.offboardAll(name));
  }

  @DELETE
  @Path("cache")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response clearDataSourceCache(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) {
    dataSourceService.clearDataSourceCache();
    return Response.ok().build();
  }

  @GET
  @Path("validate")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response validate(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @QueryParam("name") String name) {
    ensureExists(name, "name is a required field");
    try {
      // throws ThirdEyeException on datasource not found in DB
      // returns null when not able to load datasource
      if (dataSourceService.validate(name)) {
        return respondOk(new StatusApi().setCode(ThirdEyeStatus.OK));
      }
    } catch (ThirdEyeException e) {
      return respondOk(statusResponse(e));
    } catch (Exception e) {
      return respondOk(statusResponse(ERR_DATASOURCE_VALIDATION_FAILED, name, e.getMessage()));
    }
    return respondOk(statusResponse(ERR_DATASOURCE_VALIDATION_FAILED, name, "Unknown error"));
  }
}
