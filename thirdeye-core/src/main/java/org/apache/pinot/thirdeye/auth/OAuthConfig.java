package org.apache.pinot.thirdeye.auth;

public class OAuthConfig {
  private String baseUrl;
  private String clientId;
  private String issuer;
  private String audience;

  public String getBaseUrl() {
    return baseUrl;
  }

  public OAuthConfig setBaseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
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
}
