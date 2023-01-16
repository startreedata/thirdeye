/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.datalayer.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatabaseConfiguration {

  private String user;
  private String password;
  private String url;
  private String driver;
  private Map<String, String> properties = Maps.newLinkedHashMap();

  public String getUser() {
    return user;
  }

  public DatabaseConfiguration setUser(final String user) {
    this.user = user;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public DatabaseConfiguration setPassword(final String password) {
    this.password = password;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public DatabaseConfiguration setUrl(final String url) {
    this.url = url;
    return this;
  }

  public String getDriver() {
    return driver;
  }

  public DatabaseConfiguration setDriver(final String driver) {
    this.driver = driver;
    return this;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public DatabaseConfiguration setProperties(
      final Map<String, String> properties) {
    this.properties = properties;
    return this;
  }
}
