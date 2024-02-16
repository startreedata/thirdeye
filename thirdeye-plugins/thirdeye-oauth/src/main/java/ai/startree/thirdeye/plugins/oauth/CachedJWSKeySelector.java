/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.plugins.oauth;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import java.net.URL;
import java.security.Key;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class CachedJWSKeySelector implements JWSKeySelector<OidcContext> {

  private static final int CACHE_SIZE = 1; // only one issuer supported

  private final String keysUrl;

  private final AtomicBoolean authServerRunning = new AtomicBoolean(false);
  private final LoadingCache<String, Map<String, Key>> keyCache;

  @Inject
  public CachedJWSKeySelector(final OAuthConfiguration config) {
    this.keysUrl = requireNonNull(config.getKeysUrl(), "keysUrl must not be null");
    final long ttl = requireNonNull(config.getCache(), "cache config cannot be null").getTtl();

    this.keyCache = CacheBuilder.newBuilder()
        .maximumSize(CACHE_SIZE)
        .expireAfterWrite(ttl, TimeUnit.MILLISECONDS)
        .build(new KeyCacheLoader(authServerRunning));

    Gauge.builder("thirdeye_auth_running", () -> authServerRunning.get() ? 1 : 0)
        .tag("type", "oauth").register(Metrics.globalRegistry);
  }

  // NOTE: is there a better way with little complexity?
  private static Key toPublicKey(JWK jwk) {
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

  @Override
  public List<? extends Key> selectJWSKeys(JWSHeader jwsHeader, OidcContext oidcContext)
      throws KeySourceException {
    try {
      Key key = keyCache.get(this.keysUrl).get(jwsHeader.getKeyID());
      return key != null ? Collections.singletonList(key) : Collections.emptyList();
    } catch (ExecutionException e) {
      throw new KeySourceException(String.format("Could not retrieve key set from '%s'",
          this.keysUrl), e);
    }
  }

  private static class KeyCacheLoader extends CacheLoader<String, Map<String, Key>> {

    private final AtomicBoolean authServerRunning;

    public KeyCacheLoader(final AtomicBoolean authServerRunning) {
      this.authServerRunning = authServerRunning;
    }

    @Override
    public Map<String, Key> load(String keysUrl) {
      return fetchKeys(keysUrl).getKeys().stream()
          .collect(toMap(JWK::getKeyID, CachedJWSKeySelector::toPublicKey));
    }

    private JWKSet fetchKeys(String keysUrl) {
      try {
        final JWKSet jwkSet = JWKSet.load(new URL(keysUrl).openStream());
        authServerRunning.set(true);
        return jwkSet;
      } catch (Exception e) {
        authServerRunning.set(false);
        throw new IllegalArgumentException(String.format("Could not retrieve keys from '%s'",
            keysUrl), e);
      }
    }
  }
}
