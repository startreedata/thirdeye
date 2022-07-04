/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.plugins.notification.email;

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
