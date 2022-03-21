package org.apache.pinot.thirdeye.auth;

import static org.apache.pinot.thirdeye.auth.OidcUtils.getExactMatchClaimSet;
import static org.apache.pinot.thirdeye.auth.OidcUtils.getRequiredClaims;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class OidcJWTProcessor extends DefaultJWTProcessor<OidcContext> {
  public OidcJWTProcessor(Collection<JWK> keys, OidcContext context) {
    super();
    Map<String, Key> keyMap = keys.stream().collect(Collectors.toMap(JWK::getKeyID, OidcJWTProcessor::toPublicKey));
    JWTClaimsSetVerifier verifier = new DefaultJWTClaimsVerifier(
        getExactMatchClaimSet(context),
        getRequiredClaims(context));
    setJWTClaimsSetVerifier(verifier);
    setJWSKeySelector((header, c) -> {
      Key key = keyMap.get(header.getKeyID());
      return key != null ? Collections.singletonList(key) : Collections.emptyList();
    });
  }

  private static Key toPublicKey(JWK jwk) {
    try {
      switch (jwk.getKeyType().getValue()) {
        case "EC":
          return jwk.toECKey().toECPublicKey();
        case "RSA":
          return jwk.toRSAKey().toPublicKey();
        default:
          throw new IllegalArgumentException(String.format("Unsupported key type '%s'", jwk.getKeyType().getValue()));
      }
    } catch (JOSEException e) {
      throw new IllegalStateException("Could not infer public key", e);
    }
  }
}
