package org.apache.pinot.thirdeye.notification.commons;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface WebhookService {

  @POST(".")
  Call<Void> sendWebhook(@Body WebhookEntity entity);
}
