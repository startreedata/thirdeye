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

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.service.DatabaseAdminService;
import ai.startree.thirdeye.spi.Constants;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@SecurityRequirement(name="oauth")
@SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth"),
    @SecurityRequirement(name = Constants.NAMESPACE_SECURITY)
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@SecurityScheme(name = Constants.NAMESPACE_SECURITY, type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = Constants.NAMESPACE_HTTP_HEADER)
@Produces(MediaType.APPLICATION_JSON)
public class DatabaseAdminResource {

  private final DatabaseAdminService databaseAdminService;

  @Inject
  public DatabaseAdminResource(final DatabaseAdminService databaseAdminService) {
    this.databaseAdminService = databaseAdminService;
  }

  @GET
  @Path("tables")
  public Response getTables(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal)
      throws Exception {
    return Response.ok(databaseAdminService.getTables(principal)).build();
  }

  @POST
  @Path("create-all-tables")
  public Response createAllTables(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal)
      throws Exception {
    databaseAdminService.createAllTables(principal);
    return Response.ok().build();
  }

  @DELETE
  @Path("truncate-all-tables")
  public Response deleteAllData(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal)
      throws Exception {
    databaseAdminService.deleteAllData(principal);
    return Response.ok().build();
  }

  @DELETE
  @Path("drop-all-tables")
  public Response deleteAllTables(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal)
      throws Exception {
    databaseAdminService.dropAllTables(principal);
    return Response.ok().build();
  }
}
