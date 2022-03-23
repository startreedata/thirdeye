/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.auth;

import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.AuthInfoApi;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.nimbusds.jwt.JWTClaimsSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class OidcUtils {

  public static JWTClaimsSet getExactMatchClaimSet(OidcContext context) {
    return context.getExactMatchClaimsSet();
  }

  public static Set<String> getRequiredClaims(OidcContext context) {
    return context.getRequiredClaims();
  }

  public static LoadingCache<String, ThirdEyePrincipal> makeDefaultCache(OidcContext context) {
    return CacheBuilder.newBuilder()
      .maximumSize(context.getCacheSize())
      .expireAfterWrite(context.getCacheTtl(), TimeUnit.MILLISECONDS)
      .build(new OidcBindingsCache()
        .setProcessor(new OidcJWTProcessor(context))
        .setContext(context));
  }

  public static void generateOAuthConfig(OAuthManager oAuthManager, OAuthConfiguration oAuthConfig) {
    AuthInfoApi info = oAuthManager.getInfo();
    Optional.ofNullable(info.getOpenidConfiguration()).ifPresent(oidcConfig -> {
      Optional.ofNullable(oidcConfig.get(AuthConfiguration.ISSUER_KEY))
        .ifPresent(iss -> oAuthConfig.getExactMatch().put("iss", iss.toString()));
      Optional.ofNullable(oidcConfig.get(AuthConfiguration.JWKS_KEY))
        .ifPresent(jwkUrl -> oAuthConfig.setKeysUrl(jwkUrl.toString()));
    });
  }
}
