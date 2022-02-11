/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

public class UserBean {

  private String principal;

  public String getPrincipal() {
    return principal;
  }

  public UserBean setPrincipal(final String principal) {
    this.principal = principal;
    return this;
  }
}
