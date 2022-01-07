package org.apache.pinot.thirdeye.detection.alert.scheme;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.pinot.thirdeye.config.UiConfiguration;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterNotification;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.mapper.ApiBeanMapper;
import org.apache.pinot.thirdeye.notification.NotificationServiceRegistry;
import org.apache.pinot.thirdeye.spi.api.AnomalyReportApi;
import org.apache.pinot.thirdeye.spi.api.NotificationPayloadApi;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.WebhookSchemeDto;
import org.apache.pinot.thirdeye.spi.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WebhookAlertScheme {

  private static final Logger LOG = LoggerFactory.getLogger(WebhookAlertScheme.class);
  private static final String ANOMALY_DASHBOARD_PREFIX = "anomalies/view/id/";
  private final NotificationServiceRegistry notificationServiceRegistry;
  private final UiConfiguration uiConfiguration;

  @Inject
  public WebhookAlertScheme(
      final NotificationServiceRegistry notificationServiceRegistry,
      final UiConfiguration uiConfiguration) {
    this.notificationServiceRegistry = notificationServiceRegistry;
    this.uiConfiguration = uiConfiguration;
  }

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

        final NotificationPayloadApi entity = new NotificationPayloadApi()
            .setSubscriptionGroup(ApiBeanMapper.toApi(subscriptionGroupDTO))
            .setAnomalyReports(toAnomalyReports(anomalyResults));

        final NotificationService webhookNotificationService = notificationServiceRegistry
            .get("webhook", ImmutableMap.of(
                "url", webhook.getUrl(),
                "hashKey", webhook.getHashKey()
            ));
        webhookNotificationService.notify(entity);
      }
    }
  }


  private List<AnomalyReportApi> toAnomalyReports(final List<MergedAnomalyResultDTO> anomalies) {
    return anomalies.stream()
        .map(dto -> new AnomalyReportApi().setAnomaly(ApiBeanMapper.toApi(dto)))
        .map(r -> r.setUrl(getDashboardUrl(r.getAnomaly().getId())))
        .collect(Collectors.toList());
  }

  private String getDashboardUrl(final Long id) {
    String extUrl = uiConfiguration.getExternalUrl();
    if (!extUrl.matches(".*/")) {
      extUrl += "/";
    }
    return String.format("%s%s%s", extUrl, ANOMALY_DASHBOARD_PREFIX, id);
  }
}
