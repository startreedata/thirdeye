package org.apache.pinot.thirdeye.api;

public class AuthApi {

  private UserApi user;
  private String accessToken;

  public UserApi getUser() {
    return user;
  }

  public AuthApi setUser(final UserApi user) {
    this.user = user;
    return this;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public AuthApi setAccessToken(final String accessToken) {
    this.accessToken = accessToken;
    return this;
  }
}
