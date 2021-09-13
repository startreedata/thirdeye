package org.apache.pinot.thirdeye.notification.commons;

import org.apache.pinot.thirdeye.spi.api.WebhookApi;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface WebhookService {

  @POST(".")
  Call<Void> sendWebhook(@Header("Signature") String signature, @Body WebhookApi entity);
}
