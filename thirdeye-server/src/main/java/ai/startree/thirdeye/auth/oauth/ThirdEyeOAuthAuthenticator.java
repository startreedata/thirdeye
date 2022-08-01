/*
 * Copyright 2022 StarTree Inc
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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeOAuthAuthenticator implements Authenticator<String, ThirdEyePrincipal> {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeOAuthAuthenticator.class);
  private final LoadingCache<String, ThirdEyePrincipal> bindingsCache;

  @Inject
  public ThirdEyeOAuthAuthenticator(final OAuthManager oAuthManager) {
    this.bindingsCache = oAuthManager.getDefaultCache();
  }

  @Override
  public Optional<ThirdEyePrincipal> authenticate(final String authToken)
    throws AuthenticationException {
    try {
      return optional(bindingsCache.get(authToken));
    } catch (final Exception exception) {
      LOG.info("Authentication failed. ", exception);
      return Optional.empty();
    }
  }
}
