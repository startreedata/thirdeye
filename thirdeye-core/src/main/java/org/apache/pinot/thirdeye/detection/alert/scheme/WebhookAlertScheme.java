package org.apache.pinot.thirdeye.detection.alert.scheme;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.util.SecurityUtils.hmacSHA512;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@AlertScheme(type = "WEBHOOK")
@Singleton
public class WebhookAlertScheme extends DetectionAlertScheme {

  private static final Logger LOG = LoggerFactory.getLogger(WebhookAlertScheme.class);
  private final WebhookContentFormatter formatter;
  private final Counter webhookAlertsFailedCounter;
  private final Counter webhookAlertsSuccessCounter;

  @Inject
  public WebhookAlertScheme(
      final WebhookContentFormatter formatter,
      final MetricAnomaliesContent metricAnomaliesContent,
      final EntityGroupKeyContent entityGroupKeyContent,
      final MetricRegistry metricRegistry) {
    super(metricAnomaliesContent, entityGroupKeyContent);
    this.formatter = formatter;
    webhookAlertsFailedCounter = metricRegistry.counter("webhookAlertsFailedCounter");
    webhookAlertsSuccessCounter = metricRegistry.counter("webhookAlertsSuccessCounter");
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

  private void buildAndTriggerWebhook(final DetectionAlertFilterResult results) {
    for (final Map.Entry<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> result : results
        .getResult().entrySet()) {
      final SubscriptionGroupDTO subscriptionGroupDTO = result.getKey().getSubscriptionConfig();
      final WebhookSchemeDto webhook = subscriptionGroupDTO.getNotificationSchemes()
          .getWebhookScheme();
      if (webhook != null) {
        final List<MergedAnomalyResultDTO> anomalyResults = new ArrayList<>(result.getValue());
        anomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));
        final WebhookApi entity = processResults(subscriptionGroupDTO, anomalyResults);
        if (sendWebhook(webhook.getUrl(), entity, webhook.getHashKey())) {
          webhookAlertsSuccessCounter.inc();
        } else {
          webhookAlertsFailedCounter.inc();
        }
      }
    }
  }

  private boolean sendWebhook(String url, final WebhookApi entity, final String key) {
    if (!url.matches(".*/")) {
      url = url.concat("/");
    }
    final Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
    final String signature = hmacSHA512(entity, key);
    final WebhookService service = retrofit.create(WebhookService.class);
    final Call<Void> serviceCall = service.sendWebhook(signature, entity);
    try {
      final Response<Void> response = serviceCall.execute();
      if (response.isSuccessful()) {
        return true;
      }
      LOG.warn("Webhook failed for url {} with code {} : {}",
          url,
          response.code(),
          response.message());
    } catch (IOException e) {
      LOG.error("Webhook failure!");
    }
    return false;
  }

  private WebhookApi processResults(final SubscriptionGroupDTO subscriptionGroup,
      final List<MergedAnomalyResultDTO> anomalyResults) {
    return formatter.getWebhookApi(anomalyResults, subscriptionGroup);
  }
}
