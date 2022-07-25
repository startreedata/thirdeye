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

import static ai.startree.thirdeye.auth.OauthCacheConfiguration.DEFAULT_SIZE;
import static ai.startree.thirdeye.auth.OauthCacheConfiguration.DEFAULT_TTL;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import java.util.HashSet;
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
    final Builder builder = new JWTClaimsSet.Builder();
    config.getExactMatch().forEach(builder::claim);

    this.exactMatchClaimsSet = builder.build();

    optional(config.getCache()).ifPresent(cache -> {
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
