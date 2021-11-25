package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.spi.Constants.NO_AUTH_USER;
import static org.apache.pinot.thirdeye.util.ResourceUtils.respondOk;

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
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.AuthApi;
import org.apache.pinot.thirdeye.spi.api.UserApi;
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