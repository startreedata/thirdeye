/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.notification.commons;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Set;

public class SmtpConfiguration {

  private String smtpHost;
  private int smtpPort = 25;
  private String smtpUser;
  private String smtpPassword;
  private Set<String> adminRecipients;
  private Set<String> emailWhitelist;

  public String getSmtpHost() {
    return smtpHost;
  }

  public void setSmtpHost(String smtpHost) {
    this.smtpHost = smtpHost;
  }

  public int getSmtpPort() {
    return smtpPort;
  }

  public void setSmtpPort(int smtpPort) {
    this.smtpPort = smtpPort;
  }

  public String getSmtpUser() {
    return smtpUser;
  }

  public void setSmtpUser(String smtpUser) {
    this.smtpUser = smtpUser;
  }

  public String getSmtpPassword() {
    return smtpPassword;
  }

  public void setSmtpPassword(String smtpPassword) {
    this.smtpPassword = smtpPassword;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SmtpConfiguration)) {
      return false;
    }
    SmtpConfiguration at = (SmtpConfiguration) o;
    return Objects.equals(smtpHost, at.getSmtpHost())
        && Objects.equals(smtpPort, at.getSmtpPort())
        && Objects.equals(smtpUser, at.getSmtpUser())
        && Objects.equals(smtpPassword, at.getSmtpPassword());
  }

  @Override
  public int hashCode() {
    return Objects.hash(smtpHost, smtpPort, smtpUser, smtpPassword);
  }

  public Set<String> getAdminRecipients() {
    return adminRecipients;
  }

  public SmtpConfiguration setAdminRecipients(final Set<String> adminRecipients) {
    this.adminRecipients = adminRecipients;
    return this;
  }

  public Set<String> getEmailWhitelist() {
    return emailWhitelist;
  }

  public SmtpConfiguration setEmailWhitelist(final Set<String> emailWhitelist) {
    this.emailWhitelist = emailWhitelist;
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("smtpHost", smtpHost)
        .add("smtpPort", smtpPort)
        .add("smtpUser", smtpUser)
        .add("adminRecipients", adminRecipients)
        .add("emailWhitelist", emailWhitelist)
        .toString();
  }
}
