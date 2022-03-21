/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.email;

public class SmtpConfiguration {

  private String host;
  private Integer port = 25;
  private String user;
  private String password;

  public String getHost() {
    return host;
  }

  public SmtpConfiguration setHost(final String host) {
    this.host = host;
    return this;
  }

  public Integer getPort() {
    return port;
  }

  public SmtpConfiguration setPort(final Integer port) {
    this.port = port;
    return this;
  }

  public String getUser() {
    return user;
  }

  public SmtpConfiguration setUser(final String user) {
    this.user = user;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public SmtpConfiguration setPassword(final String password) {
    this.password = password;
    return this;
  }
}
