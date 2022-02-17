/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.commons;

import com.google.common.base.MoreObjects;
import java.util.Objects;

public class SmtpConfiguration {

  private String host;
  private int port = 25;
  private String user;
  private String password;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SmtpConfiguration)) {
      return false;
    }
    SmtpConfiguration at = (SmtpConfiguration) o;
    return Objects.equals(host, at.getHost())
        && Objects.equals(port, at.getPort())
        && Objects.equals(user, at.getUser())
        && Objects.equals(password, at.getPassword());
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, port, user, password);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("host", host)
        .add("port", port)
        .add("user", user)
        .add("password", password)
        .toString();
  }
}
