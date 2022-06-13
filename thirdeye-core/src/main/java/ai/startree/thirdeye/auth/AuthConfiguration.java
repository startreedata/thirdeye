/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthConfiguration {

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
