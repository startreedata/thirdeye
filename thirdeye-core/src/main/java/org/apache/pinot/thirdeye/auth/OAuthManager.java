package org.apache.pinot.thirdeye.auth;

import static org.apache.pinot.thirdeye.auth.AuthConfiguration.ISSUER_KEY;
import static org.apache.pinot.thirdeye.auth.AuthConfiguration.OIDC_CONFIG_SUFFIX;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.apache.pinot.thirdeye.restclient.InfoService;
import org.apache.pinot.thirdeye.spi.api.AuthInfoApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Singleton
public class OAuthManager {

  private static final Logger log = LoggerFactory.getLogger(OAuthManager.class);

  private final OAuthConfiguration oAuthConfig;
  LoadingCache<String, Map<String, Object>> openidConfigCache = CacheBuilder.newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES)
    .maximumSize(1)
    .initialCapacity(1)
    .build(new AuthInfoCacheLoader());

  @Inject
  public OAuthManager(@Nullable final OAuthConfiguration oAuthConfig) {
    this.oAuthConfig = oAuthConfig;
  }

  public AuthInfoApi getInfo() {
    AuthInfoApi authInfo = new AuthInfoApi();
    if(oAuthConfig != null && oAuthConfig.getServerUrl() != null) {
      try {
        Map<String, Object> info = openidConfigCache.get(oAuthConfig.getServerUrl());
        Optional.ofNullable(info.get(ISSUER_KEY)).ifPresent(issuer -> {
          authInfo.setOidcIssuerUrl(issuer.toString());
          authInfo.setOpenidConfiguration(info);
        });
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    return authInfo;
  }

  public static class AuthInfoCacheLoader extends CacheLoader<String, Map<String, Object>> {

    @Override
    public Map<String, Object> load(String url) {
      if(!url.endsWith("/")){
        url+="/";
      }
      final Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
      final InfoService service = retrofit.create(InfoService.class);
      final Call<Map<String, Object>> call = service.getInfo(String.format("%s%s", url, OIDC_CONFIG_SUFFIX));
      try {
        Response<Map<String, Object>> response = call.execute();
        if (response.code() == 200) {
          return response.body();
        }
        log.error("Unable to fetch oidc info! code: {}, message: {}",
          response.code(),
          response.message());
      } catch (IOException e) {
        log.error(String.format("Unable to fetch oidc info! url : %s", url), e);
      }
      return Collections.emptyMap();
    }
  }
}
