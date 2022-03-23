/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.restclient;

import java.util.Optional;

public class ThirdEyeRestClientConfiguration {

  private static final String DEFAULT_THIRDEYE_SERVICE_NAME = "thirdeye";
  private static final String DEFAULT_THIRDEYE_SERVICE_KEY = "thirdeyeServiceKey";

  private String adminUser;
  private String sessionKey;

  public String getAdminUser() {
    return Optional.ofNullable(adminUser).orElse(DEFAULT_THIRDEYE_SERVICE_NAME);
  }

  public void setAdminUser(String adminUser) {
    this.adminUser = adminUser;
  }

  public String getSessionKey() {
    return Optional.ofNullable(sessionKey).orElse(DEFAULT_THIRDEYE_SERVICE_KEY);
  }

  public void setSessionKey(String sessionKey) {
    this.sessionKey = sessionKey;
  }
}
