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

import ai.startree.thirdeye.spi.auth.Authenticator;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import java.security.Principal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeOAuthAuthenticator implements Authenticator<String, Principal> {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeOAuthAuthenticator.class);

  private final OidcJWTProcessor processor;
  private final OidcContext oidcContext;
  private final OAuthConfiguration config;
  private final LoadingCache<String, ThirdEyePrincipal> bindingsCache;

  @Inject
  public ThirdEyeOAuthAuthenticator(final OidcJWTProcessor processor,
      final OidcContext oidcContext,
      final OAuthConfiguration config) {
    this.processor = processor;
    this.oidcContext = oidcContext;
    this.config = config;
    this.bindingsCache = getDefaultCache();
  }

  @Override
  public Optional<Principal> authenticate(final String authToken) {
    try {
      return optional(bindingsCache.get(authToken));
    } catch (final Exception exception) {
      LOG.error("Authentication failed. msg: {}", exception.getMessage());
      return Optional.empty();
    }
  }

  public LoadingCache<String, ThirdEyePrincipal> getDefaultCache() {
    return CacheBuilder.newBuilder()
        .maximumSize(config.getCache().getSize())
        .expireAfterWrite(config.getCache().getTtl(), TimeUnit.MILLISECONDS)
        .build(new OidcBindingsCache()
            .setProcessor(processor)
            .setContext(oidcContext));
  }
}
