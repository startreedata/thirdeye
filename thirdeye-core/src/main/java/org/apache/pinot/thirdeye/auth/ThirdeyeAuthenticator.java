package org.apache.pinot.thirdeye.auth;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Optional;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdeyeAuthenticator implements Authenticator<String, ThirdEyePrincipal> {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdeyeAuthenticator.class);
  private AuthConfiguration config;

  public ThirdeyeAuthenticator(AuthConfiguration config) {
    this.config = config;
  }

  @Override
  public Optional<ThirdEyePrincipal> authenticate(final String s) throws AuthenticationException {
    try {
      Jwk key = getJWK(extractKID(s));
      Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) key.getPublicKey(), null);
      JWTVerifier verifier = JWT.require(algorithm)
          .withIssuer(config.getOAuthConfig().getIssuer())
          .build();
      DecodedJWT jwt = verifier.verify(s);
      return Optional.of(new ThirdEyePrincipal(jwt));
    } catch (Exception exception){
      LOG.info("Authentication failed. ", exception);
    }
    return Optional.empty();
  }

  private Jwk getJWK(String kid){
    try {
      JwkProvider provider = new UrlJwkProvider(new URL(config.getOAuthConfig().getBaseUrl()));
      return provider.get(kid);
    } catch (MalformedURLException | JwkException e) {
      e.printStackTrace();
      return null;
    }
  }

  private String extractKID(String token){
    Base64.Decoder decoder = Base64.getDecoder();
    try {
      String json = new String(decoder.decode(token.split("\\.")[0]));
      JsonNode payload = new ObjectMapper().readTree(json);
      return payload.get("kid").getTextValue();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
