/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification.webhook;

import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface WebhookService {

  @POST
  Call<Void> sendWebhook(@Url String url, @Header("X-Signature") String signature, @Body NotificationPayloadApi entity);

  @POST
  Call<Void> sendWebhook(@Url String url, @Body NotificationPayloadApi entity);
}
