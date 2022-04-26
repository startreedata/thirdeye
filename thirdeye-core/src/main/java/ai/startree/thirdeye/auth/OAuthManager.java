/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.auth;

import ai.startree.thirdeye.restclient.InfoService;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.AuthInfoApi;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Singleton
public class OAuthManager implements AuthManager {

  private static final Logger log = LoggerFactory.getLogger(OAuthManager.class);
  public static final String ISSUER_KEY = "issuer";
  public static final String JWKS_KEY = "jwks_uri";
  public static final String OIDC_CONFIG_SUFFIX = ".well-known/openid-configuration";

  private final OidcJWTProcessor processor;
  private final OAuthConfiguration oAuthConfig;
  private final OidcContext context;
  LoadingCache<String, Map<String, Object>> openidConfigCache = CacheBuilder.newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES)
    .maximumSize(1)
    .initialCapacity(1)
    .build(new AuthInfoCacheLoader());

  @Inject
  public OAuthManager(@Nullable final OAuthConfiguration oAuthConfig,
      final OidcJWTProcessor processor) {
    this.oAuthConfig = oAuthConfig;
    this.processor = processor;
    this.context = generateOidcContext(oAuthConfig);
  }

  @Override
  public LoadingCache<String, ThirdEyePrincipal> getDefaultCache() {
    processor.init(context);
    return CacheBuilder.newBuilder()
        .maximumSize(context.getCacheSize())
        .expireAfterWrite(context.getCacheTtl(), TimeUnit.MILLISECONDS)
        .build(new OidcBindingsCache()
            .setProcessor(processor)
            .setContext(context));
  }

  public AuthInfoApi getInfo() {
    AuthInfoApi authInfo = new AuthInfoApi();
    if (oAuthConfig != null && oAuthConfig.getServerUrl() != null) {
      try {
        Map<String, Object> info = openidConfigCache.get(oAuthConfig.getServerUrl());
        Optional.of(info.get(ISSUER_KEY)).ifPresent(issuer -> {
          authInfo.setOidcIssuerUrl(issuer.toString());
          authInfo.setOpenidConfiguration(info);
        });
      } catch (ExecutionException | NullPointerException e) {
        e.printStackTrace();
        return null;
      }
    }
    return authInfo;
  }

  private OidcContext generateOidcContext(final OAuthConfiguration config) {
    AuthInfoApi info = getInfo();
    Optional.ofNullable(info.getOpenidConfiguration()).ifPresent(oidcConfig -> {
      Optional.ofNullable(oidcConfig.get(ISSUER_KEY))
          .ifPresent(iss -> config.getExactMatch().put("iss", iss.toString()));
      Optional.ofNullable(oidcConfig.get(JWKS_KEY))
          .ifPresent(jwkUrl -> config.setKeysUrl(jwkUrl.toString()));
    });
    return new OidcContext(config);
  }

  public static class AuthInfoCacheLoader extends CacheLoader<String, Map<String, Object>> {

    @Override
    public Map<String, Object> load(String url) {
      if (!url.endsWith("/")) {
        url += "/";
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
