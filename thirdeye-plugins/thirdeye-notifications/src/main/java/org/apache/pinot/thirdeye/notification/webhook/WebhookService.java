package org.apache.pinot.thirdeye.notification.webhook;

import org.apache.pinot.thirdeye.spi.api.NotificationPayloadApi;
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
