package org.apache.pinot.thirdeye.spi.datalayer.pojo;

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
