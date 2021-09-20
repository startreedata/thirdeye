package org.apache.pinot.thirdeye.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthConfiguration {

  private boolean enabled = true;

  @JsonProperty("jwt")
  private JwtConfiguration jwtConfiguration;

  public JwtConfiguration getJwtConfiguration() {
    return jwtConfiguration;
  }

  public AuthConfiguration setJwtConfiguration(
      final JwtConfiguration jwtConfiguration) {
    this.jwtConfiguration = jwtConfiguration;
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
