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

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.restclient.InfoService;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.AuthInfoApi;
import com.codahale.metrics.MetricRegistry;
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
public class OAuthManager {

  private static final Logger log = LoggerFactory.getLogger(OAuthManager.class);
  public static final String ISSUER_KEY = "issuer";
  public static final String JWKS_KEY = "jwks_uri";
  public static final String OIDC_CONFIG_SUFFIX = ".well-known/openid-configuration";

  private final OidcJWTProcessor processor;
  private final OAuthConfiguration oAuthConfig;
  private final OidcContext context;
  private final MetricRegistry metricRegistry;
  LoadingCache<String, Map<String, Object>> openidConfigCache = CacheBuilder.newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES)
    .maximumSize(1)
    .initialCapacity(1)
    .build(new AuthInfoCacheLoader());

  @Inject
  public OAuthManager(@Nullable final OAuthConfiguration oAuthConfig,
      final OidcJWTProcessor processor,
      final MetricRegistry metricRegistry) {
    this.oAuthConfig = oAuthConfig;
    this.processor = processor;
    this.metricRegistry = metricRegistry;
    this.context = oAuthConfig != null ? generateOidcContext(oAuthConfig) : null;
  }

  public LoadingCache<String, ThirdEyePrincipal> getDefaultCache() {
    processor.init(context, metricRegistry);
    return CacheBuilder.newBuilder()
        .maximumSize(context.getCacheSize())
        .expireAfterWrite(context.getCacheTtl(), TimeUnit.MILLISECONDS)
        .build(new OidcBindingsCache()
            .setProcessor(processor)
            .setContext(context));
  }

  public AuthInfoApi getInfo() {
    final AuthInfoApi authInfo = new AuthInfoApi();
    if (oAuthConfig != null && oAuthConfig.getServerUrl() != null) {
      try {
        final Map<String, Object> info = openidConfigCache.get(oAuthConfig.getServerUrl());
        Optional.of(info.get(ISSUER_KEY)).ifPresent(issuer -> {
          authInfo.setOidcIssuerUrl(issuer.toString());
          authInfo.setOpenidConfiguration(info);
        });
      } catch (final ExecutionException | NullPointerException e) {
        e.printStackTrace();
        return null;
      }
    }
    return authInfo;
  }

  private OidcContext generateOidcContext(final OAuthConfiguration config) {
    final AuthInfoApi info = getInfo();
    optional(info.getOpenidConfiguration()).ifPresent(oidcConfig -> {
      optional(oidcConfig.get(ISSUER_KEY))
          .ifPresent(iss -> config.getExactMatch().put("iss", iss.toString()));
      optional(oidcConfig.get(JWKS_KEY))
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
        final Response<Map<String, Object>> response = call.execute();
        if (response.code() == 200) {
          return response.body();
        }
        log.error("Unable to fetch oidc info! code: {}, message: {}",
          response.code(),
          response.message());
      } catch (final IOException e) {
        log.error(String.format("Unable to fetch oidc info! url : %s", url), e);
      }
      return Collections.emptyMap();
    }
  }
}
