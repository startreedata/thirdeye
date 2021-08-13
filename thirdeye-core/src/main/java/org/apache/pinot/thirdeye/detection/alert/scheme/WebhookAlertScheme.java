package org.apache.pinot.thirdeye.detection.alert.scheme;

import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.notification.commons.WebhookService;
import org.apache.pinot.thirdeye.notification.commons.WebhookEntity;
import org.apache.pinot.thirdeye.notification.content.BaseNotificationContent;
import org.apache.pinot.thirdeye.notification.content.templates.EntityGroupKeyContent;
import org.apache.pinot.thirdeye.notification.content.templates.MetricAnomaliesContent;
import org.apache.pinot.thirdeye.notification.formatter.channels.WebhookContentFormatter;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResult;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;
import org.apache.pinot.thirdeye.spi.detection.annotation.AlertScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@AlertScheme(type = "WEBHOOK")
@Singleton
public class WebhookAlertScheme extends DetectionAlertScheme {
  public static final String PROP_WEBHOOK_SCHEME = "webhookScheme";
  private static final Logger LOG = LoggerFactory.getLogger(WebhookAlertScheme.class);
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

  private void buildAndTriggerWebhook(final DetectionAlertFilterResult results){
    for (final Map.Entry<DetectionAlertFilterNotification, Set<MergedAnomalyResultDTO>> result : results
        .getResult().entrySet()) {
      final SubscriptionGroupDTO subscriptionGroupDTO = result.getKey().getSubscriptionConfig();
      final Map<String, Object> webhook = (Map<String, Object>) subscriptionGroupDTO.getAlertSchemes().get(PROP_WEBHOOK_SCHEME);
      if (webhook == null) {
        throw new IllegalArgumentException(
            "Invalid webhook settings in subscription group " + subscriptionGroupDTO.getId());
      }

      final List<AnomalyResult> anomalyResults = new ArrayList<>(result.getValue());
      anomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));
      final WebhookEntity entity = processResults(subscriptionGroupDTO, anomalyResults);
      if(sendWebhook(webhook.get("url").toString(), entity)){
        LOG.info("Webhook trigger successful to url {}", webhook.get("url"));
      } else {
        LOG.error("Webhook trigger failed to url {}", webhook.get("url"));
      }
    }
  }

  private boolean sendWebhook(String url, final WebhookEntity entity) {
    if(!url.matches(".*/")){
      url = url.concat("/");
    }
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
    WebhookService service = retrofit.create(WebhookService.class);
    try {
      Response<Object> response = service.sendWebhook(entity).execute();
      if(response.isSuccessful()){
        return true;
      }
    } catch (IOException e) {
      LOG.debug("Webhook failure cause : {}",e);
    }
    return false;
  }

  private WebhookEntity processResults(final SubscriptionGroupDTO subscriptionGroup, final List<AnomalyResult> anomalyResults){
    final Properties webhookConfig = new Properties();
    webhookConfig.putAll(ConfigUtils.getMap(subscriptionGroup.getAlertSchemes()
        .get(PROP_WEBHOOK_SCHEME)));
    final BaseNotificationContent content = getNotificationContent(webhookConfig);
    return formatter.getWebhookEntity(anomalyResults, content, subscriptionGroup);
  }
}
