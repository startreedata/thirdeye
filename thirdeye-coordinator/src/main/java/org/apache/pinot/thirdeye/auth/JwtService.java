package org.apache.pinot.thirdeye.auth;

import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.inject.Singleton;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class JwtService {

  private static final Logger log = LoggerFactory.getLogger(JwtService.class);
  private static final String PRINCIPAL = "principal";

  private boolean isEnabled;
  private final String issuer;
  private final Algorithm signingAlgorithm;
  private final JWTVerifier verifier;
  private final Duration accessTokenExpiry;

  @Inject
  public JwtService(JwtConfiguration config) {
    isEnabled = config.isEnabled();
    issuer = config.getIssuer();
    signingAlgorithm = Algorithm.HMAC512(config.getSigningKey());
    accessTokenExpiry = config.getAccessTokenExpiry();
    verifier = JWT.require(signingAlgorithm).withIssuer(issuer).build();
  }

  public String createAccessToken(String principal) {
    try {
      final JWTCreator.Builder builder =
          JWT.create().withIssuer(issuer).withExpiresAt(Date.from(new Date().toInstant().plus(accessTokenExpiry)));

      optional(principal).ifPresent(v -> builder.withClaim(PRINCIPAL, v));

      return builder.sign(signingAlgorithm);
    } catch (JWTCreationException e) {
      log.error("Invalid Signing configuration / Couldn't convert Claims.", e);
    }
    return null;
  }

  /**
   * Validate a JWT token.
   *
   * @param jwtToken token string
   * @return username of the token owner
   */
  public Optional<String> readPrincipal(String jwtToken) {
    if (jwtToken == null) {
      return Optional.empty();
    }
    try {
      final DecodedJWT jwt = verifier.verify(jwtToken);
      return optional(jwt.getClaim(PRINCIPAL)).map(Claim::asString);
    } catch (RuntimeException e) {
      log.error(String.format("Invalid signature/claims. errorMsg: '%s' Token: %s", e.getMessage(), jwtToken));
    }
    return Optional.empty();
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void setEnabled(boolean enabled) {
    isEnabled = enabled;
  }
}
