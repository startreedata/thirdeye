package org.apache.pinot.thirdeye.auth;

public class OAuthConfig {
  private String keysUrl;
  private String clientId;
  private String issuer;
  private String audience;
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

  public String getIssuer() {
    return issuer;
  }

  public OAuthConfig setIssuer(final String issuer) {
    this.issuer = issuer;
    return this;
  }

  public String getAudience() {
    return audience;
  }

  public OAuthConfig setAudience(final String audience) {
    this.audience = audience;
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
