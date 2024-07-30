/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.plugins.oauth;

import static ai.startree.thirdeye.spi.Constants.OAUTH_ISSUER;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.api.AuthInfoApi;
import ai.startree.thirdeye.spi.auth.OpenIdConfigurationProvider;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Singleton
public class OpenIdConfigurationProviderImpl implements OpenIdConfigurationProvider {

  public static final String OIDC_CONFIG_SUFFIX = ".well-known/openid-configuration";
  private static final Logger log = LoggerFactory.getLogger(OpenIdConfigurationProviderImpl.class);

  private final OAuthConfiguration oAuthConfig;
  private final LoadingCache<String, Map<String, Object>> openidConfigCache;

  @Inject
  public OpenIdConfigurationProviderImpl(@Nullable OAuthConfiguration oAuthConfig) {
    this.oAuthConfig = oAuthConfig;
    openidConfigCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .maximumSize(1)
        .initialCapacity(1)
        .build(new CacheLoader<>() {
          @Override
          public Map<String, Object> load(String key) {
            return loadOpenIdConfiguration(key);
          }
        });
  }

  private static Map<String, Object> loadOpenIdConfiguration(String url) {
    if (!url.endsWith("/")) {
      url += "/";
    }
    final Retrofit retrofit = new Builder()
        .baseUrl(url)
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
    final AuthInfoService service = retrofit.create(AuthInfoService.class);
    final Call<Map<String, Object>> call = service.getInfo(String.format("%s%s",
        url,
        OIDC_CONFIG_SUFFIX));
    try {
      final Response<Map<String, Object>> response = call.execute();
      if (response.isSuccessful()) {
        return response.body();
      }
      log.error("Unable to fetch oidc info! code: {}, message: {}",
          response.code(),
          response.message());
    } catch (final IOException e) {
      log.error("Unable to fetch oidc info! url: " + url, e);
    }
    return Collections.emptyMap();
  }

  @Override
  public AuthInfoApi getOpenIdConfiguration() {
    final AuthInfoApi authInfo = new AuthInfoApi();
    if (oAuthConfig == null || oAuthConfig.getServerUrl() == null) {
      return authInfo;
    }
    try {
      final Map<String, Object> info = openidConfigCache.get(oAuthConfig.getServerUrl());
      optional(info.get(OAUTH_ISSUER))
          .ifPresent(issuer -> authInfo
              .setOidcIssuerUrl(issuer.toString())
              .setOpenidConfiguration(info));
    } catch (final ExecutionException | NullPointerException e) {
      e.printStackTrace();
      return null;
    }
    return authInfo;
  }
}
