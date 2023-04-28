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
package ai.startree.thirdeye.plugins.oauth;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.auth.Authenticator;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.Principal;
import java.text.ParseException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeOAuthAuthenticator implements Authenticator<String, Principal> {

  public static final String NAME_CLAIM = "email";
  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeOAuthAuthenticator.class);

  private final OidcJWTProcessor processor;
  private final OidcContext oidcContext;
  private final LoadingCache<String, ThirdEyePrincipal> tokenPrincipalCache;

  @Inject
  public ThirdEyeOAuthAuthenticator(final OidcJWTProcessor processor,
      final OidcContext oidcContext,
      final OAuthConfiguration config) {
    this.processor = processor;
    this.oidcContext = oidcContext;
    final var cacheConfiguration = requireNonNull(config.getCache(), "cache configuration is null");
    this.tokenPrincipalCache = CacheBuilder.newBuilder()
        .maximumSize(cacheConfiguration.getSize())
        .expireAfterWrite(cacheConfiguration.getTtl(), TimeUnit.MILLISECONDS)
        .build(getCacheLoader());
  }

  public static String getName(final JWTClaimsSet claims) {
    try {
      return claims.getStringClaim(NAME_CLAIM);
    } catch (ParseException e) {
      LOG.error("Could not get user name. email should be a String", e);
      return null;
    }
  }

  @Override
  public Optional<Principal> authenticate(final String authToken) {
    try {
      return optional(tokenPrincipalCache.get(authToken));
    } catch (final Exception exception) {
      LOG.error("Authentication failed. msg: {}", exception.getMessage());
      return Optional.empty();
    }
  }

  CacheLoader<String, ThirdEyePrincipal> getCacheLoader() {
    return new CacheLoader<>() {

      @Override
      public ThirdEyePrincipal load(String authToken)
          throws Exception {
        final SignedJWT jwt = SignedJWT.parse(authToken);
        final JWTClaimsSet claims = processor.process(jwt, oidcContext);
        return new ThirdEyePrincipal(getName(claims));
      }
    };
  }
}
