package org.apache.pinot.thirdeye.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthConfiguration {

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
}
