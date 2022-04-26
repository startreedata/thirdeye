/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.auth;

import static ai.startree.thirdeye.auth.OidcUtils.getExactMatchClaimSet;
import static ai.startree.thirdeye.auth.OidcUtils.getRequiredClaims;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
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

  private boolean authServerRunning = false;

  public void init(final OidcContext context, final MetricRegistry metricRegistry) {
    metricRegistry.register("authServerRunning",
        new Gauge<Integer>() {
          @Override
          public Integer getValue() {
            return authServerRunning ? 1 : 0;
          }
        });
    init(context);
  }

  public void init(final OidcContext context) {
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
      authServerRunning = true;
      return JWKSet.load(new URL(keysUrl).openStream());
    } catch (Exception e) {
      authServerRunning = false;
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
