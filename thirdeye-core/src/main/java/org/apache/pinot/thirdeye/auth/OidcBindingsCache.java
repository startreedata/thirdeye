package org.apache.pinot.thirdeye.auth;

import com.google.common.cache.CacheLoader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import javax.validation.constraints.NotNull;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;

public class OidcBindingsCache extends CacheLoader<String, ThirdEyePrincipal> {

  private OidcJWTProcessor processor;
  private OidcContext context;

  public OidcBindingsCache setProcessor(final OidcJWTProcessor processor) {
    this.processor = processor;
    return this;
  }

  public OidcBindingsCache setContext(final OidcContext context) {
    this.context = context;
    return this;
  }

  @Override
  public ThirdEyePrincipal load(@NotNull String authToken)
      throws Exception {
    SignedJWT jwt = SignedJWT.parse(authToken);
    JWTClaimsSet claims = processor.process(jwt, context);
    return new ThirdEyePrincipal(claims);
  }
}
