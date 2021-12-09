package org.apache.pinot.thirdeye.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthConfiguration {

  private boolean enabled = true;

  @JsonProperty("oauth")
  private OAuthConfig oAuthConfig;
  private String infoURL;

  public OAuthConfig getOAuthConfig() {
    return oAuthConfig;
  }

  public AuthConfiguration setOAuthConfig(final OAuthConfig oAuthConfig) {
    this.oAuthConfig = oAuthConfig;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public AuthConfiguration setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public String getInfoURL() {
    return infoURL;
  }

  public AuthConfiguration setInfoURL(final String infoURL) {
    this.infoURL = infoURL;
    return this;
  }
}
