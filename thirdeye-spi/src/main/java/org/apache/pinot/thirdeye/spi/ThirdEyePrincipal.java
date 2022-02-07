package org.apache.pinot.thirdeye.spi;

import com.nimbusds.jwt.JWTClaimsSet;
import java.security.Principal;
import java.text.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdEyePrincipal implements Principal {

  private static final Logger log = LoggerFactory.getLogger(ThirdEyePrincipal.class);

  private JWTClaimsSet claims;

  public ThirdEyePrincipal() {
  }

  public ThirdEyePrincipal(final JWTClaimsSet claims) {
    this.claims = claims;
  }

  @Override
  public String getName() {
    try {
      return claims.getStringClaim("email");
    } catch (ParseException e) {
      log.error("Email should be a String!");
      return null;
    }
  }

  public JWTClaimsSet getClaims() {
    return claims;
  }

  public ThirdEyePrincipal setClaims(final JWTClaimsSet claims) {
    this.claims = claims;
    return this;
  }
}
