package org.apache.pinot.thirdeye.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OAuthConfig {
  private String keysUrl;
  private String clientId;
  private List<String> required = new ArrayList<>();
  private Map<String, Object> exactMatch = new HashMap<>();
  private CacheConfig cache;

  public String getKeysUrl() {
    return keysUrl;
  }

  public OAuthConfig setKeysUrl(final String baseUrl) {
    this.keysUrl = baseUrl;
    return this;
  }

  public String getClientId() {
    return clientId;
  }

  public OAuthConfig setClientId(final String clientId) {
    this.clientId = clientId;
    return this;
  }

  public List<String> getRequired() {
    return required;
  }

  public OAuthConfig setRequired(final List<String> required) {
    this.required = required;
    return this;
  }

  public Map<String, Object> getExactMatch() {
    return exactMatch;
  }

  public OAuthConfig setExactMatch(final Map<String, Object> exactMatch) {
    this.exactMatch = exactMatch;
    return this;
  }

  public CacheConfig getCache() {
    return cache;
  }

  public OAuthConfig setCache(final CacheConfig cache) {
    this.cache = cache;
    return this;
  }
}
