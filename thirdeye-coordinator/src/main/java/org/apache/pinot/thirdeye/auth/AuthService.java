package org.apache.pinot.thirdeye.auth;

import static org.apache.pinot.thirdeye.Constants.NO_AUTH_USER;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.unauthenticatedException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import java.util.Optional;
import javax.ws.rs.NotAuthorizedException;
import org.apache.pinot.thirdeye.resources.ResourceUtils;

@Singleton
public class AuthService {

  // For use in authorization
  static final String OAUTH2_ACCESS_TOKEN = "access_token";
  static final String OAUTH2_BEARER_PREFIX = "Bearer ";

  private final Authenticator<ThirdEyeCredentials, ThirdEyePrincipal> authenticator;
  private final JwtService jwtService;
  private final boolean enabled;

  @Inject
  public AuthService(final Authenticator<ThirdEyeCredentials, ThirdEyePrincipal> authenticator,
      final AuthConfiguration authConfiguration,
      final JwtService jwtService) {
    this.authenticator = authenticator;
    this.jwtService = jwtService;
    this.enabled = authConfiguration.isEnabled();
  }

  /**
   * Checks auth headers, validates JWT token and returns principal if valid.
   *
   * @param authHeader HTTP Authorization header
   * @return principal
   * @throws NotAuthorizedException when credentials are invalid
   */
  public ThirdEyePrincipal authenticate(String authHeader) {
    if (!enabled) {
      return new ThirdEyePrincipal(NO_AUTH_USER);
    }
    if (authHeader != null && authHeader.startsWith(OAUTH2_BEARER_PREFIX)) {
      String jwtTokenString = authHeader.substring(OAUTH2_BEARER_PREFIX.length());
      final Optional<ThirdEyePrincipal> principal =
          jwtService.readPrincipal(jwtTokenString).map(p -> new ThirdEyePrincipal(p, null));
      ResourceUtils.authenticate(principal.isPresent());
      return principal.get();
    }
    throw unauthenticatedException();
  }

  /**
   * @param principal principal identifier for user. username/email
   * @param password password for the user.
   * @return the principal
   * @throws NotAuthorizedException when credentials are invalid
   */
  public ThirdEyePrincipal authenticate(String principal, String password) {
    try {
      final Optional<ThirdEyePrincipal> thirdEyePrincipal =
          authenticator.authenticate(new ThirdEyeCredentials(principal, password));
      ResourceUtils.authenticate(thirdEyePrincipal.isPresent());
      return thirdEyePrincipal.get();
    } catch (AuthenticationException e) {
      throw unauthenticatedException();
    }
  }

  public String createAccessToken(String principal) {
    return jwtService.createAccessToken(principal);
  }
}
