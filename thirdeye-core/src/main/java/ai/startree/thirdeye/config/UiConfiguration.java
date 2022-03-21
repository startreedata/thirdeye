/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.config;

public class UiConfiguration {

  private String externalUrl;
  private String clientId;

  public String getExternalUrl() {
    return externalUrl;
  }

  public UiConfiguration setExternalUrl(final String externalUrl) {
    this.externalUrl = externalUrl;
    return this;
  }

  public String getClientId() {
    return clientId;
  }

  public UiConfiguration setClientId(String clientId) {
    this.clientId = clientId;
    return this;
  }
}
