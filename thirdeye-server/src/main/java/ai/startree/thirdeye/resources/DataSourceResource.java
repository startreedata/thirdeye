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

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_DATASOURCE_VALIDATION_FAILED;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.ResourceUtils.respondOk;
import static ai.startree.thirdeye.util.ResourceUtils.statusResponse;
import static com.google.common.base.Preconditions.checkArgument;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.service.DataSourceService;
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
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "Data Source")
@SecurityRequirement(name="oauth")
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth")
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
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
  @Path("/name/{name}/datasets")
  @Deprecated // use getDatasetsById
  public Response getDatasets(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @PathParam("name") String name) {
    LOG.warn("Using deprecated endpoint /name/<name>/datasets. Prefer /<id>/datasets");
    return respondOk(dataSourceService.getDatasets(principal, name));
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
      LOG.warn("Using deprecated onboard-dataset with param 'dataSourceName'. Prefer id.");
      return respondOk(dataSourceService.onboardDataset(principal, dataSourceName, datasetName));
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
      LOG.warn("Using deprecated onboard-all with param 'name'. Prefer id.");
      onboarded = dataSourceService.onboardAll(principal, name);
    } else {
      onboarded = dataSourceService.onboardAll(principal, id); 
    }
    return respondOk(onboarded);
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
      LOG.warn("Using deprecated offboard-all with param 'name'. Prefer id.");
      return respondOk(dataSourceService.offboardAll(principal, name));  
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
        LOG.warn("Using deprecated validate with param 'name'. Prefer id.");
        // throws ThirdEyeException on datasource not found in DB
        // returns null when not able to load datasource
        if (dataSourceService.validate(principal, name)) {
          return respondOk(new StatusApi().setCode(ThirdEyeStatus.OK));
        }  
      } else {
        if (dataSourceService.validate(principal, id)) {
          return respondOk(new StatusApi().setCode(ThirdEyeStatus.OK));
        }
      }
    } catch (ThirdEyeException e) {
      return respondOk(statusResponse(e));
    } catch (Exception e) {
      return respondOk(statusResponse(ERR_DATASOURCE_VALIDATION_FAILED, name, e.getMessage()));
    }
    return respondOk(statusResponse(ERR_DATASOURCE_VALIDATION_FAILED, name, "Unknown error"));
  }
}
