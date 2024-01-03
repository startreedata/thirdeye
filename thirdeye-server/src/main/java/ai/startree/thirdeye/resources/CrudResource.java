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

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.ResourceUtils.respondOk;
import static ai.startree.thirdeye.util.ResourceUtils.statusResponse;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.service.CrudService;
import ai.startree.thirdeye.spi.api.ThirdEyeCrudApi;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CrudResource<ApiT extends ThirdEyeCrudApi<ApiT>, DtoT extends AbstractDTO> {

  private static final Logger log = LoggerFactory.getLogger(CrudResource.class);

  protected final CrudService<ApiT, DtoT> crudService;

  public CrudResource(final CrudService<ApiT, DtoT> crudService) {
    this.crudService = crudService;
  }

  @GET
  @Timed
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
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @PathParam("id") Long id) {
    return respondOk(crudService.get(principal, id));
  }

  @GET
  @Path("name/{name}")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @PathParam("name") String name) {
    return respondOk(crudService.findByName(principal, name));
  }

  @POST
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response createMultiple(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      List<ApiT> list) {
    ensureExists(list, "Invalid request");

    return respondOk(crudService.createMultiple(principal, list));
  }

  @PUT
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response editMultiple(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      List<ApiT> list) {

    return respondOk(crudService.editMultiple(principal, list));
  }

  @DELETE
  @Path("{id}")
  @Timed
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
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteAll(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal) {
    crudService.deleteAll(principal);
    return Response.ok().build();
  }

  @GET
  @Path("/count")
  @Timed
  public Response countWithPredicate(
      @Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @Context UriInfo uriInfo
  ) {
    return Response.ok(crudService.count(uriInfo.getQueryParameters())).build();
  }
}
