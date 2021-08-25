package org.apache.pinot.thirdeye.detection.alert.scheme;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.notification.commons.WebhookService;
import org.apache.pinot.thirdeye.notification.content.templates.EntityGroupKeyContent;
import org.apache.pinot.thirdeye.notification.content.templates.MetricAnomaliesContent;
import org.apache.pinot.thirdeye.notification.formatter.channels.WebhookContentFormatter;
import org.apache.pinot.thirdeye.spi.api.WebhookApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.WebhookSchemeDto;
import org.apache.pinot.thirdeye.spi.detection.annotation.AlertScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@AlertScheme(type = "WEBHOOK")
@Singleton
public class WebhookAlertScheme extends DetectionAlertScheme {

  private static final Logger LOG = LoggerFactory.getLogger(WebhookAlertScheme.class);
  private static final String HMAC_SHA512 = "HmacSHA512";
  private final WebhookContentFormatter formatter;

  @Inject
  public WebhookAlertScheme(
      final WebhookContentFormatter formatter,
      final MetricAnomaliesContent metricAnomaliesContent,
      final EntityGroupKeyContent entityGroupKeyContent) {
    super(metricAnomaliesContent, entityGroupKeyContent);
    this.formatter = formatter;
  }

  @Override
  public void run(final SubscriptionGroupDTO subscriptionGroup,
      final DetectionAlertFilterResult results) throws Exception {
    requireNonNull(results);
    if (results.getAllAnomalies().size() == 0) {
      LOG.debug("Zero anomalies found, skipping webhook alert for {}", subscriptionGroup.getId());
      return;
    }
    buildAndTriggerWebhook(results);
  }

  private void buildAndTriggerWebhook(final DetectionAlertFilterResult results) throws Exception {
    for (final Map.Entry<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> result : results
        .getResult().entrySet()) {
      final SubscriptionGroupDTO subscriptionGroupDTO = result.getKey().getSubscriptionConfig();
      final WebhookSchemeDto webhook = subscriptionGroupDTO.getNotificationSchemes()
          .getWebhookScheme();
      if (webhook != null) {
        final List<MergedAnomalyResultDTO> anomalyResults = new ArrayList<>(result.getValue());
        anomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));
        final WebhookApi entity = processResults(subscriptionGroupDTO, anomalyResults);
        sendWebhook(webhook.getUrl(), entity, webhook.getSecret());
      }
    }
  }

  private boolean sendWebhook(String url, final WebhookApi entity, final String key)
      throws Exception {
    if (!url.matches(".*/")) {
      url = url.concat("/");
    }
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
    String signature = getSignature(entity, key);
    WebhookService service = retrofit.create(WebhookService.class);
    try {
      Response<Void> response = service.sendWebhook(signature, entity).execute();
      if (response.isSuccessful()) {
        LOG.info("Webhook trigger successful to url {}", url);
        return true;
      }
      LOG.warn("Webhook failed for url {} with code {} : {}",
          url,
          response.code(),
          response.message());
      return false;
    } catch (IOException e) {
      LOG.error("Webhook failure!");
      throw e;
    }
  }

  private String getSignature(final WebhookApi entity, final String key)
      throws Exception {
    Mac sha512Hmac;
    final byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
    try {
      sha512Hmac = Mac.getInstance(HMAC_SHA512);
      SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);
      sha512Hmac.init(keySpec);
      byte[] macData = sha512Hmac.doFinal(new ObjectMapper().writeValueAsBytes(entity));
      return Base64.getEncoder().encodeToString(macData);
    } catch (NoSuchAlgorithmException | InvalidKeyException | JsonProcessingException e) {
      LOG.error("Signature generation failure!");
      throw e;
    }
  }

  private WebhookApi processResults(final SubscriptionGroupDTO subscriptionGroup,
      final List<MergedAnomalyResultDTO> anomalyResults) {
    return formatter.getWebhookApi(anomalyResults, subscriptionGroup);
  }
}
