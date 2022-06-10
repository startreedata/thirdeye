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
package ai.startree.thirdeye.plugins.notification.webhook;

import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import ai.startree.thirdeye.spi.notification.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class WebhookNotificationService implements NotificationService {

  private static final Logger LOG = LoggerFactory.getLogger(WebhookNotificationService.class);
  private static final String HMAC_SHA512 = "HmacSHA512";
  private static final String AUTH_TYPE = "Thirdeye-HMAC-SHA512";

  private final String url;
  private final String hashKey;
  private final WebhookService service;

  public WebhookNotificationService(final WebhookConfiguration webhookConfiguration) {
    url = webhookConfiguration.getUrl();
    hashKey = webhookConfiguration.getHashKey();

    final Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(url.substring(0, url.lastIndexOf('/') + 1))
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
    service = retrofit.create(WebhookService.class);
  }

  public static String hmacSHA512(final Object entity, final String key) {
    final Mac sha512Hmac;
    final byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
    try {
      sha512Hmac = Mac.getInstance(HMAC_SHA512);
      final SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);
      sha512Hmac.init(keySpec);
      final byte[] macData = sha512Hmac.doFinal(new ObjectMapper().writeValueAsBytes(entity));
      return String.format("%s %s", AUTH_TYPE, Base64.getEncoder().encodeToString(macData));
    } catch (final NoSuchAlgorithmException | InvalidKeyException | JsonProcessingException e) {
      LOG.error("Signature generation failure!", e);
      return null;
    }
  }

  @Override
  public void notify(final NotificationPayloadApi api) {
    final Call<Void> serviceCall;
    if (hashKey == null || hashKey.isEmpty()) {
      serviceCall = service.sendWebhook(url, api);
    } else {
      serviceCall = service.sendWebhook(url, hmacSHA512(api, hashKey), api);
    }
    try {
      final Response<Void> response = serviceCall.execute();
      if (response.isSuccessful()) {
        return;
      }
      LOG.warn("Webhook failed for url {} with code {} : {}",
          url,
          response.code(),
          response.message());
    } catch (final IOException e) {
      LOG.error("Webhook failure!");
    }
  }
}
