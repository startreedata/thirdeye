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

import static ai.startree.thirdeye.spi.Constants.NO_AUTH_USER;
import static ai.startree.thirdeye.util.ResourceUtils.respondOk;

import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.spi.api.AuthApi;
import ai.startree.thirdeye.spi.api.UserApi;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Singleton;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
@Tag(name = "Auth")
@SecurityRequirement(name="oauth")
@OpenAPIDefinition(security = {
    @SecurityRequirement(name = "oauth")
})
@SecurityScheme(name = "oauth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

  private static AuthApi authApi(final ThirdEyeServerPrincipal thirdEyePrincipal) {
    final String principal = thirdEyePrincipal.getName();
    return new AuthApi()
        .setUser(new UserApi()
            .setPrincipal(principal))
        .setAccessToken(NO_AUTH_USER);
  }

  @Timed
  @Path("/login")
  @POST
  public Response login(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal) {
    return respondOk(authApi(principal));
  }

  @Timed
  @Path("/logout")
  @POST
  public Response logout(@Parameter(hidden = true) @Auth ThirdEyeServerPrincipal principal) {
    // TODO spyne to be implemented.
    return Response.ok().build();
  }
}
