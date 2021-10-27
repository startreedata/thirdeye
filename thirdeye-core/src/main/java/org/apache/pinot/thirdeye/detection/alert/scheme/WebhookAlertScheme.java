package org.apache.pinot.thirdeye.detection.alert.scheme;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;
import static org.apache.pinot.thirdeye.util.SecurityUtils.hmacSHA512;

import com.codahale.metrics.Counter;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.notification.NotificationSchemeContext;
import org.apache.pinot.thirdeye.notification.commons.WebhookService;
import org.apache.pinot.thirdeye.spi.api.AnomalyReportApi;
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
public class WebhookAlertScheme extends NotificationScheme {

  private static final Logger LOG = LoggerFactory.getLogger(WebhookAlertScheme.class);
  private static final String ANOMALY_DASHBOARD_PREFIX = "anomalies/view/id/";

  private Counter webhookAlertsFailedCounter;
  private Counter webhookAlertsSuccessCounter;

  @Override
  public void init(final NotificationSchemeContext context) {
    super.init(context);

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
    optional(subscriptionGroup.getNotificationSchemes()
        .getWebhookScheme()).ifPresent(w -> buildAndTriggerWebhook(results));
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

  private boolean sendWebhook(final String url, final WebhookApi entity, final String key) {
    final Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(url.substring(0, url.lastIndexOf('/') + 1))
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
    final Call<Void> serviceCall;
    final WebhookService service = retrofit.create(WebhookService.class);
    if (key == null || key.isEmpty()) {
      serviceCall = service.sendWebhook(url, entity);
    } else {
      serviceCall = service.sendWebhook(url, hmacSHA512(entity, key), entity);
    }
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
    return getWebhookApi(anomalyResults, subscriptionGroup);
  }

  public WebhookApi getWebhookApi(final List<MergedAnomalyResultDTO> anomalies,
      SubscriptionGroupDTO subsConfig) {
    WebhookApi api = ApiBeanMapper.toWebhookApi(anomalies, subsConfig);
    List<AnomalyReportApi> results = api.getAnomalyReports();
    return api.setAnomalyReports(results.stream()
        .map(result -> result.setUrl(getDashboardUrl(result.getAnomaly().getId())))
        .collect(
            Collectors.toList()));
  }

  private String getDashboardUrl(final Long id) {
    String extUrl = context.getUiPublicUrl();
    if (!extUrl.matches(".*/")) {
      extUrl += "/";
    }
    return String.format("%s%s%s", extUrl, ANOMALY_DASHBOARD_PREFIX, id);
  }
}
