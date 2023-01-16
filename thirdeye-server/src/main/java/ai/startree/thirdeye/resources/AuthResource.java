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

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.AuthApi;
import ai.startree.thirdeye.spi.api.UserApi;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Singleton;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Singleton
@Api(tags = "Auth", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

  @Timed
  @Path("/login")
  @POST
  public Response login(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal
  ) {
      return respondOk(authApi(principal));
  }

  private AuthApi authApi(final ThirdEyePrincipal thirdEyePrincipal) {
    final String principal = thirdEyePrincipal.getName();
    return new AuthApi()
        .setUser(new UserApi()
            .setPrincipal(principal))
        .setAccessToken(NO_AUTH_USER);
  }

  @Timed
  @Path("/logout")
  @POST
  public Response logout(@ApiParam(hidden = true) @Auth ThirdEyePrincipal principal) {
    // TODO spyne to be implemented.
    return Response.ok().build();
  }
}
