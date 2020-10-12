package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.unauthenticatedException;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.api.AuthApi;
import org.apache.pinot.thirdeye.api.UserApi;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.auth.GrantType;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;

@Singleton
@Api(tags = "Auth")
public class AuthResource {

  private final AuthService authService;

  @Inject
  public AuthResource(final AuthService authService) {
    this.authService = authService;
  }

  @Timed
  @Path("/login")
  @POST
  public Response login(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @ApiParam(defaultValue = "password", allowableValues = "password, authorization_code")
      @FormParam("grant_type") String grantTypeStr,
      @FormParam("principal") String principal,
      @FormParam("password") String password
  ) {
    try {
      final GrantType grantType = GrantType.fromValue(grantTypeStr);
      ResourceUtils.authenticate(grantType != null);

      final ThirdEyePrincipal thirdEyePrincipal = authenticate(
          authHeader,
          grantType,
          principal,
          password
      );

      return Response.ok(authApi(thirdEyePrincipal)).build();
    } catch (NotAuthorizedException e) {
      throw e;
    } catch (Exception e) {
      throw unauthenticatedException();
    }
  }

  private ThirdEyePrincipal authenticate(
      final String authHeader,
      final GrantType grantType,
      final String principal,
      final String password) {
    switch (grantType) {
      case PASSWORD:
        return authService.authenticate(principal, password);
      case AUTHORIZATION_CODE:
        return authService.authenticate(authHeader);
    }
    throw unauthenticatedException();
  }

  private AuthApi authApi(final ThirdEyePrincipal thirdEyePrincipal) {
    final String principal = thirdEyePrincipal.getName();
    final String accessToken = authService.createAccessToken(principal);
    return new AuthApi()
        .setUser(new UserApi()
            .setPrincipal(principal))
        .setAccessToken(accessToken);
  }

  @Timed
  @Path("/logout")
  @POST
  public Response logout() {
    // TODO spyne to be implemented.
    return Response.ok().build();
  }
}
