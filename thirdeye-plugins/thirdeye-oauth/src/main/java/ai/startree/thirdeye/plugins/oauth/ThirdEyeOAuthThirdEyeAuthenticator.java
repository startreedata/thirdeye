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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.spi.auth.ThirdEyeAuthenticator;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthenticator.AuthTokenAndNamespace;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeOAuthThirdEyeAuthenticator implements ThirdEyeAuthenticator<AuthTokenAndNamespace> {

  public static final String NAME_CLAIM = "email";
  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeOAuthThirdEyeAuthenticator.class);

  private final OidcJWTProcessor processor;
  private final OidcContext oidcContext;
  private final LoadingCache<AuthTokenAndNamespace, Optional<ThirdEyePrincipal>> tokenPrincipalCache;

  @Inject
  public ThirdEyeOAuthThirdEyeAuthenticator(final OidcJWTProcessor processor,
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

  private static String getName(final JWTClaimsSet claims) {
    try {
      return claims.getStringClaim(NAME_CLAIM);
    } catch (ParseException e) {
      LOG.error("Could not get user name. email should be a String", e);
      return null;
    }
  }

  @Override
  public Optional<ThirdEyePrincipal> authenticate(final AuthTokenAndNamespace authTokenAndNamespace) {
    try {
      return tokenPrincipalCache.get(authTokenAndNamespace);
    } catch (final Exception exception) {
      LOG.error("Authentication failed.", exception);
      return Optional.empty();
    }
  }

  @VisibleForTesting
  protected CacheLoader<AuthTokenAndNamespace, Optional<ThirdEyePrincipal>> getCacheLoader() {
    return new CacheLoader<>() {

      @Override
      public Optional<ThirdEyePrincipal> load(final AuthTokenAndNamespace authTokenAndNamespace)
          throws Exception {
        if (authTokenAndNamespace.authToken() == null) {
          return Optional.empty();
        }
        final SignedJWT jwt = SignedJWT.parse(authTokenAndNamespace.authToken());
        final JWTClaimsSet claims = processor.process(jwt, oidcContext);
        return optional(new OAuthThirdEyePrincipal(getName(claims), authTokenAndNamespace.namespace()));
      }
    };
  }
}
