package org.apache.pinot.thirdeye.auth;

import static org.apache.pinot.thirdeye.auth.AuthConfiguration.ISSUER_KEY;
import static org.apache.pinot.thirdeye.auth.AuthConfiguration.JWKS_KEY;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.nimbusds.jwt.JWTClaimsSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.spi.api.AuthInfoApi;

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
      Optional.ofNullable(oidcConfig.get(ISSUER_KEY))
        .ifPresent(iss -> oAuthConfig.getExactMatch().put("iss", iss.toString()));
      Optional.ofNullable(oidcConfig.get(JWKS_KEY))
        .ifPresent(jwkUrl -> oAuthConfig.setKeysUrl(jwkUrl.toString()));
    });
  }
}
