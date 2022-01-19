package org.apache.pinot.thirdeye.auth;

import static org.apache.pinot.thirdeye.auth.OidcUtils.generateOAuthConfig;
import static org.apache.pinot.thirdeye.auth.OidcUtils.makeDefaultCache;

import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import java.util.Optional;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyeAuthenticator implements Authenticator<String, ThirdEyePrincipal> {

  private static final Logger LOG = LoggerFactory.getLogger(ThirdEyeAuthenticator.class);
  private LoadingCache<String, ThirdEyePrincipal> bindingsCache;

  @Inject
  public ThirdEyeAuthenticator(final OAuthConfiguration oAuthConfig,
    final OAuthManager oAuthManager) {
    generateOAuthConfig(oAuthManager, oAuthConfig);
    this.bindingsCache = makeDefaultCache(new OidcContext(oAuthConfig));
  }

  @Override
  public Optional<ThirdEyePrincipal> authenticate(final String authToken)
    throws AuthenticationException {
    try {
      return Optional.ofNullable(bindingsCache.get(authToken));
    } catch (Exception exception) {
      LOG.info("Authentication failed. ", exception);
      return Optional.empty();
    }
  }
}
