package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.authenticate;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.swagger.annotations.ApiParam;
import java.util.Optional;
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
      authenticate(grantType != null);

      Optional<ThirdEyePrincipal> thirdEyePrincipal = Optional.empty();
      switch (grantType) {
        case PASSWORD:
          thirdEyePrincipal = authService.authenticate(principal, password);
          break;
        case AUTHORIZATION_CODE:
          thirdEyePrincipal = authService.authenticate(authHeader);
      }
      authenticate(thirdEyePrincipal.isPresent());

      return Response.ok(authApi(thirdEyePrincipal.get())).build();
    } catch (NotAuthorizedException e) {
      throw e;
    } catch (Exception e) {
      throw new NotAuthorizedException("Authentication Failure");
    }
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
