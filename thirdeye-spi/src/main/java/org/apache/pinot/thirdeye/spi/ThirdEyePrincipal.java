package org.apache.pinot.thirdeye.spi;

import com.nimbusds.jwt.JWTClaimsSet;
import java.security.Principal;

public class ThirdEyePrincipal implements Principal {

  private JWTClaimsSet claims;

  public ThirdEyePrincipal() {
  }

  public ThirdEyePrincipal(final JWTClaimsSet claims) {
    this.claims = claims;
  }

  @Override
  public String getName() {
    return claims.getSubject();
  }

  public JWTClaimsSet getClaims() {
    return claims;
  }

  public ThirdEyePrincipal setClaims(final JWTClaimsSet claims) {
    this.claims = claims;
    return this;
  }
}
