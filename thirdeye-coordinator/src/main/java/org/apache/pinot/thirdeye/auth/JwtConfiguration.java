package org.apache.pinot.thirdeye.auth;

import javax.validation.constraints.NotNull;
import org.joda.time.Duration;

public class JwtConfiguration {

  @NotNull
  private String signingKey;

  @NotNull
  private String issuer;

  private Duration accessTokenExpiry = Duration.standardDays(1);

  public String getSigningKey() {
    return signingKey;
  }

  public JwtConfiguration setSigningKey(final String signingKey) {
    this.signingKey = signingKey;
    return this;
  }

  public String getIssuer() {
    return issuer;
  }

  public JwtConfiguration setIssuer(final String issuer) {
    this.issuer = issuer;
    return this;
  }

  public Duration getAccessTokenExpiry() {
    return accessTokenExpiry;
  }

  public JwtConfiguration setAccessTokenExpiry(final Duration accessTokenExpiry) {
    this.accessTokenExpiry = accessTokenExpiry;
    return this;
  }
}
