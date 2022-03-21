/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AuthApi implements ThirdEyeApi {

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
