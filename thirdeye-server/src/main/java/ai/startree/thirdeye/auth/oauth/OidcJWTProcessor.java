/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.auth.oauth;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import java.net.URL;
import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Singleton
public class OidcJWTProcessor extends DefaultJWTProcessor<OidcContext> {

  private final AtomicBoolean authServerRunning = new AtomicBoolean(false);

  @Inject
  public OidcJWTProcessor(final MetricRegistry metricRegistry,
      final OidcContext oidcContext) {
    setJWTClaimsSetVerifier(createJWTClaimsSetVerifier(oidcContext));
    setJWSKeySelector(this::keySelector);

    metricRegistry.register("authServerRunning",
        (Gauge<Integer>) () -> authServerRunning.get() ? 1 : 0);
  }

  private static Key toPublicKey(final JWK jwk) {
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
    } catch (final JOSEException e) {
      throw new IllegalStateException("Could not infer public key", e);
    }
  }

  private static JWTClaimsSetVerifier<OidcContext> createJWTClaimsSetVerifier(
      final OidcContext context) {
    return new DefaultJWTClaimsVerifier<>(
        context.getExactMatchClaimsSet(),
        context.getRequiredClaims());
  }

  private List<Key> keySelector(final JWSHeader header, final OidcContext c) {
    final Key key = fetchKeys(c.getKeysUrl()).getKeys().stream()
        .collect(Collectors.toMap(JWK::getKeyID, OidcJWTProcessor::toPublicKey))
        .get(header.getKeyID());
    return key != null ? Collections.singletonList(key) : Collections.emptyList();
  }

  private JWKSet fetchKeys(final String keysUrl) {
    try {
      authServerRunning.set(true);
      return JWKSet.load(new URL(keysUrl).openStream());
    } catch (final Exception e) {
      authServerRunning.set(false);
      throw new IllegalArgumentException(
          String.format("Could not retrieve keys from '%s'", keysUrl),
          e);
    }
  }
}
