package org.apache.pinot.thirdeye.auth;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import java.util.Optional;
import javax.ws.rs.NotAuthorizedException;

@Singleton
public class AuthService {

  // For use in authorization
  static final String OAUTH2_ACCESS_TOKEN = "access_token";
  static final String OAUTH2_BEARER_PREFIX = "Bearer ";

  private final Authenticator<ThirdEyeCredentials, ThirdEyePrincipal> authenticator;
  private final JwtService jwtService;

  @Inject
  public AuthService(
      final Authenticator<ThirdEyeCredentials, ThirdEyePrincipal> authenticator,
      final JwtService jwtService) {
    this.authenticator = authenticator;
    this.jwtService = jwtService;
  }

  /**
   * Checks auth headers, validates JWT token and returns username if valid.
   *
   * @param authHeader HTTP Authorization header
   * @return username if valid token found, else empty.
   */
  public Optional<ThirdEyePrincipal> authenticate(String authHeader) {
    if (authHeader != null && authHeader.startsWith(OAUTH2_BEARER_PREFIX)) {
      String jwtTokenString = authHeader.substring(OAUTH2_BEARER_PREFIX.length());
      return jwtService
          .readPrincipal(jwtTokenString)
          .map(p -> new ThirdEyePrincipal(p, null));
    }
    return Optional.empty();
  }

  public String createAccessToken(String principal) {
    return jwtService.createAccessToken(principal);
  }

  public Optional<ThirdEyePrincipal> authenticate(String principal, String password) {
    try {
      return authenticator.authenticate(new ThirdEyeCredentials(principal, password));
    } catch (AuthenticationException e) {
      throw new NotAuthorizedException("Authentication Failure");
    }
  }
}
