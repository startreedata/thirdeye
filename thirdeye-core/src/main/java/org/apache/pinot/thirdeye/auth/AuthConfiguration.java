package org.apache.pinot.thirdeye.auth;

public class AuthConfiguration {

  private boolean enabled = true;

  public boolean isEnabled() {
    return enabled;
  }

  public AuthConfiguration setEnabled(final boolean enabled) {
    this.enabled = enabled;
    return this;
  }
}
