package org.apache.pinot.thirdeye.auth;

import static org.apache.pinot.thirdeye.auth.CacheConfig.DEFAULT_SIZE;
import static org.apache.pinot.thirdeye.auth.CacheConfig.DEFAULT_TTL;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class OidcContext implements SecurityContext {

  private String keysUrl;
  private Set<String> requiredClaims;
  private JWTClaimsSet exactMatchClaimsSet;
  private long cacheSize = DEFAULT_SIZE;
  private long cacheTtl = DEFAULT_TTL;

  public OidcContext(final OAuthConfiguration config) {
    this.keysUrl = config.getKeysUrl();
    this.requiredClaims = new HashSet<>(config.getRequired());
    Builder builder = new JWTClaimsSet.Builder();
    config.getExactMatch().forEach((name, value) -> builder.claim(name, value));
    this.exactMatchClaimsSet = builder.build();
    Optional.ofNullable(config.getCache()).ifPresent(cache -> {
      this.cacheSize = config.getCache().getSize();
      this.cacheTtl = config.getCache().getTtl();
    });
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

  public Set<String> getRequiredClaims() {
    return requiredClaims;
  }

  public OidcContext setRequiredClaims(final Set<String> requiredClaims) {
    this.requiredClaims = requiredClaims;
    return this;
  }

  public JWTClaimsSet getExactMatchClaimsSet() {
    return exactMatchClaimsSet;
  }

  public OidcContext setExactMatchClaimsSet(final JWTClaimsSet exactMatchClaimsSet) {
    this.exactMatchClaimsSet = exactMatchClaimsSet;
    return this;
  }
}
