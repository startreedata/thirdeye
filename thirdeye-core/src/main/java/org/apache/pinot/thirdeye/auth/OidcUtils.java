package org.apache.pinot.thirdeye.auth;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.restclient.InfoService;
import org.apache.pinot.thirdeye.spi.ThirdEyePrincipal;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

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
            .setProcessor(new OidcJWTProcessor(fetchKeys(context.getKeysUrl()).getKeys(), context))
            .setContext(context));
  }

  public static JWKSet fetchKeys(String keysUrl) {
    try {
      return JWKSet.load(new URL(keysUrl).openStream());
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format("Could not retrieve keys from '%s'",
          keysUrl), e);
    }
  }
}
