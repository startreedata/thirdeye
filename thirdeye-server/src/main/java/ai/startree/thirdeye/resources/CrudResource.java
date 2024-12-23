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
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.service.CrudService;
import ai.startree.thirdeye.spi.api.ThirdEyeCrudApi;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import io.dropwizard.auth.Auth;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CrudResource<ApiT extends ThirdEyeCrudApi<ApiT>, DtoT extends AbstractDTO> {

  private static final Logger LOG = LoggerFactory.getLogger(CrudResource.class);

  protected final CrudService<ApiT, DtoT> crudService;

  public CrudResource(final CrudService<ApiT, DtoT> crudService) {
    this.crudService = crudService;
  }

  @GET
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response list(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @Context UriInfo uriInfo
  ) {
    final MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
    return respondOk(crudService.list(principal, queryParameters));
  }

  @GET
  @Path("{id}")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @PathParam("id") Long id) {
    return respondOk(crudService.get(principal, id));
  }

  @POST
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response createMultiple(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      List<ApiT> list) {
    ensureExists(list, "Invalid request");

    return respondOk(crudService.createMultiple(principal, list));
  }

  @PUT
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response editMultiple(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      List<ApiT> list) {

    return respondOk(crudService.editMultiple(principal, list));
  }

  @DELETE
  @Path("{id}")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @PathParam("id") Long id) {
    final ApiT apiT = crudService.delete(principal, id);
    if (apiT != null) {
      return respondOk(apiT);
    }
    return respondOk(statusResponse(ERR_OBJECT_DOES_NOT_EXIST, id));
  }

  @DELETE
  @Path("/all")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteAll(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal) {
    crudService.deleteAll(principal);
    return Response.ok().build();
  }

  @GET
  @Path("/count")
  @Timed(percentiles = {0.5, 0.75, 0.90, 0.95, 0.98, 0.99, 0.999})
  public Response countWithPredicate(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @Context UriInfo uriInfo
  ) {
    return Response.ok(crudService.count(principal, uriInfo.getQueryParameters())).build();
  }
}
