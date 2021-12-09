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

  public static HashMap<String, Object> getAuthInfo(final String infoURL){
    final Retrofit retrofit = new Retrofit.Builder()
      .baseUrl(infoURL.substring(0, infoURL.lastIndexOf('/') + 1))
      .addConverterFactory(JacksonConverterFactory.create())
      .build();
    final InfoService service = retrofit.create(InfoService.class);
    final Call<HashMap<String, Object>> call = service.getInfo(infoURL);
    try {
      return call.execute().body();
    } catch (IOException e) {
      return null;
    }
  }
}
