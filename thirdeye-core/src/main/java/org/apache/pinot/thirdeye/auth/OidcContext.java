package org.apache.pinot.thirdeye.auth;

import static org.apache.pinot.thirdeye.auth.CacheConfig.DEFAULT_SIZE;
import static org.apache.pinot.thirdeye.auth.CacheConfig.DEFAULT_TTL;

import com.nimbusds.jose.proc.SecurityContext;
import java.util.Optional;

public class OidcContext implements SecurityContext {

  private String issuer;
  private String keysUrl;
  private long cacheSize = DEFAULT_SIZE;
  private long cacheTtl = DEFAULT_TTL;

  public OidcContext(final OAuthConfig config) {
    this.issuer = config.getIssuer();
    this.keysUrl = config.getKeysUrl();
    Optional.ofNullable(config.getCache()).ifPresent(cache -> {
      this.cacheSize = config.getCache().getSize();
      this.cacheTtl = config.getCache().getTtl();
    });
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
