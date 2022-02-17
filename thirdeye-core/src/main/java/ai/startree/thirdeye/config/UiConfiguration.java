/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.config;

public class UiConfiguration {

  private String externalUrl;

  public String getExternalUrl() {
    return externalUrl;
  }

  public UiConfiguration setExternalUrl(final String externalUrl) {
    this.externalUrl = externalUrl;
    return this;
  }
}
