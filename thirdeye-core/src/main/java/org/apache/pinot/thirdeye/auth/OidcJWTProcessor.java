package org.apache.pinot.thirdeye.auth;

import static org.apache.pinot.thirdeye.auth.OidcUtils.getExactMatchClaimSet;
import static org.apache.pinot.thirdeye.auth.OidcUtils.getRequiredClaims;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import java.net.URL;
import java.security.Key;
import java.util.Collections;
import java.util.stream.Collectors;

public class OidcJWTProcessor extends DefaultJWTProcessor<OidcContext> {

  public OidcJWTProcessor(OidcContext context) {
    super();
    JWTClaimsSetVerifier verifier = new DefaultJWTClaimsVerifier(
      getExactMatchClaimSet(context),
      getRequiredClaims(context));
    setJWTClaimsSetVerifier(verifier);
    setJWSKeySelector((header, c) -> {
      Key key = fetchKeys(c.getKeysUrl()).getKeys().stream()
        .collect(Collectors.toMap(JWK::getKeyID, value -> toPublicKey(value)))
        .get(header.getKeyID());
      return key != null ? Collections.singletonList(key) : Collections.emptyList();
    });
  }

  private JWKSet fetchKeys(String keysUrl) {
    try {
      return JWKSet.load(new URL(keysUrl).openStream());
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format("Could not retrieve keys from '%s'",
        keysUrl), e);
    }
  }

  private Key toPublicKey(JWK jwk) {
    try {
      switch (jwk.getKeyType().getValue()) {
        case "EC":
          return jwk.toECKey().toECPublicKey();
        case "RSA":
          return jwk.toRSAKey().toPublicKey();
        default:
          throw new IllegalArgumentException(String.format("Unsupported key type '%s'",
            jwk.getKeyType().getValue()));
      }
    } catch (JOSEException e) {
      throw new IllegalStateException("Could not infer public key", e);
    }
  }
}
