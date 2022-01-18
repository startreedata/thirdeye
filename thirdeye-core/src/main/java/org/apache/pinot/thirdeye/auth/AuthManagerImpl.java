package org.apache.pinot.thirdeye.auth;

import static org.apache.pinot.thirdeye.auth.AuthConfiguration.ISSUER_KEY;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.restclient.InfoService;
import org.apache.pinot.thirdeye.spi.api.AuthInfoApi;
import org.apache.pinot.thirdeye.spi.auth.AuthManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Singleton
public class AuthManagerImpl implements AuthManager {

  private static final Logger log = LoggerFactory.getLogger(AuthManagerImpl.class);

  private final AuthConfiguration authConfig;
  LoadingCache<String, Map<String, Object>> openidConfigCache = CacheBuilder.newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(1).initialCapacity(1).build(new AuthInfoCacheLoader());

  public AuthManagerImpl(AuthConfiguration authConfig){
    this.authConfig = authConfig;
  }

  @Override
  public AuthInfoApi getInfo() {
    AuthInfoApi authInfo = new AuthInfoApi();
    Optional.ofNullable(authConfig.getInfoURL()).ifPresent(url -> {
      try {
        Map<String, Object> info = openidConfigCache.get(url);
        Optional.ofNullable(info.get(ISSUER_KEY)).ifPresent(issuer -> {
          authInfo.setOidcIssuerUrl(issuer.toString());
          authInfo.setOpenidConfiguration(info);
        });
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    });
    return authInfo;
  }

  public static class AuthInfoCacheLoader extends CacheLoader<String, Map<String, Object>> {
    @Override
    public Map<String, Object> load(String url) {
      final Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(url.substring(0, url.lastIndexOf('/') + 1))
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
      final InfoService service = retrofit.create(InfoService.class);
      final Call<Map<String, Object>> call = service.getInfo(url);
      try {
        Response<Map<String, Object>> response = call.execute();
        if(response.code() == 200){
          return response.body();
        }
        log.error("Unable to fetch oidc info! code: {}, message: {}", response.code(), response.message());
      } catch (IOException e) {
        log.error(String.format("Unable to fetch oidc info! url : %s", url), e);
      }
      return Collections.emptyMap();
    }
  }
}
