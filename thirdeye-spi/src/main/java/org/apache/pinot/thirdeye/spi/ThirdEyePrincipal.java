package org.apache.pinot.thirdeye.spi;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.security.Principal;

public class ThirdEyePrincipal implements Principal {

  private static final String SUBJECT = "sub";
  private final DecodedJWT accessToken;

  public ThirdEyePrincipal(final DecodedJWT accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public String getName() {
    return accessToken.getClaim(SUBJECT).asString();
  }
}
