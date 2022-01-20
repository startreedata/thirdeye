package org.apache.pinot.thirdeye.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthConfiguration {

  public static final String ISSUER_KEY = "issuer";
  public static final String JWKS_KEY = "jwks_uri";
  public static final String OIDC_CONFIG_SUFFIX = ".well-known/openid-configuration";

  private boolean enabled;

  @JsonProperty("oauth")
  private OAuthConfiguration oAuthConfig;

  public OAuthConfiguration getOAuthConfig() {
    return oAuthConfig;
  }

  public AuthConfiguration setOAuthConfig(final OAuthConfiguration oAuthConfiguration) {
    this.oAuthConfig = oAuthConfiguration;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public AuthConfiguration setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }
}
