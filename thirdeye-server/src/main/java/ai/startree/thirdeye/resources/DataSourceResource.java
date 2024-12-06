/*
 * Copyright 2024 StarTree Inc
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

import static ai.startree.thirdeye.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.ResourceUtils.respondOk;
import static ai.startree.thirdeye.ResourceUtils.statusResponse;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DATASOURCE_VALIDATION_FAILED;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.service.DataSourceService;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.ThirdEyeException;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.api.DataSourceApi;
import ai.startree.thirdeye.spi.api.DatasetApi;
import ai.startree.thirdeye.spi.api.StatusApi;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import io.dropwizard.auth.Auth;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "Data Source")
@SecurityRequirement(name = "oauth")
@SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth"),
    @SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@SecurityScheme(name = Constants.NAMESPACE_SECURITY, type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = Constants.NAMESPACE_HTTP_HEADER)
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DataSourceResource extends CrudResource<DataSourceApi, DataSourceDTO> {
  
  private final Logger LOG = LoggerFactory.getLogger(DataSourceResource.class);

  private final DataSourceService dataSourceService;

  @Inject
  public DataSourceResource(final DataSourceService dataSourceService) {
    super(dataSourceService);
    this.dataSourceService = dataSourceService;
  }

  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @GET
  @Path("/{id}/datasets")
  public Response getDatasetsById(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @PathParam("id") Long id) {
    return respondOk(dataSourceService.getDatasets(principal, id));
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("onboard-dataset")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response onboardDataset(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @FormParam("dataSourceName") String dataSourceName,
      @FormParam("dataSourceId") Long dataSourceId,
      @FormParam("datasetName") String datasetName) {
    ensureExists(datasetName, "datasetName is a required field");
    // ensureExists(dataSourceId, "dataSourceId is a required field");
    // temporary migration code todo cyril only use id once frontend has migrated
    final boolean nameIsUsed = dataSourceName != null && dataSourceId == null;
    final boolean idIsUsed = dataSourceName == null && dataSourceId != null;
    checkArgument(nameIsUsed || idIsUsed, "Either name or id parameters must be set.");
    if (nameIsUsed) {
      throw new IllegalArgumentException("Using deprecated onboard-dataset with param 'dataSourceName'. This is not supported anymore. Please use id.");
    } else {
      return respondOk(dataSourceService.onboardDataset(principal, dataSourceId, datasetName));
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("onboard-all")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response onboardAll(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @Deprecated @FormParam("name") String name, @FormParam("id") Long id) {
    // ensureExists(id, "id is a required field");
    // temporary migration code todo cyril  only use id once frontend has migrated
    final boolean nameIsUsed = name != null && id == null;
    final boolean idIsUsed = name == null && id != null;
    checkArgument(nameIsUsed || idIsUsed, "Either name or id parameters must be set.");
    final List<DatasetApi> onboarded;
    if (nameIsUsed) {
      throw new IllegalArgumentException("Using deprecated onboard-all with param 'name'. This is not supported anymore. Please use id.");
    } else {
      onboarded = dataSourceService.onboardAll(principal, id); 
    }
    return respondOk(onboarded);
  }

  @GET
  @Path("recommend")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response recommendConfiguration(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal) {
    return respondOk(dataSourceService.recommend(principal));
  }

  @DELETE
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("offboard-all")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response offboardAll(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @Deprecated @FormParam("name") String name, @FormParam("id") Long id) {
    // ensureExists(id, "id is a required field");
    // temporary migration code todo cyril  only use id once frontend has migrated
    final boolean nameIsUsed = name != null && id == null;
    final boolean idIsUsed = name == null && id != null;
    checkArgument(nameIsUsed || idIsUsed, "Either name or id parameters must be set.");
    if (nameIsUsed) {
      throw new IllegalArgumentException("Using deprecated offboard-all with param 'name'. This is not supported anymore. Please use id.");
    } else {
      return respondOk(dataSourceService.offboardAll(principal, id));
    }
  }

  @DELETE
  @Path("cache")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response clearDataSourceCache(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal) {
    dataSourceService.clearDataSourceCache(principal);
    return Response.ok().build();
  }

  @GET
  @Path("validate")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response validate(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @Deprecated @QueryParam("name") String name, @QueryParam("id") Long id) {
    // do not use name anymore - use id instead
    // ensureExists(id, "id is a required field");
    // temporary migration code todo cyril  only use id once frontend has migrated
    final boolean nameIsUsed = name != null && id == null;
    final boolean idIsUsed = name == null && id != null;
    checkArgument(nameIsUsed || idIsUsed, "Either name or id parameters must be set.");
    try {
      if (nameIsUsed) {
        throw new IllegalArgumentException("Using deprecated validate with param 'name'. This is not supported anymore. Please use id.");
      } else {
        if (dataSourceService.validate(principal, id)) {
          return respondOk(new StatusApi().setCode(ThirdEyeStatus.OK));
        }
      }
    } catch (ThirdEyeException e) {
      return respondOk(statusResponse(e));
    } catch (Exception e) {
      return respondOk(statusResponse(ERR_DATASOURCE_VALIDATION_FAILED, id, e.getMessage()));
    }
    return respondOk(statusResponse(ERR_DATASOURCE_VALIDATION_FAILED, id, "Unknown error"));
  }
}
