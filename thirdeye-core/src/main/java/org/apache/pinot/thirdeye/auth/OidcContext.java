package org.apache.pinot.thirdeye.auth;

import com.nimbusds.jose.proc.SecurityContext;

public class OidcContext implements SecurityContext {

  private String issuer;
  private String keysUrl;
  private long cacheSize;
  private long cacheTtl;

  public OidcContext(final OAuthConfig config) {
    this.issuer = config.getIssuer();
    this.keysUrl = config.getKeysUrl();
    this.cacheSize = config.getCache().getSize();
    this.cacheTtl = config.getCache().getTtl();
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(final String issuer) {
    this.issuer = issuer;
  }

  public long getCacheSize() {
    return cacheSize;
  }

  public void setCacheSize(final long cacheSize) {
    this.cacheSize = cacheSize;
  }

  public long getCacheTtl() {
    return cacheTtl;
  }

  public void setCacheTtl(final long cacheTtl) {
    this.cacheTtl = cacheTtl;
  }

  public String getKeysUrl() {
    return keysUrl;
  }

  public void setKeysUrl(final String keysUrl) {
    this.keysUrl = keysUrl;
  }
}
