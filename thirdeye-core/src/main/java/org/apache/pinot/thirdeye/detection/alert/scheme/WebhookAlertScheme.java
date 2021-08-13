package org.apache.pinot.thirdeye.detection.alert.scheme;

import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.notification.commons.DefaultWebhookClient;
import org.apache.pinot.thirdeye.notification.commons.WebhookEntity;
import org.apache.pinot.thirdeye.notification.content.BaseNotificationContent;
import org.apache.pinot.thirdeye.notification.formatter.channels.WebhookContentFormatter;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.WebhookSchemeDto;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResult;
import org.apache.pinot.thirdeye.spi.detection.ConfigUtils;
import org.apache.pinot.thirdeye.spi.detection.annotation.AlertScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AlertScheme(type = "WEBHOOK")
@Singleton
public class WebhookAlertScheme extends DetectionAlertScheme {
  public static final String PROP_WEBHOOK_SCHEME = "webhookScheme";
  private static final Logger LOG = LoggerFactory.getLogger(WebhookAlertScheme.class);
  private final ThirdEyeCoordinatorConfiguration teConfig;

  @Inject
  public WebhookAlertScheme(
      final ThirdEyeCoordinatorConfiguration teConfig,
      final MetricConfigManager metricConfigManager,
      final AlertManager detectionConfigManager,
      final EventManager eventManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    super(metricConfigManager, detectionConfigManager, eventManager, mergedAnomalyResultManager);
    this.teConfig = teConfig;
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
      final WebhookSchemeDto webhook = subscriptionGroupDTO.getAlertSchemes().getWebhookScheme();
      if (webhook == null) {
        throw new IllegalArgumentException(
            "Invalid webhook settings in subscription group " + subscriptionGroupDTO.getId());
      }

      final List<AnomalyResult> anomalyResults = new ArrayList<>(result.getValue());
      anomalyResults.sort((o1, o2) -> -1 * Long.compare(o1.getStartTime(), o2.getStartTime()));
      final WebhookEntity entity = processResults(subscriptionGroupDTO, anomalyResults);
      if(DefaultWebhookClient.doPost(webhook.getUrl(), entity)<300){
        LOG.info("Webhook trigger successful to url {}", webhook.getUrl());
      } else {
        LOG.error("Webhook trigger failed to url {}", webhook.getUrl());
      }
    }
  }
  private WebhookEntity processResults(final SubscriptionGroupDTO subscriptionGroup, final List<AnomalyResult> anomalyResults){
    final Properties webhookConfig = new Properties();
    webhookConfig.putAll(ConfigUtils.getMap(subscriptionGroup.getAlertSchemes()
        .getWebhookScheme()));
    final BaseNotificationContent content = getNotificationContent(webhookConfig);
    final WebhookContentFormatter formatter = new WebhookContentFormatter(webhookConfig,
        content,
        teConfig,
        subscriptionGroup);
    return formatter.getWebhookEntity(anomalyResults);
  }
}
