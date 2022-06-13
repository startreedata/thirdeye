/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.restclient;

public enum Protocol {
  HTTP("http://"),
  HTTPS("https://");

  private final String val;

  Protocol(String val) {
    this.val = val;
  }

  public String val() {
    return this.val;
  }
}
