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

import static ai.startree.thirdeye.util.ResourceUtils.serverError;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.service.EntityService;
import ai.startree.thirdeye.spi.ThirdEyeStatus;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Entity")
@SecurityRequirement(name = "oauth")
@OpenAPIDefinition(security = {@SecurityRequirement(name = "oauth")})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
public class EntityResource {

  private final EntityService entityService;

  @Inject
  public EntityResource(final EntityService entityService) {
    this.entityService = entityService;
  }

  @GET
  @Path("{id}")
  public Response getRawEntity(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @PathParam("id") Long id) {
    return Response.ok(entityService.getRawEntity(principal, id)).build();
  }

  @GET
  @Path("types")
  public Response listEntities(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal) {
    return Response.ok(entityService.countEntitiesByType(principal)).build();
  }

  @GET
  @Path("types/{bean_class}/info")
  public Response getEntityInfo(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @PathParam("bean_class") String beanClass) {
    try {
      return Response.ok(entityService.getBeanFields(principal, beanClass)).build();
    } catch (Exception e) {
      throw serverError(ThirdEyeStatus.ERR_UNKNOWN, e);
    }
  }

  @GET
  @Path("types/{bean_class}")
  public Response getEntity(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal,
      @PathParam("bean_class") String beanClassRef, @Context UriInfo uriInfo) {
    try {
      final List<? extends AbstractDTO> abstractBeans = entityService.getEntity(principal,
          beanClassRef, uriInfo);
      return Response.ok(abstractBeans).build();
    } catch (Exception e) {
      throw serverError(ThirdEyeStatus.ERR_UNKNOWN, e);
    }
  }
}
