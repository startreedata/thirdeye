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
package ai.startree.thirdeye.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OAuthConfiguration {
  private String serverUrl;
  private String keysUrl;
  private String clientId;
  private List<String> required = new ArrayList<>();
  private Map<String, Object> exactMatch = new HashMap<>();
  private OauthCacheConfiguration cache;

  public String getServerUrl() {
    return serverUrl;
  }

  public OAuthConfiguration setServerUrl(final String serverUrl) {
    this.serverUrl = serverUrl;
    return this;
  }

  public String getKeysUrl() {
    return keysUrl;
  }

  public OAuthConfiguration setKeysUrl(final String baseUrl) {
    this.keysUrl = baseUrl;
    return this;
  }

  public String getClientId() {
    return clientId;
  }

  public OAuthConfiguration setClientId(final String clientId) {
    this.clientId = clientId;
    return this;
  }

  public List<String> getRequired() {
    return required;
  }

  public OAuthConfiguration setRequired(final List<String> required) {
    this.required = required;
    return this;
  }

  public Map<String, Object> getExactMatch() {
    return exactMatch;
  }

  public OAuthConfiguration setExactMatch(final Map<String, Object> exactMatch) {
    this.exactMatch = exactMatch;
    return this;
  }

  public OauthCacheConfiguration getCache() {
    return cache;
  }

  public OAuthConfiguration setCache(final OauthCacheConfiguration cache) {
    this.cache = cache;
    return this;
  }
}
